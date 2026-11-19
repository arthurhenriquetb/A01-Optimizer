package br.com.a01optimizer

/** Sugestões condicionais para pacotes que continuam no nível vermelho.
 * Não são autorizações nem garantias; dependências podem variar no firmware.
 */
object RiskSuggestions {
    val notes: Map<String, String> = mapOf(
        "com.android.wallpaper.livepicker" to "Candidato se você NÃO usa papéis de parede animados. Pode retirar a seleção desses temas.",
        "com.android.wallpaperbackup" to "Candidato se dispensa restaurar o papel de parede após backup/migração.",
        "com.android.bookmarkprovider" to "Candidato apenas se não usa integração de favoritos do navegador; efeito incerto em alguns apps.",
        "com.android.printspooler" to "Candidato se nunca imprime pelo celular. Pode eliminar impressão de documentos e comprovantes.",
        "com.google.android.gms.location.history" to "Candidato se não usa o histórico de localização; preserve serviços de localização principais.",
        "com.samsung.android.callbgprovider" to "Candidato se não usa fundos de chamada Samsung; pode alterar a tela de chamadas.",
        "com.samsung.android.app.soundpicker" to "Candidato se não personaliza sons; pode prejudicar a escolha de toques e notificações.",
        "com.samsung.android.secsoundpicker" to "Candidato se não personaliza toques; pode prejudicar seleção de arquivos de áudio.",
        "com.samsung.android.mapsagent" to "Candidato somente se dispensa integração de mapas Samsung; função exata depende do firmware.",
        "com.samsung.android.networkdiagnostic" to "Candidato se dispensa ferramentas Samsung de diagnóstico de rede; efeitos não validados.",
        "com.sec.android.app.billing" to "Candidato se não faz compras na Galaxy Store; pode afetar pagamentos internos Samsung.",
        "com.sec.android.app.quicktool" to "Candidato se não usa ferramentas rápidas Samsung; confirme primeiro o que deixa de funcionar."
    )
}
