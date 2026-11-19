package br.com.a01optimizer

/** Opções adicionais identificadas no inventário do SM-A015M.
 * São selecionáveis somente depois de habilitar o modo avançado.
 * A classificação considera a função aparente do pacote, não um teste no aparelho.
 */
object AdvancedCatalog {
    private fun lower(name: String, label: String, explanation: String) =
        OptionalPackage(name, label, explanation, Risk.LOWER)
    private fun conditional(name: String, label: String, explanation: String) =
        OptionalPackage(name, label, explanation, Risk.CONDITIONAL)

    val entries = listOf(
        lower("com.android.traceur", "Rastreamento do Android", "Ferramenta de registro de desempenho para diagnóstico; não remova se usa rastreamentos do sistema."),
        lower("com.google.android.feedback", "Feedback do Google", "Remove a interface de envio de relatórios e comentários de aplicativos Google."),
        conditional("com.google.android.tts", "Leitura de texto Google", "Pode prejudicar acessibilidade, voz e leitura de textos por aplicativos."),
        conditional("com.samsung.android.calendar", "Calendário Samsung", "Remove o aplicativo de calendário da Samsung; confira lembretes e eventos antes."),
        conditional("com.samsung.android.bluelightfilter", "Filtro de luz azul", "Pode retirar o filtro de luz azul e opções de conforto visual."),
        conditional("com.sec.android.app.myfiles", "Meus Arquivos Samsung", "Remove o gerenciador Samsung; pode afetar tarefas com arquivos, downloads e anexos."),
        conditional("com.sec.android.app.clockpackage", "Relógio Samsung", "Pode eliminar alarmes, temporizador e cronômetro. Não remova se depende de alarmes."),
        conditional("com.sec.android.app.soundalive", "Efeitos de áudio Samsung", "Pode retirar equalização e efeitos de áudio de alguns aplicativos."),
        conditional("com.google.android.syncadapters.calendar", "Sincronização do Google Agenda", "Pode interromper sincronização de compromissos e calendários Google."),
        conditional("com.samsung.android.beaconmanager", "Dispositivos próximos Samsung", "Pode afetar detecção de acessórios e serviços de proximidade Samsung.")
    )
}
