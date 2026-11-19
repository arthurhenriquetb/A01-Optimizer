# A01 Optimizer 0.4 — experimental

Aplicativo criado para o **Samsung Galaxy A01 SM-A015M/DS (Android 12)**. Usa depuração sem fio com pareamento ADB local; **não precisa de root, Shizuku ou computador**. Nenhum comando é executado sem seleção e confirmação.

## Funções

1. Diagnóstico de aplicativos do sistema.
2. Desativar, com possibilidade de reativar, uma lista controlada de 35 componentes opcionais.
3. Reativar o modo de emergência Samsung se estiver desativado.
4. **Desinstalar aplicativos instalados pelo usuário**, com pesquisa e seleção manual. Isso pode apagar os dados desses aplicativos; para recuperá-los normalmente é necessário reinstalá-los. O app não promete restauração desses dados.
5. **Remover aplicativos opcionais de fábrica apenas do usuário Android 0**, por `pm uninstall -k --user 0`. O arquivo de sistema não é excluído e a remoção pode ser revertida.
6. **Restaurar aplicativos de fábrica que a versão 0.4 removeu**, com `cmd package install-existing --user 0`. O app procura preservar também o estado de desativação anterior.
7. Exibir nomes amigáveis de apps, identificadores e avisos de risco, inclusive pesquisa na lista.

A remoção de apps de fábrica só é oferecida para a lista fechada de **51 pacotes opcionais** (`SafeCatalog` + `FactoryExtras`), validados contra o inventário enviado pelo usuário. Não existe campo para digitar comandos arbitrários. Apps de bancos conhecidos, a própria família A01 Optimizer e serviços essenciais não são candidatos à remoção de fábrica.

## Como usar no celular

1. Conecte-se ao Wi-Fi privado de confiança.
2. Abra o A01 Optimizer 0.4, toque em **Preparar notificação de pareamento**.
3. Em **Configurações → Opções do desenvolvedor → Depuração sem fio → Parear dispositivo com código**, responda à notificação com **PORTA CÓDIGO**, separados por um espaço.
4. Na tela principal de depuração sem fio, copie para o app só a **porta de conexão**, após os dois-pontos (não é a porta de pareamento).
5. Toque **Conectar e analisar**.
6. Para desinstalar, toque em **Abrir gerenciador de desinstalação → Atualizar lista**.
7. Pesquise o nome de um app, selecione apenas o que conhece e confira o aviso antes de confirmar.
8. Para reverter remoções de fábrica da 0.4, use **Restaurar apps de fábrica removidos por esta versão**.
9. Teste seus bancos (Nubank, Banco do Brasil, PicPay, PagBank, Inter e Banestes), chamadas, Wi-Fi, SMS, biometria e Pix após alterar apps. Desligue a depuração sem fio ao terminar.

## Segurança e limitações

- **Apps comuns**: a desinstalação pode apagar dados e não há restauração automática.
- **Apps de fábrica**: apenas removidos para o usuário 0, sem apagar a partição do sistema. Isso não garante grande ganho de RAM, espaço ou velocidade.
- Banco e apps essenciais são protegidos por uma lista de bloqueio e por uma lista restrita de apps de fábrica. A identificação de apps bancários não é infalível, então revise as escolhas.
- Não toca em root, bootloader, Play Integrity, Google Play Services, Knox, autenticação, rádio, telefonia ou biometria.
- Mantenha as versões antigas instaladas até restaurar seus históricos, principalmente a v0.1.1 e v0.3.1. A v0.4 é um novo pacote Android, pois o GitHub Actions gera assinaturas de depuração novas em cada compilação. Requer novo pareamento.
- Faça backup antes de desinstalar. **Não há garantia de compatibilidade bancária ou melhoria de desempenho.**
- Não foi testado fisicamente no SM-A015M; experimente com um único app opcional de cada vez.

## Código e compilação

Projeto Kotlin/Android com a biblioteca [Kadb](https://github.com/flyfishxu/Kadb), licença Apache-2.0. GitHub Actions publica o APK de teste como artefato.