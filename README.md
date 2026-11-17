# MNA Minimal Launcher

A lightweight native Android launcher project (Java, no third-party libraries).

## Features
- Default Home launcher activity
- Dark minimal home screen with clock/date
- App drawer with 4-column grid
- Search installed launchable apps
- Alphabetical app ordering
- Tap to launch; long-press for Open / App info
- Refreshes app list when returning to launcher

## Build APK
1. Install Android Studio (current stable) on a computer.
2. Open this project folder in Android Studio.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Install and set as default
1. Transfer the APK to the Android phone and install it.
2. Press Home. Choose **MNA Minimal Launcher**.
3. Select **Always** to make it the default launcher.
   You can change it later in Android Settings > Apps > Default apps > Home app.

## Notes
- Minimum Android version: Android 6.0 (API 23).
- This is a starter version. The current home screen favourites are the first five apps alphabetically.
- `QUERY_ALL_PACKAGES` is used so the app drawer can list installed launchable apps. This is appropriate for a user-installed launcher, but app-store distribution has additional policy requirements.
- The project source is provided; this environment does not have the Android SDK/Gradle build toolchain installed, so an APK has not been compiled here.
