```markdown
# Floating DoubleTap (Android)

This project provides an Android app that shows a movable floating button (overlay) and, when tapped, performs a configurable sequence of taps (default: double-tap). The floating button can be locked/unlocked from the app.

What this deliverable includes
- Android Kotlin app (app module)
- AccessibilityService to inject gestures via dispatchGesture
- FloatingService to show an overlay button (TYPE_APPLICATION_OVERLAY)
- UI to configure tap count and interval
- GitHub Actions workflow to build an unsigned debug APK and upload it as an artifact

Important permissions and notes
- Overlay ("Display over other apps") must be granted by the user in Settings.
- Accessibility service must be enabled by the user in Settings → Accessibility for the app to inject gestures.
- Uses dispatchGesture — requires API 24+.
- The APK produced by the included workflow is unsigned (debug build). For a signed release you must supply a keystore and configure secrets.

How to build locally
1. Install Android Studio and the Android SDK (API 33 recommended).
2. From the repository root, run:
   - ./gradlew assembleDebug
3. The unsigned debug APK will be located at:
   - app/build/outputs/apk/debug/app-debug.apk

How to push these files into your branch (recommended)
1. Clone your repo and switch to the branch:
   - git clone https://github.com/kabir-a/floating-doubletap.git
   - cd floating-doubletap
   - git fetch origin
   - git checkout -b feature/add-floating-doubletap origin/feature/add-floating-doubletap 2>/dev/null || git checkout -b feature/add-floating-doubletap

2. Copy the files above into the repository root (preserve paths).

3. Add, commit and push:
   - git add .
   - git commit -m "Add floating-doubletap Android app (floating overlay + accessibility gesture)"
   - git push -u origin feature/add-floating-doubletap

After push: CI and artifact
- Pushing to the branch will trigger the GitHub Actions workflow (.github/workflows/android-build.yml).
- After the workflow completes, go to GitHub → Actions → select the run → Artifacts → download app-debug-apk (contains unsigned app-debug.apk).

Install & test on a device
1. Install APK (if using adb):
   - adb install -r app/build/outputs/apk/debug/app-debug.apk
2. Open the app.
3. Grant "Display over other apps" when prompted (or via Settings → Apps → Special access → Display over other apps).
4. Enable the Accessibility Service for this app (Settings → Accessibility → [Your app] → Turn on).
5. In the app, set "Tap count" and "Interval (ms)" as desired and press "Start floating button".
6. The floating button should appear; tap it to execute the configured tap sequence at the button's screen location.
7. Use the lock toggle inside the app to lock/unlock overlay dragging.

Security & privacy
- Accessibility service has powerful capabilities. Only enable it if you trust the app.
- Explain to users why accessibility is needed.

```
