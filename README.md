# A01 Optimizer v0.8 — experimental

Lightweight Android package manager for the Samsung Galaxy A01 (SM-A015M/DS, Android 12). Developed by **Arthur Henrique Tostes Batista Aranha**, 2026.

**Default language:** English. Select **Settings → Português** for Brazilian Portuguese.

## Interface

- Fixed bottom tabs: **Apps · Guide · Connect · Settings**. No need to scroll hundreds of rows to reach core actions.
- **Apps:** searchable list of system and installed apps, with functional category and risk filters, sorted by name, total size, or current RAM snapshot. The uninstall action stays visible below the list.
- **Guide:** reviewed candidates by category and risk. Green means relatively optional, yellow may disable useful functions, and red means uncertain or dangerous. Some red entries say **KEEP** and are not recommended for removal; they remain accessible in Apps. Tap a reviewed candidate to find/select it; long-press an app in Apps to read its description.
- **Connect:** automatic wireless ADB discovery, pairing, manual fallback, and restoration of factory apps removed by this version.
- **Settings:** language and optional usage-access permission for app size measurements.

Available category filters: Samsung, Phone & SMS, Maps & Location, Google, Wi-Fi & Connections, Camera & Media, Home & Appearance, Security & Banking, Tools & Diagnostics, Other, Installed Apps, and All Apps. Samsung and Google filters cut across the functional categories.

All package entries are visible and selectable from the start. No advanced-mode unlocks, hidden categories, command-line paths, or risk-specific permission gates. The app displays one final confirmation before uninstalling. It **does not uninstall anything automatically**.

## Automatic connection without a computer

1. On startup, tap **Yes / Sim**. The app discovers the current ADB connection port using mDNS and reuses its saved pairing key.
2. On first installation, if not paired yet, tap **Open settings**. Enable **Developer Options → Wireless debugging** and open **Pair device with pairing code**.
3. Reply to the A01 notification with the six-digit **CODE / CÓDIGO**. The app tries to discover the pairing port. If that fails, enter `PORT CODE` / `PORTA CÓDIGO` instead.
4. Return to **Connect → Connect automatically**. On later launches, pressing **Yes / Sim** is sufficient as long as wireless debugging remains enabled and this app stays trusted.
5. If the router blocks service discovery, open **Connect → Manual connection** and enter the main wireless debugging *connection port* shown after the IP address, not the pairing port.

**Android limitation:** an ordinary app cannot enable USB/wireless debugging or authorize its first ADB trust request itself. Android settings must be used once. No root, Shizuku or Accessibility Service is required for automatic reconnects.

System apps are uninstalled **for Android user 0**, via `pm uninstall -k --user 0`. The original system partition is not erased. Previously removed factory apps may be restored via **Connect → Restore removed system apps** if ADB still works. Regular installed apps use `pm uninstall --user 0`, and their data may be lost. A restoration log is kept only for factory-app changes made by v0.8.

Size sorting uses Android `StorageStatsManager`; usage access must be granted manually under Android settings. RAM sorting uses a momentary PSS snapshot from `dumpsys meminfo`. Missing RAM data does **not** mean zero RAM consumption. Neither measurement predicts actual space/RAM savings from an uninstall.

## Safety

**Removing core Android, Samsung, Play Services, WebView, telephony, security or authentication components may stop the phone from booting, break mobile calls/PIX/banking apps, or permanently prevent ADB restoration.** Green/yellow/red are guidance, not verified safety guarantees for every firmware or use case. Back up your data first and remove only apps whose function you understand.

No bank compatibility is guaranteed, including Nubank, Banco do Brasil, PicPay, PagBank, Inter and Banestes. Test these, biometrics, calls, messages and Wi-Fi after any change. Disable wireless debugging after use.

Because v0.7 used an ephemeral debug signing key, v0.8 has a separate application identifier (`br.com.a01optimizer.v08`) and requires a new one-time pairing. Starting with v0.8, GitHub Actions caches its debug signing key to support future in-place updates, although that cache is not guaranteed permanent. Keep previous versions until their restoration histories are no longer needed.

## Build

Kotlin / Android, with [Kadb](https://github.com/flyfishxu/Kadb) (Apache 2.0). GitHub Actions builds the experimental APK under **Actions → Build APK → Artifacts**.
