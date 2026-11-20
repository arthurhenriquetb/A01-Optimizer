# DebloaToast Expert v0.11

**Developer:** Arthur Henrique Tostes Batista Aranha  
**AI transparency:** The application was designed and directed by the developer, with code produced using ChatGPT assistance.

**⚠ Expert release — severe risk of permanent device failure.**

The Expert edition now includes the **⚫ Black — Critical** risk category, after green, yellow and red. You can select and attempt to uninstall packages even when they are critical Android system components or are not validated for removal. The only in-app removal gate is a final warning and confirmation. Android or ADB itself may still reject the command.

**This can permanently disable the phone or prevent it from booting, break calls, network connectivity, authentication or banking apps, erase access needed for recovery and result in lost data.** Factory firmware recovery may be needed or may fail. **Users choose which packages to remove and accept responsibility for the consequences. Restoration is not guaranteed.** Make a backup before proceeding.

### Features
- Lists actual installed and factory packages on Android 10+ devices across Motorola, Xiaomi, Samsung and other manufacturers.
- Search, manufacturer/function/risk filters, and batch selection from suggestions.
- Sort by name, storage size or memory snapshot when available.
- Uninstall regular apps through Android's confirmation, or attempt system-app removal with authorized ADB.
- Restore history for removed system packages where the phone still permits it.
- English and optional Brazilian Portuguese.

### Guided ADB configuration
- **Android 10:** The app offers a Windows helper to download on your phone. Copy the ZIP to a Windows PC, extract and run `Conectar-Windows.cmd`. It automatically downloads Google's ADB tools, detects the phone over USB and prepares the connection. You still must enable USB debugging and approve Android's authorization prompt. You can unplug USB after a working network connection has been established.
- **Android 11+:** Use the phone's built-in wireless debugging and pair the app using Android's code; a computer is normally unnecessary.
- **No ADB:** You can browse packages and remove regular downloaded apps.

### Download
- `DebloaToast-Expert-v0.11.apk` — Android installer, debug-signed.
- `DebloaToast-Windows.zip` — optional Windows companion.

This software is experimental, has not been independently security audited, and is offered **as-is, without guarantees that a device can be restored after package removal**.
