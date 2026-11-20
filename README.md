# Android Optimizer — v0.10 (current working version)

## Version status

| Version | Status | Public distribution |
| --- | --- | --- |
| **v0.10** | **Current working version and the baseline for all future normal development.** Includes guided Android 10 setup and conservative risk categories; **no black/critical removal tier**. | Intended public release |
| **v0.10-X** (formerly v0.11) | **PRIVATE, UNTESTED, EXTREMELY HIGH-RISK EXPERIMENT — NOT A RELEASE.** Expert mode can select **every detected package**, including Android's critical components, for attempted removal after a single warning. It could disable essential functions, cause data loss, boot failures or force a factory reset. **This experiment will not be the basis for future normal versions.** | **No public source code, build, APK, or downloads** |

**Important:** v0.10-X is mentioned here only to document its existence and safety risks. Its private code and builds are **not** part of the public v0.10 release. Do not publish the experimental branch, its Git history or its build artifacts. This repository currently contains historical experimental commits, so **do not make this existing repository public without first separating the v0.10 source into a fresh public repository with clean history**.

## About v0.10

Lightweight app browser and package management for **Android 10+** across Motorola, Samsung, Xiaomi and other manufacturers. Created by **Arthur Henrique Tostes Batista Aranha · 2026**.

## No technical tutorial needed

The application reads the actual installed apps directly from Android, without ADB. It offers searchable lists, functional and manufacturer filters, name/size/RAM sorting where available, and selected-app checkboxes in the suggestions tab. You can mark several suggestions without leaving that screen.

The app includes a **guided, on-device full-access setup for Android 10**:
1. It opens the phone information and developer settings for the user, one step at a time.
2. It provides a **Windows companion ZIP** containing a double-clickable **Conectar-Windows.cmd**.
3. The companion downloads **Google's official Android Platform Tools** if necessary, detects the connected USB device, waits for the Android authorization prompt, and automatically enables legacy `adb tcpip 5555`.
4. Back in the phone app, **Check connection / Verificar conexão** tests local port 5555 and confirms success. The main app uses the same saved pairing identity and preserves the previously selected apps.
5. On later launches, the app attempts to reconnect using the saved port without prompting if the connection is no longer available.

**Important:** Android cannot allow an ordinary app to enable Developer options or authorize its own ADB session without user interaction. The user must grant USB debugging approval on the phone. On some Windows PCs, OEM USB drivers or a USB data cable are also necessary. A true zero-interaction setup is impossible without root or prior privileged provisioning.

**Android 11+:** existing wireless-pairing flow remains available, with in-app confirmation and automatic connection-port discovery.

## Windows companion

The [Windows helper ZIP](https://github.com/arthurhenriquetb/A01-Optimizer/raw/refs/heads/main/desktop/Android-Optimizer-Windows.zip) contains these human-facing actions:
- **Conectar-Windows.cmd**: connect and prepare the phone once using USB, with automatic ADB installation from Google.
- **Desconectar-Windows.cmd**: attempt to turn off the active legacy network-debugging connection. The safest fallback is to restart the phone and turn off USB debugging in developer settings.

No ADB commands need to be typed by the user. Unzip and open the appropriate .cmd file on a Windows computer, then approve USB debugging on the phone. This procedure does not install proprietary Motorola drivers, which may be needed on some computers.

## Compatibility

- **Android 10**: read and select installed apps without ADB; uninstall regular apps with Android's built-in confirmation. Factory-app removal requires the initial USB + Windows companion, and a permitted local TCP connection.
- **Android 11+**: one-time system wireless pairing plus reconnect (where the firmware supports it).
- For all versions, package inventory comes from the **current phone**, not a preset Samsung list. Suggestions are reviewed IDs where available and cannot guarantee safety on unfamiliar devices.

## Privacy and security

Legacy `adb tcpip 5555` exposes a debugging endpoint over the local network, not just to the app. Use it **only on a trusted private Wi-Fi network**; stop network debugging when finished. After a reboot, Android 10 often requires running the companion again.

Removing essential system packages may break boot, phone calls, banks, authentication, camera, Wi-Fi or updates. Uninstalling factory apps through ADB affects Android user 0 rather than the read-only system partition; restoration is not guaranteed. The app does not automatically remove anything. Unknown or critical system packages are excluded from uninstall commands. Normal apps may lose their data.

## Build

Kotlin / Android 10+ (minSdk 29), Kadb 2.1.5, Gradle 9.4.1. GitHub Actions publishes the debug-signed APK in **Artifacts**. Application ID is kept at `br.com.a01optimizer.v08` so compatible signed v0.8/0.9 installs can be upgraded without deleting local data. Preserving the cached debug signing key in GitHub Actions remains necessary.
