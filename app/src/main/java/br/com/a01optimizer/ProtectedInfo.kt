package br.com.a01optimizer

/** Informações sobre componentes críticos e pacotes de sistema desconhecidos.
 * A classificação alerta sobre riscos e não impede a seleção na versão 0.11.
 */
object ProtectedInfo {
    private val critical = setOf(
        "android", "com.android.systemui", "com.android.settings", "com.android.shell",
        "com.android.phone", "com.android.keychain", "com.android.se",
        "com.android.vending", "com.google.android.gms", "com.google.android.gsf",
        "com.google.android.webview", "com.android.chrome",
        "com.google.android.packageinstaller", "com.google.android.permissioncontroller",
        "com.sec.android.app.launcher", "com.samsung.android.honeyboard",
        "com.sec.imsservice", "com.sec.epdg", "com.sec.android.emergencylauncher",
        "com.samsung.android.biometrics.app.setting", "com.samsung.android.kgclient",
        "com.motorola.ccc.ota", "com.motorola.launcher3", "com.motorola.camera3",
        "com.miui.home", "com.miui.securitycenter", "com.xiaomi.finddevice"
    )

    fun isCritical(packageName: String): Boolean = packageName in critical
    fun criticalIds(): Set<String> = critical

    private val reasons = mapOf(
        "com.google.android.gms" to "Google Play Services: usado por autenticação e serviços de muitos aplicativos, inclusive bancos.",
        "com.google.android.gsf" to "Google Services Framework: infraestrutura Google e serviços de conta.",
        "com.android.vending" to "Google Play Store: instalações, atualizações e verificações de aplicativos.",
        "com.google.android.webview" to "Android System WebView: permite abrir conteúdos e autenticações da web dentro de aplicativos.",
        "com.android.chrome" to "Chrome: pode participar de autenticação e links externos de aplicativos.",
        "com.android.systemui" to "Interface principal do Android: barra de status, notificações e controles.",
        "com.sec.android.app.launcher" to "Tela inicial Samsung: necessária para a navegação normal pelo aparelho.",
        "com.samsung.android.honeyboard" to "Teclado Samsung: importante para digitação, inclusive em aplicativos bancários.",
        "com.android.keychain" to "Certificados e credenciais usados em conexões e autenticações.",
        "com.android.se" to "Serviço de elementos seguros usado por funcionalidades de segurança.",
        "com.samsung.android.biometrics.app.setting" to "Configuração de biometria; pode afetar desbloqueio e autenticação.",
        "com.sec.android.emergencylauncher" to "Modo de emergência Samsung: preservado por segurança; já apareceu desativado no seu relatório.",
        "com.samsung.android.fmm" to "Localização e proteção do aparelho perdido; preservar para segurança.",
        "com.samsung.android.app.updatecenter" to "Serviço de atualização Samsung; remoção pode interromper atualizações.",
        "com.sec.android.app.samsungapps" to "Galaxy Store: atualizações e distribuição de aplicativos Samsung.",
        "com.samsung.android.kgclient" to "Controle de integridade e segurança Knox; pode afetar restrições e autenticação.",
        "com.samsung.android.lool" to "Cuidados com o dispositivo, bateria e manutenção do sistema Samsung.",
        "com.sec.imsservice" to "Serviços IMS: podem ser necessários para chamadas e mensagens da operadora.",
        "com.sec.epdg" to "Comunicação da operadora e chamadas via Wi-Fi; conservar.",
        "com.android.phone" to "Telefonia e conexão com a operadora; componente essencial.",
        "com.android.settings" to "Configurações do sistema Android; componente essencial.",
        "com.google.android.permissioncontroller" to "Gerenciamento de permissões e privacidade do Android.",
        "com.android.providers.downloads" to "Gerenciador de downloads usado por diversos aplicativos.",
        "com.google.android.apps.restore" to "Restauração de dados e aplicativos do Google; pode ser importante ao recuperar o aparelho.",
        "com.google.android.apps.turbo" to "Serviços de saúde do aparelho e gerenciamento adaptativo; impacto não validado.",
        "com.google.android.syncadapters.contacts" to "Sincronização de contatos Google; pode afetar contatos, chamadas e funções de Pix."
    )

    fun reason(packageName: String): String = when {
        reasons.containsKey(packageName) -> reasons.getValue(packageName)
        packageName.startsWith("android.auto_generated_rro") ||
            packageName.contains(".overlay.") || packageName.contains(".resources") ||
            packageName.startsWith("com.android.theme.") ||
            packageName.startsWith("com.samsung.internal.systemui") ->
            "Sobreposição ou recurso de interface do Android; não tratado como app opcional."
        packageName.startsWith("com.android.providers.") ||
            packageName.startsWith("com.samsung.android.providers.") ->
            "Provedor de dados do sistema; pode afetar armazenamento e outros aplicativos."
        packageName.startsWith("vendor.") || packageName.startsWith("com.qualcomm.") ||
            packageName.startsWith("com.qti.") ->
            "Componente do fabricante de hardware; a dependência do aparelho não foi validada."
        packageName.contains("knox", ignoreCase = true) ||
            packageName.startsWith("com.sec.enterprise.") ||
            packageName.startsWith("com.samsung.android.mdm") ->
            "Gerenciamento ou segurança Samsung/Knox; não remover sem validação específica."
        packageName.startsWith("com.sec.ims") || packageName.startsWith("com.sec.phone") ||
            packageName.startsWith("com.sec.epdg") ->
            "Ligado à telefonia e à operadora; remover pode afetar chamadas e rede."
        packageName.startsWith("com.motorola.") || packageName.startsWith("com.moto.") ||
            packageName.startsWith("com.lenovo.") ->
            "Componente Motorola/Lenovo. Sua função e dependências precisam ser avaliadas neste modelo."
        packageName.startsWith("com.miui.") || packageName.startsWith("com.xiaomi.") ||
            packageName.startsWith("com.mi.") ->
            "Componente Xiaomi/MIUI/HyperOS. Sua função e dependências precisam ser avaliadas neste modelo."
        packageName.startsWith("com.google.android.") ->
            "Componente Google ainda não validado para remoção; pode ser usado por outros aplicativos."
        packageName.startsWith("com.samsung.") || packageName.startsWith("com.sec.") ->
            "Componente Samsung ainda não validado para remoção neste modelo."
        packageName.startsWith("com.android.") || packageName == "android" ->
            "Componente Android ainda não validado para remoção; pode ter dependências internas."
        else -> "Função e dependências não validadas; remover pode causar falhas no Android."
    }
}
