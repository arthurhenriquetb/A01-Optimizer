# Android Optimizer v0.9 — universal (experimental)

Android package viewer and optional app-removal tool for **Motorola, Samsung, Xiaomi, Pixel and other Android brands**, developed by Arthur Henrique Tostes Batista Aranha, 2026.

**Compatibility:** Android 10 (API 29) or newer. The app reads the phone's **actual installed packages using Android PackageManager**, without a hardcoded Galaxy A01 inventory and without needing ADB for viewing. Android may hide some internal packages from the app; ADB can retrieve a more complete list. Package names, app names and functions can vary by firmware, manufacturer and version.

## What changed in v0.9

- New local read-only inventory available immediately on Android 10+, including Motorola.
- ADB commands still query the current device using `pm list packages`, never a preset A01 list.
- The **Suggestions / Sugestões** tab has checkboxes. Selecting multiple suggestions keeps the user **on the same screen**. The selected count is shown, with a separate button to review the selection.
- Device manufacturer grouping now recognizes Motorola/Lenovo, Xiaomi/MIUI/HyperOS, Samsung, Google/Android and other common brands.
- Added **conditional, not device-validated** Motorola/Xiaomi candidates. Only installed packages appear; absent ones are never invented in the interface.
- The Android 10 setup screen explains that built-in **Wireless debugging with pairing codes requires Android 11+**.
- Normal (non-system) apps can be uninstalled without ADB, one at a time via the Android system's own confirmation screen.
- Unknown or critical **system** packages are protected from the app's removal command by default. Green/yellow labels are guidance, not guarantees of safety.

## Connection

**Android 11 and newer:** enable Developer Options > Wireless debugging in Android Settings, pair this app once with the displayed six-digit code using the notification, then choose **Connect automatically**. Discovery uses the phone's current wireless ADB port. Android requires the initial system-side approval.

**Android 10:** local viewing, filtering, grouping and selection already work **without ADB**. Android 10 lacks the newer wireless pairing settings. To issue system-app removal commands, an external PC with USB ADB is needed for initial setup; the phone cannot grant itself ADB privileges. ADB over TCP/IP may be configured externally, but connectivity of this app with legacy TCP ADB is not yet verified.

A smartphone app cannot autonomously activate USB/wireless debugging or approve its initial ADB authorization, even if the user gives ordinary application permissions.

## Removing apps and safety

1. Review the name, package ID, description and risk before selecting.
2. For a **normal downloaded app without ADB**, the Android system confirms each removal. This app cannot silently uninstall it.
3. For reviewed **factory system apps with ADB**, the app uses `pm uninstall -k --user 0`; it does not wipe the system partition.
4. Unknown or critical system packages are not eligible for this app's removal command. A recovery history is saved only for factory packages removed through the ADB functionality.

**Warnings:** Some preinstalled apps are dependencies for phone calls, security, banking, Pix, notifications, firmware updates, accessibility, camera, and device boot. Do not remove packages just because the application offers a checkbox. Manufacturer and firmware dependencies are not comprehensively validated. Back up data and test critical functions after changes. Recovery is not always possible.

The optional size sort uses Android usage access; memory measurements require ADB and represent a momentary RAM snapshot, not guaranteed savings.

## Updates and build

Package ID remains `br.com.a01optimizer.v08` to permit upgrading **v0.8 to v0.9** while preserving its locally saved pairing and removal history, provided the APK uses the same signing key. The original A01 app versions up through v0.7 used a different package ID. The GitHub Actions workflow reuses its cached v0.8 debug key; GitHub Actions cache retention is not guaranteed indefinitely.

Source: Kotlin / Android, plus Kadb. APK build: **Actions > Build A01 Optimizer APK > latest successful run > Artifacts**.
