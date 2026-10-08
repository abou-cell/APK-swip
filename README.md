# Auto Swipe Accessibility

Android accessibility helper for hands-free/right-swipe assistance.

## Behaviour

- Configure your Tinder filters and location directly in Tinder.
- Open **Auto Swipe** and enable its Android Accessibility service once.
- Press **START SWIPING**.
- Tinder opens and the app performs repeated right-swipes while Tinder is in the foreground.
- A floating **STOP** button remains available to stop immediately.
- The app pauses automatically when Tinder is not the foreground app.
- The app declares **no INTERNET permission**.

## Build

GitHub Actions builds a debug APK on every push to `main` and exposes it as the artifact `AutoSwipeAccessibility-debug-apk`.

> Note: automated interaction with a third-party app can conflict with that service's terms. Use this only as an accessibility aid and at your own discretion.
