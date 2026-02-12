package com.aether.reader

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var emptyView: TextView
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    private val openDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { openPdf(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        emptyView = findViewById(R.id.emptyView)

        goFullscreen()

        // Tap center of screen to toggle system bars
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})

        // Check if opened from an intent (file manager "Open with")
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            openPdf(intent.data!!)
        } else {
            // Show file picker
            openDocument.launch(arrayOf("application/pdf"))
        }

        // Tap empty view to open file picker
        emptyView.setOnClickListener {
            openDocument.launch(arrayOf("application/pdf"))
        }
    }

    private fun openPdf(uri: Uri) {
        // Take persistable permission if available
        try {
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) { }

        // Clean up previous
        closePdf()

        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return
        fileDescriptor = fd
        pdfRenderer = PdfRenderer(fd)

        val renderer = pdfRenderer ?: return
        val pageCount = renderer.pageCount

        if (pageCount == 0) {
            emptyView.text = "Empty PDF"
            emptyView.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
            return
        }

        emptyView.visibility = View.GONE
        viewPager.visibility = View.VISIBLE
        viewPager.adapter = PdfPageAdapter(renderer, pageCount)
    }

    private fun goFullscreen() {
        window.insetsController?.let {
            it.hide(android.view.WindowInsets.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun closePdf() {
        viewPager.adapter = null
        pdfRenderer?.close()
        pdfRenderer = null
        fileDescriptor?.close()
        fileDescriptor = null
    }

    override fun onDestroy() {
        closePdf()
        super.onDestroy()
    }

    // --- Adapter: renders each PDF page as a bitmap in an ImageView ---

    private class PdfPageAdapter(
        private val renderer: PdfRenderer,
        private val pageCount: Int
    ) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

        class PageViewHolder(val imageView: ImageView) :
            RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(android.graphics.Color.BLACK)
            }
            return PageViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            val page = renderer.openPage(position)

            // Render at 2x for crisp text on high-DPI screens
            val scale = 2
            val bitmap = Bitmap.createBitmap(
                page.width * scale,
                page.height * scale,
                Bitmap.Config.ARGB_8888
            )
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            holder.imageView.setImageBitmap(bitmap)
        }

        override fun getItemCount() = pageCount
    }
}
