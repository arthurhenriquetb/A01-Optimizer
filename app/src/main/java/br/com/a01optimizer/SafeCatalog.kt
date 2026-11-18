package br.com.a01optimizer

data class OptionalPackage(val name: String, val label: String, val explanation: String)

/** Lista fechada. Nunca incluir Android, Google, Knox, bancos ou autenticação. */
object SafeCatalog {
    val entries = listOf(
        OptionalPackage("com.samsung.android.app.tips", "Dicas da Samsung", "Tutoriais do aparelho"),
        OptionalPackage("com.samsung.android.voc", "Samsung Members", "Suporte e diagnósticos opcionais"),
        OptionalPackage("com.samsung.android.app.spage", "Samsung Free", "Painel de notícias"),
        OptionalPackage("com.samsung.android.game.gamehome", "Game Launcher", "Catálogo de jogos"),
        OptionalPackage("com.facebook.appmanager", "Facebook App Manager", "Serviço auxiliar do Facebook"),
        OptionalPackage("com.facebook.services", "Facebook Services", "Serviço auxiliar do Facebook"),
        OptionalPackage("com.facebook.system", "Facebook System", "Serviço auxiliar do Facebook"),
        OptionalPackage("com.microsoft.skydrive", "OneDrive", "Sincronização Microsoft; não desative se utiliza"),
        OptionalPackage("com.samsung.android.kidsinstaller", "Samsung Kids", "Instalador do modo infantil")
    )
    fun allowed(name: String): Boolean = entries.any { it.name == name }
}
