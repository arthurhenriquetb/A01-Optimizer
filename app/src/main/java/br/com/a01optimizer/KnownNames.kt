package br.com.a01optimizer

/** Nomes amigáveis conferidos a partir do inventário do Galaxy A01.
 * O nome do próprio Android é usado para o restante; o identificador aparece sempre.
 */
object KnownNames {
    private val names = mapOf(
        "android" to "Sistema Android",
        "com.android.systemui" to "Interface do sistema Android",
        "com.android.settings" to "Configurações do Android",
        "com.android.shell" to "Serviço de comandos ADB",
        "com.android.phone" to "Serviços de telefonia Android",
        "com.android.vending" to "Google Play Store",
        "com.android.keychain" to "Certificados e credenciais",
        "com.android.se" to "Elemento seguro do Android",
        "com.android.printspooler" to "Fila de impressão Android",
        "com.android.bookmarkprovider" to "Provedor de favoritos do navegador",
        "com.android.wallpaper.livepicker" to "Seletor de papéis de parede animados",
        "com.android.wallpaperbackup" to "Backup de papéis de parede",
        "com.android.providers.downloads" to "Gerenciador de downloads Android",
        "com.android.providers.media" to "Banco de dados de fotos e arquivos",
        "com.android.providers.telephony" to "Banco de dados de SMS e telefonia",
        "com.google.android.gms" to "Serviços do Google Play",
        "com.google.android.gsf" to "Google Services Framework",
        "com.google.android.webview" to "Android System WebView",
        "com.google.android.packageinstaller" to "Instalador de aplicativos Android",
        "com.google.android.permissioncontroller" to "Gerenciador de permissões",
        "com.google.android.syncadapters.contacts" to "Sincronização dos contatos Google",
        "com.google.android.gms.location.history" to "Histórico de localização Google",
        "com.google.mainline.telemetry" to "Telemetria de módulos Google",
        "com.google.android.apps.restore" to "Restauração de dados Google",
        "com.google.android.apps.turbo" to "Saúde do dispositivo Google",
        "com.sec.android.app.launcher" to "Tela inicial Samsung (One UI Home)",
        "com.samsung.android.honeyboard" to "Teclado Samsung",
        "com.sec.android.app.camera" to "Câmera Samsung",
        "com.sec.android.app.clockpackage" to "Relógio e alarmes Samsung",
        "com.sec.android.app.myfiles" to "Meus Arquivos Samsung",
        "com.sec.android.app.samsungapps" to "Galaxy Store",
        "com.samsung.android.app.updatecenter" to "Central de atualizações Samsung",
        "com.sec.android.emergencylauncher" to "Modo de emergência Samsung",
        "com.sec.imsservice" to "Telefonia IMS Samsung",
        "com.sec.epdg" to "Chamadas Wi-Fi da operadora",
        "com.samsung.android.kgclient" to "Knox Guard Samsung",
        "com.samsung.android.fmm" to "Localizar meu dispositivo Samsung",
        "com.samsung.android.biometrics.app.setting" to "Configurações de biometria",
        "com.samsung.android.lool" to "Assistência do aparelho Samsung",
        "com.samsung.android.callbgprovider" to "Fundos de chamada Samsung",
        "com.samsung.android.app.soundpicker" to "Seletor de sons Samsung",
        "com.samsung.android.secsoundpicker" to "Seletor de toques Samsung",
        "com.samsung.android.mapsagent" to "Agente de mapas Samsung",
        "com.samsung.android.networkdiagnostic" to "Diagnóstico de rede Samsung",
        "com.sec.android.app.billing" to "Pagamentos da Galaxy Store",
        "com.sec.android.app.quicktool" to "Ferramentas rápidas Samsung",
        "com.samsung.android.safetyinformation" to "Informações de segurança Samsung",
        "com.samsung.android.bluelightfilter" to "Filtro de luz azul Samsung",
        "com.samsung.android.beaconmanager" to "Detecção de acessórios Samsung",
        "com.samsung.android.calendar" to "Calendário Samsung",
        "com.samsung.android.faceservice" to "Reconhecimento facial Samsung",
        "com.samsung.android.sm.devicesecurity" to "Segurança do dispositivo Samsung",
        "com.samsung.android.scloud" to "Samsung Cloud",
        "com.samsung.android.dynamiclock" to "Tela de bloqueio dinâmica Samsung",
        "com.samsung.android.game.gos" to "Otimização de jogos Samsung",
        "com.dti.samsung" to "Instalação de apps pela operadora (DTI)"
    )

    fun label(packageName: String): String? = names[packageName]

    fun isSamsung(packageName: String): Boolean =
        packageName.startsWith("com.samsung.") || packageName.startsWith("com.sec.") ||
            packageName.startsWith("com.osp.") || packageName.startsWith("com.dti.samsung") ||
            packageName.startsWith("com.wssyncmldm") || packageName.startsWith("com.wsomacp")

    fun group(packageName: String): String = when {
        isSamsung(packageName) -> "SAMSUNG"
        packageName == "android" || packageName.startsWith("android.") ||
            packageName.startsWith("com.android.") || packageName.startsWith("com.google.") ->
            "ANDROID / GOOGLE"
        else -> "OUTROS / OPERADORA"
    }
}
