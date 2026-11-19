package br.com.a01optimizer

/** Sugestões conservadoras para aparelhos não Samsung.
 * Apenas pacotes realmente instalados aparecem; a existência e o impacto
 * dependem do modelo, da versão Android e do fabricante. Não são garantias.
 */
object UniversalCatalog {
    private fun conditional(id: String, label: String, info: String) =
        OptionalPackage(id, label, info, Risk.CONDITIONAL)

    val entries = listOf(
        conditional("com.motorola.help", "Ajuda do dispositivo Motorola",
            "Pode retirar a central de ajuda da Motorola. Verifique se não precisa dela."),
        conditional("com.motorola.moto", "Recursos Moto",
            "Pode desativar gestos, atalhos e personalizações Moto. Mantenha se utiliza essas funções."),
        conditional("com.motorola.fmplayer", "Rádio FM Motorola",
            "Pode remover o rádio FM. Mantenha se escuta rádio pelo aparelho."),
        conditional("com.motorola.timeweatherwidget", "Widget de horário e clima Motorola",
            "Pode retirar o widget de relógio e previsão do tempo da Motorola."),
        conditional("com.miui.weather2", "Clima Xiaomi",
            "Pode remover o aplicativo e widgets de previsão do tempo da Xiaomi."),
        conditional("com.miui.player", "Música Xiaomi",
            "Pode remover o aplicativo de música da Xiaomi e associações de reprodução."),
        conditional("com.miui.videoplayer", "Vídeo Xiaomi",
            "Pode retirar o reprodutor de vídeo da Xiaomi."),
        conditional("com.miui.bugreport", "Relatórios de erros Xiaomi",
            "Pode impedir a coleta ou o envio de relatórios de falhas pela Xiaomi.")
    )
}
