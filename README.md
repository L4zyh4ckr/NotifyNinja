
# NotifyNinja — Notification‑only Reminders (Android 15+)

**No launcher icon.** Manage reminders entirely from the notification drawer. Add/edit via the notification's *Manage* action.

## Features
- Android 15 (API 35) target/compile.
- Schedule exact alarms via `AlarmManager.setAlarmClock` (no special permission).
- Categories: Payments, Classes, Journeys, Exercise, Other.
- Repeats: None, Daily, Weekly, Monthly.
- Reschedules after reboot.

## Build without Android Studio (GitHub Actions)

1. Create a GitHub repository and push this project.
2. Add this workflow file at `.github/workflows/build.yml` (already included):

The workflow:
- Uses Java 17.
- Installs specific Gradle (no wrapper needed).
- Builds `app:assembleDebug` and uploads the APK as an artifact.

## Local build (CLI)
You can also build locally with an installed Gradle 8.7+ and Android SDK:
```bash
gradle :app:assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

## How to use
- Install the APK.
- Pull down the notification shade; you'll see **NotifyNinja** persistent notification (first run after a scheduled reminder fires).
- Tap **Manage** (the notification content) to open the hidden manager activity and add reminders.

## Why no launcher icon?
The `ManagerActivity` has **no LAUNCHER** intent filter, so the app stays off the home screen. You reach it via the notification action only.

## Testing
- Unit test: `:app:testDebugUnitTest`
- Manual: create a reminder a minute in the future—observe exact-time notification.

## Troubleshooting
- **No notification?** Ensure **Notifications** are enabled for NotifyNinja in system settings.
- **Battery optimizations**: If the device is extremely aggressive, allow unrestricted battery for best reliability.
- **Reboot lost schedules**: The app listens to `BOOT_COMPLETED` and reschedules; wait a minute after boot.

## Android 15 compliance notes
- No foreground services for scheduling; alarms/notifications only.
- No launcher category, edge-to-edge is inherited from Material theme.
- Uses insets-friendly transparent bars, no status bar color hacks.
