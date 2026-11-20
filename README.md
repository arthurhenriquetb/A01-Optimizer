# DebloaToast

**Android debloating made simple.**

DebloaToast is a lightweight Android package manager for Android 10 and later, designed to browse installed apps, identify optional packages, and remove unwanted apps with user confirmation. It uses the device's actual package inventory instead of a fixed manufacturer-specific list. Compatibility varies by device and Android version.

## Current release

**DebloaToast v0.10** is the only published release of the project. [Download the Android APK](../../releases/latest).

The v0.10.1 experiment explored unrestricted removal of critical system components. **It is unreleased, untested, and not available for download.** The published v0.10 intentionally protects known critical and unreviewed system packages.

## Features

- Browse installed and factory system packages on Motorola, Samsung, Xiaomi, and other Android devices.
- Search by name or package ID, filter by manufacturer, function and risk, and sort by name, size or RAM where supported.
- Review package suggestions and select several apps with checkboxes without leaving the suggestions tab.
- Uninstall regular applications through Android's own confirmation screen.
- Remove supported optional factory-installed applications for Android user 0 with an authorized ADB connection, without root.
- Restore factory packages removed through this app when supported by the device.
- English interface by default, with an optional Brazilian Portuguese translation.
- Guided first-time setup, including a Windows companion for Android 10.

## Android compatibility and setup

### Guided setup — no command line

DebloaToast works in **basic mode without ADB**: browse packages, use filters and suggestions, and uninstall regular downloaded apps through Android's confirmation screen.

For factory-app management, DebloaToast guides you through the connection process, without asking you to install or type ADB commands yourself:

- **Android 10 (Windows PC + USB data cable for first setup):** In the app, open **Connect → Set up full access**. It guides you to enable USB debugging and provides a **Download Windows helper** button, so the helper ZIP can be downloaded on your phone. Transfer the ZIP to your Windows PC (for example, over USB), extract it, and double-click **Conectar-Windows.cmd**. The helper automatically downloads **Google's official Android Platform Tools** if needed, detects the connected phone and sets up ADB. Accept the USB debugging authorization on the phone, then return to DebloaToast and tap **Check connection**. Once the network connection is active, the USB cable can be disconnected while you keep working on the same trusted network.
- **Android 11+ (no computer for supported devices):** Use the phone's built-in **Wireless debugging** option. Pair DebloaToast using the code shown by Android; the app discovers the connection port and can reconnect using its saved pairing authorization when wireless debugging remains enabled.

You **don't need to download or configure ADB separately**: the Windows helper obtains the official tools, and Android 11+ uses the system's own wireless debugging functionality. This aims to make setup usable without an external tutorial.

**Limits:** An ordinary Android app cannot enable debugging or approve its own first ADB authorization. Some Windows PCs need an OEM USB driver, and the wireless connection may need to be set up again after a phone restart. The Android 10 helper opens legacy ADB on TCP port 5555; use it only on trusted Wi-Fi and turn off network debugging when finished.

## Risk and safety

**Green** indicates lower-risk candidates, **yellow** indicates feature-dependent removals, and **red** indicates unknown or higher-risk packages. Risk labels are guidance, not a universal safety guarantee. **The published v0.10 blocks known critical and unreviewed system packages from its ADB removal operation.**

Removing system packages can disrupt updates, cellular service, accounts, accessibility, banking apps or even device startup. Back up important data before making changes. ADB uninstall for Android user 0 does not erase the read-only system image; restoration is not guaranteed.

The Android 10 Windows companion may enable legacy ADB TCP mode on port 5555, which exposes a debugging endpoint on the local network. Use only on trusted private Wi-Fi and disable the debugging connection afterward.

## Technical information

- Android 10+ (API 29+), Kotlin, Kadb and Gradle.
- Built as a debug-signed APK; it is not a Play Store release.
- Current package identifier remains `br.com.a01optimizer.v08` for compatibility with earlier signed installations.
- [Source code](../../) and [latest release](../../releases/latest).

No automatic uninstallation is performed. This software has not been independently security-audited.
