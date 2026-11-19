package br.com.a01optimizer

enum class Risk { LOWER, CONDITIONAL }

data class OptionalPackage(
    val name: String,
    val label: String,
    val explanation: String,
    val risk: Risk
)

/** Lista EXATA. Apenas nomes validados no relatório do Galaxy A01 SM-A015M Android 12.
 * Nenhum serviço bancário, Google Play, telefônico, localização, Knox, biometria ou atualização.
 */
object SafeCatalog {
    private fun lower(name: String, label: String, info: String) =
        OptionalPackage(name, label, info, Risk.LOWER)
    private fun conditional(name: String, label: String, info: String) =
        OptionalPackage(name, label, info, Risk.CONDITIONAL)

    val entries: List<OptionalPackage> = listOf(
        lower("com.android.dreams.basic", "Protetor de tela básico", "Remove animação de descanso do Android."),
        lower("com.android.dreams.phototable", "Protetor de tela com fotos", "Remove exibição de fotos quando em repouso."),
        lower("com.android.egg", "Surpresa do Android", "Apenas o Easter Egg da versão Android."),
        lower("com.samsung.android.app.settings.bixby", "Configurações Bixby", "Dispensável se você não usa Bixby."),
        lower("com.samsung.android.app.watchmanagerstub", "Atalho Galaxy Watch", "Dispensável se não usa relógio Samsung."),
        lower("com.samsung.android.dynamiclock", "Tela de bloqueio dinâmica", "Dispensável se não usa papéis de parede dinâmicos."),
        lower("com.samsung.android.stickercenter", "Figurinhas Samsung", "Desativa funções de figurinhas e adesivos."),
        lower("com.samsung.storyservice", "Histórias da Galeria", "Desativa histórias automáticas da Galeria."),
        lower("com.samsung.android.app.appsedge", "Apps Edge", "Dispensável se não usa o painel lateral de aplicativos."),
        lower("com.samsung.android.app.clipboardedge", "Área de transferência Edge", "Dispensável se não usa o painel lateral de cópia."),
        lower("com.samsung.android.app.taskedge", "Tarefas Edge", "Dispensável se não usa atalhos laterais."),
        lower("com.samsung.android.service.peoplestripe", "Contatos Edge", "Dispensável se não usa o painel de contatos."),
        lower("com.samsung.android.app.clockpack", "Relógios da tela de bloqueio", "Pode eliminar estilos alternativos de relógio."),
        lower("com.sec.android.app.fm", "Rádio FM", "Só marque se não escuta rádio FM pelo telefone."),
        lower("com.google.android.printservice.recommendation", "Sugestões de impressoras", "Não afeta impressão já configurada, mas retira sugestões."),
        conditional("com.android.bips", "Serviço de impressão Android", "Pode impedir impressão pelo telefone."),
        conditional("com.google.android.projection.gearhead", "Android Auto", "Pode impedir a conexão do Android Auto ao carro."),
        conditional("com.samsung.android.app.galaxyfinder", "Busca Finder Samsung", "Pode remover a pesquisa de aplicativos na tela inicial."),
        conditional("com.samsung.android.app.cocktailbarservice", "Serviço de painéis Edge", "Desativa os painéis Edge; pode afetar atalhos laterais."),
        conditional("com.samsung.android.forest", "Bem-estar digital", "Pode afetar controle de tempo de tela e controle dos pais."),
        conditional("com.samsung.android.themestore", "Loja de temas Samsung", "Pode impedir baixar e mudar temas pela Galaxy Themes."),
        conditional("com.samsung.android.themecenter", "Serviço de temas Samsung", "Pode prejudicar temas e personalização; evite se usa."),
        conditional("com.samsung.android.app.dressroom", "Papéis de parede e estilos", "Pode limitar personalização visual."),
        conditional("com.samsung.android.scloud", "Samsung Cloud", "Pode interromper cópias e sincronização da conta Samsung."),
        conditional("com.samsung.android.rubin.app", "Personalização Samsung", "Desativa sugestões e personalização de uso."),
        conditional("com.samsung.android.mdx.kit", "Integração Windows", "Pode prejudicar Link para Windows."),
        conditional("com.samsung.android.mdx.quickboard", "Área de transferência Windows", "Pode prejudicar sincronização com computador."),
        conditional("com.samsung.android.app.sharelive", "Compartilhamento rápido", "Pode impedir Quick Share."),
        conditional("com.samsung.android.allshare.service.fileshare", "Compartilhar arquivos Samsung", "Pode remover funções de compartilhamento."),
        conditional("com.samsung.android.aware.service", "Compartilhamento por proximidade", "Pode prejudicar detecção de aparelhos próximos."),
        conditional("com.samsung.android.easysetup", "Configuração de acessórios", "Pode interferir na configuração de acessórios Samsung."),
        conditional("com.sec.android.daemonapp", "Serviço de clima Samsung", "Pode interromper o widget de clima."),
        conditional("com.sec.android.easyMover.Agent", "Smart Switch (agente)", "Pode afetar transferência de dados ao trocar de aparelho."),
        conditional("com.samsung.android.smartswitchassistant", "Smart Switch (assistente)", "Pode afetar transferência de dados ao trocar de aparelho."),
        conditional("com.samsung.android.uds", "Economia extrema de dados", "Pode desativar opções do modo de economia de dados.")
    )
    fun allowed(name: String): Boolean = entries.any { it.name == name }
}
