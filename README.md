# Android Optimizer — v0.10

**v0.10 is the current working version.** This is the baseline for future development.

Android Optimizer is an Android 10+ app for viewing installed packages and managing unwanted applications, including some factory-installed apps through ADB. Designed for Motorola, Samsung, Xiaomi, and other Android devices; actual compatibility varies by manufacturer and firmware.

**Project concept and requirements:** Arthur Henrique Tostes Batista Aranha  
**Code:** Written with ChatGPT (AI-generated). The project has not undergone an independent security audit.

## Versions

| Version | Status | Availability |
| --- | --- | --- |
| **v0.10** | Current working version, with conservative protections and **no unrestricted critical-package removal**. All normal development continues from here. | Source code in this repository; APK built via GitHub Actions |
| **v0.10.1** | **Unreleased, untested, extremely dangerous experiment.** The experimental expert mode would let users attempt to remove **any detected package**, including critical Android system components, after a warning. It could disable essential functions or prevent the device from booting. **Not a successor or recommended upgrade to v0.10.** | **Not published. No source code or APK is offered for this experiment.** |

## What v0.10 does

- Shows packages found on the actual connected Android device, rather than using a fixed list for one manufacturer.
- Lets users search, filter by function/manufacturer, and sort by app name, size, or RAM information where available.
- Offers a reviewed suggestion list and multiple-app selection.
- Uses risk categories to distinguish lower-risk, conditional, and potentially harmful removals.
- **Blocks known critical and unreviewed system packages from the ADB uninstall operation.** Risk classifications are not guarantees of safety on every phone.
- Can uninstall ordinary user apps with Android's normal confirmation.
- Can attempt to remove eligible factory apps for Android user 0 using ADB, without requiring root. This does **not** erase applications from the read-only system image.

## Setup and compatibility

**Android 10:** Installed apps can be browsed without ADB. Factory-app management requires a one-time USB debugging authorization using a Windows computer. The [Windows helper ZIP](desktop/Android-Optimizer-Windows.zip) contains scripts that download Google's official Android Platform Tools if necessary, detect the USB-connected phone, and help establish a local ADB connection. Users must approve USB debugging on their device; on some computers, OEM USB drivers are also required. No manual ADB commands are needed.

**Android 11+:** The app supports Android's wireless debugging pairing flow, including connection-port discovery where supported by the device.

A completely permission-free, zero-interaction setup is not possible for ordinary Android applications. The app does not automatically remove packages without the user's selection and confirmation.

## Security and limitations

- On Android 10, the Windows helper may enable legacy ADB over TCP port **5555**. This opens a debugging endpoint to the local network; use only on a trusted private network, and disable network debugging after use. A reboot may require repeating the setup.
- Removing even supposedly optional packages can break device features, authentication, calling, banking apps, Wi-Fi, or updates. **Back up important data and review packages before removal.**
- ADB-based uninstall for user 0 is not always reversible; restoring an application or device functionality is not guaranteed.
- Compatibility and package classifications are not exhaustively tested on every device, and the application is not independently security-audited.

## Build

Built with Kotlin, Android minSdk 29 (Android 10), Kadb 2.1.5, and Gradle 9.4.1. GitHub Actions compiles a **debug-signed** v0.10 APK and makes it available as a workflow artifact. This is a test-oriented distribution method, not a Play Store release.

The application ID remains `br.com.a01optimizer.v08` for compatibility with certain earlier debug-signed installations.
