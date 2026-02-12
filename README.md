# Aether PDF Reader

A full-screen PDF reader for Android. Swipe to change pages. That's it.

No ads. No tracking. No subscriptions. No "premium features." Just reads PDFs.

## Download

**[Latest Release (APK)](https://github.com/AgnosticMonk/aether-pdf-reader/releases/latest)** — download, install, done.

Also coming to Google Play Store soon.

## Why

Every PDF reader on the Play Store is either ad-infested, wants a subscription, or clutters the screen with toolbars and menus. We just wanted to read a PDF full-screen and swipe between pages. So we built one.

## Features

- **Full immersive screen** — no status bar, no navigation bar, just the content
- **Swipe to change pages** — horizontal swipe, smooth transitions
- **Open from anywhere** — built-in file picker, or use "Open with" from any file manager
- **Crisp rendering** — 2x scale for sharp text on high-DPI screens
- **Tiny** — under 1MB installed
- **Zero permissions** — doesn't access network, contacts, location, or anything else

## Technical Details

- Built with Kotlin targeting Android 8.0+ (API 26)
- Uses Android's native `PdfRenderer` — no third-party PDF libraries
- `ViewPager2` for page swiping
- Single activity, ~120 lines of code

## Building

Requires JDK 17 and Android SDK (API 34).

```bash
# Debug build
./gradlew assembleDebug

# Signed release (set your keystore password)
AETHER_KEYSTORE_PASS=yourpassword ./gradlew assembleRelease
```

## License

MIT
