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

**Android 10:** The local package list and normal app uninstallation work without ADB. Factory-app operations require one-time USB debugging authorization and the [Windows companion](desktop/DebloaToast-Windows.zip). The guided setup inside the app opens the relevant Android settings and checks the ADB connection.

**Android 11+:** Wireless debugging can be paired using the Android pairing code. The app supports discovery of the connection port and reuses the saved pairing identity where available.

An ordinary Android app cannot enable debugging or approve its own first ADB authorization. You must approve the system request. Some PCs also require an OEM USB driver.

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
