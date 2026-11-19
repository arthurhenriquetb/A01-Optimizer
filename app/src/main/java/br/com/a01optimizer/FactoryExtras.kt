package br.com.a01optimizer

/** Extras presentes no inventário original do Galaxy A01 SM-A015M/DS.
 * Nenhum deles é essencial a bancos, telefonia, autenticação ou Play Services.
 * Desinstalação de fábrica é permitida somente para os nomes desta lista e SafeCatalog.
 */
object FactoryExtras {
    private fun lower(name: String, label: String, explanation: String) =
        OptionalPackage(name, label, explanation, Risk.LOWER)
    private fun conditional(name: String, label: String, explanation: String) =
        OptionalPackage(name, label, explanation, Risk.CONDITIONAL)

    val entries: List<OptionalPackage> = listOf(
        lower("com.facebook.appmanager", "Facebook App Manager", "Gerenciador de atualizações e serviços pré-instalados do Facebook."),
        lower("com.facebook.katana", "Facebook", "Aplicativo do Facebook, caso não o utilize."),
        lower("com.facebook.services", "Facebook Services", "Serviços extras do Facebook pré-instalados."),
        lower("com.facebook.system", "Facebook System", "Integração pré-instalada do Facebook."),
        conditional("com.google.android.apps.tachyon", "Google Meet / Duo", "Remove o app antigo de videochamadas, caso esteja no sistema."),
        lower("com.microsoft.skydrive", "Microsoft OneDrive", "Remove o armazenamento OneDrive se você não usa a sincronização de fotos."),
        lower("com.opera.max.oem", "Opera Max OEM", "Remova apenas se não utiliza os recursos de economia ou gerenciamento de dados."),
        lower("com.samsung.android.kidsinstaller", "Instalador Samsung Kids", "Remove o instalador do ambiente infantil Samsung."),
        lower("com.sec.android.usermanual", "Manual do usuário Samsung", "Remove o atalho e o conteúdo do manual do fabricante."),
        conditional("com.google.android.youtube", "YouTube", "Remove o YouTube pré-instalado; reinstale pela loja se precisar."),
        conditional("com.google.android.gm", "Gmail", "Remove o Gmail; use só se não depende de notificações e contas no aplicativo."),
        conditional("com.google.android.apps.maps", "Google Maps", "Remove Mapas e navegação; pode afetar atalhos para mapas de outros apps."),
        conditional("com.google.android.googlequicksearchbox", "Google (pesquisa e assistente)", "Pode afetar pesquisa, comandos de voz e Assistente."),
        conditional("com.samsung.android.video", "Reprodutor de vídeo Samsung", "Pode alterar opções de reprodução de vídeos locais."),
        conditional("com.hiya.star", "Identificador de chamadas Hiya", "Pode remover identificação de chamadas e filtro antispam."),
        conditional("com.samsung.android.game.gos", "Game Optimizing Service", "Pode alterar gerenciamento de desempenho, bateria e temperatura em jogos.")
    )
}
