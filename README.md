# A01 Optimizer v0.7 — experimental

Lightweight Android package manager for the Samsung Galaxy A01 (SM-A015M/DS, Android 12). Developed by **Arthur Henrique Tostes Batista Aranha**, 2026.

**Default language:** English. Select **Settings → Português** for Brazilian Portuguese.

## Interface

- Fixed bottom tabs: **Apps · Guide · Connect · Settings**. No need to scroll hundreds of rows to reach core actions.
- **Apps:** searchable list of system and installed apps, with functional category and risk filters, sorted by name, total size, or current RAM snapshot. The uninstall action stays visible below the list.
- **Guide:** reviewed candidates by category and risk. Green means relatively optional, yellow may disable useful functions, and red means uncertain or dangerous. Some red entries say **KEEP** and are not recommended for removal; they remain accessible in Apps. Tap a reviewed candidate to find/select it; long-press an app in Apps to read its description.
- **Connect:** wireless ADB pairing, connection, and restoration of factory apps removed by this version.
- **Settings:** language and optional usage-access permission for app size measurements.

Available category filters: Samsung, Phone & SMS, Maps & Location, Google, Wi-Fi & Connections, Camera & Media, Home & Appearance, Security & Banking, Tools & Diagnostics, Other, Installed Apps, and All Apps. Samsung and Google filters cut across the functional categories.

All package entries are visible and selectable from the start. No advanced-mode unlocks, hidden categories, command-line paths, or risk-specific permission gates. The app displays one final confirmation before uninstalling. It **does not uninstall anything automatically**.

## Connect without a computer

1. Connect the Galaxy A01 to a private Wi-Fi network and enable **Developer Options → Wireless debugging**.
2. Open **Connect → Pair** in the app and choose **Pair device with pairing code** in Android settings.
3. Reply to the A01 notification with `PORT CODE` (two values separated by one space). In Portuguese, the prompt reads `PORTA CÓDIGO`. Use the temporary pairing port on the pairing dialog.
4. Return to the wireless debugging main screen. In the app, enter the **connection port** shown after the device IP address, **not** the pairing port.
5. Tap **Connect**. Apps and memory information load from the phone over ADB. Use **Refresh** to update the list.

System apps are uninstalled **for Android user 0**, via `pm uninstall -k --user 0`. The original system partition is not erased. Previously removed factory apps may be restored via **Connect → Restore removed system apps** if ADB still works. Regular installed apps use `pm uninstall --user 0`, and their data may be lost. A restoration log is kept only for factory-app changes made by v0.7.

Size sorting uses Android `StorageStatsManager`; usage access must be granted manually under Android settings. RAM sorting uses a momentary PSS snapshot from `dumpsys meminfo`. Missing RAM data does **not** mean zero RAM consumption. Neither measurement predicts actual space/RAM savings from an uninstall.

## Safety

**Removing core Android, Samsung, Play Services, WebView, telephony, security or authentication components may stop the phone from booting, break mobile calls/PIX/banking apps, or permanently prevent ADB restoration.** Green/yellow/red are guidance, not verified safety guarantees for every firmware or use case. Back up your data first and remove only apps whose function you understand.

No bank compatibility is guaranteed, including Nubank, Banco do Brasil, PicPay, PagBank, Inter and Banestes. Test these, biometrics, calls, messages and Wi-Fi after any change. Disable wireless debugging after use.

As GitHub Actions produces a differently signed debug APK on different runs, v0.7 uses an independent application identifier (`br.com.a01optimizer.v07`). Keep previous versions installed until their own restoration histories are no longer needed.

## Build

Kotlin / Android, with [Kadb](https://github.com/flyfishxu/Kadb) (Apache 2.0). GitHub Actions builds the experimental APK under **Actions → Build APK → Artifacts**.
