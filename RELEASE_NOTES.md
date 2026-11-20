# DebloaToast v0.10

**Android debloating made simple.**

DebloaToast helps you inspect installed Android packages, understand their purpose, select multiple unwanted apps, and remove eligible packages with appropriate confirmation.

**Compatibility:** Android 10 and newer; Motorola, Samsung, Xiaomi and other Android devices (firmware behavior may vary).

**Highlights**
- Searchable app list with manufacturer and function filters.
- Suggestions grouped by risk, plus multiple-selection checkboxes.
- Sort by name, package size or RAM where supported.
- **No separate ADB install or command line required.** The app provides a guided connection flow instead of a technical tutorial.
- **Android 10:** the app offers a button to download its Windows helper ZIP to the phone. Transfer the ZIP to a Windows PC, extract it, and double-click **Conectar-Windows.cmd**. The helper downloads Google's official ADB tools automatically, detects the USB-connected phone and configures ADB. Approve USB debugging on the phone; once the connection works, the USB cable can be disconnected while using the same trusted network.
- **Android 11+:** use Android's built-in wireless debugging and pair with the app directly on the phone. No PC is normally required on supported devices.
- **Basic mode:** browsing packages, viewing suggestions and uninstalling regular user-installed apps do not require an ADB connection.
- English interface with optional Brazilian Portuguese.
- Restore history for factory apps removed through the tool, where supported.

**Safety:** This public version excludes known critical and unreviewed system packages from its ADB uninstall operation. Uninstalling system software can still break important functions. Back up your data before modifying your phone. No app is removed without your action.

**Files**
- `DebloaToast-v0.10.apk`: Android installer.
- `DebloaToast-Windows.zip`: Windows setup companion for Android 10.

An unrestricted v0.10.1 experiment has not been published. This APK is a debug-signed release and has not undergone an independent security audit.
