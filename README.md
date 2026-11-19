# A01 Optimizer v0.6 — experimental

Aplicativo para o Galaxy A01 SM-A015M/DS, Android 12. Usa ADB sem fio após o pareamento manual; **não precisa de root, Shizuku ou computador**.

## Novidades

- **Tela inicial é o gerenciador completo de aplicativos.** O usuário pode parear e informar a porta ADB na própria tela.
- **TODOS os pacotes de sistema instalados** são mostrados, sem esconder os vermelhos. Inclui o pacote base `android` e os pacotes de sobreposição de interface.
- **Subcategoria Samsung** em separado, seguida de Android/Google e Outros/Operadora.
- **Nomes conhecidos** apresentados em português, sempre com o identificador real abaixo. Pacotes não reconhecidos usam o nome obtido do Android ou o próprio identificador.
- **Ordenar por nome, tamanho ou RAM atual**, em cada grupo. Tamanho corresponde ao aplicativo + dados disponíveis via `StorageStatsManager`; é necessário conceder manualmente **Acesso a dados de uso** nas Configurações e tocar em **Calcular tamanhos**. Não confundir tamanho informado com espaço liberado pela remoção para usuário 0.
- **RAM** usa uma leitura momentânea do PSS dos processos ativos via `dumpsys meminfo`. Um traço indica ausência de medição, não consumo zero. Tocar em **Atualizar RAM agora** para nova leitura.
- **Verde:** opcionais selecionáveis. **Amarelo:** requer ativar o modo avançado. **Vermelho:** remoção manual de alto risco, após desbloquear o modo especializado e digitar o identificador completo de UM pacote por vez. Sem seleção automática.
- **Componentes absolutamente críticos** (Android base, SystemUI, ADB, Play Services, WebView, telefonia, biometria e outros) continuam visíveis, mas bloqueados. O objetivo é preservar a possibilidade de recuperar e usar o telefone, especialmente bancos.
- **Sugestões não garantidas** de candidatos vermelhos aparecem com a etiqueta **POSSÍVEL CANDIDATO**, quando há uma hipótese plausível de dispensabilidade caso o recurso não seja usado.
- Rodapé: **Desenvolvido por Arthur Henrique, em 2026**.
- A tela antiga de diagnóstico, desativação reversível e modo de emergência continua disponível pelo botão **Abrir diagnóstico e desativação reversível**.

## Como usar

1. Abra o A01 Optimizer 0.6 e conecte o telefone a um Wi-Fi privado.
2. Na tela inicial, toque em **Preparar notificação de pareamento**, depois em **Abrir Opções do desenvolvedor**.
3. Nas configurações do Android, abra **Depuração sem fio → Parear dispositivo com código** e responda à notificação com **PORTA CÓDIGO**, separados por espaço.
4. Na página principal de depuração sem fio, copie somente a **porta de conexão** depois do IP, diferente da porta de pareamento. Informe no campo do app.
5. Toque em **Atualizar lista de aplicativos**. O sistema vai listar aplicativos comuns, aplicativos de fábrica, nomes conhecidos, suas seções Samsung/Google/Outros e a memória RAM atual disponível.
6. Use a pesquisa ou o seletor **Ordenar por** (nome, tamanho ou RAM).
7. Para ver tamanhos completos: toque **Permitir acesso aos tamanhos**, permita **A01 Optimizer 0.6** em Acesso a dados de uso e volte. Depois toque **Calcular tamanhos**. Essa etapa pode demorar.
8. Para remover amarelos, ative modo avançado. Para vermelhos não críticos, leia o alerta e desbloqueie a remoção de alto risco. Pacotes vermelhos exigem confirmação exata do nome e só podem ser removidos individualmente.
9. Os pacotes de fábrica são removidos **somente para o usuário 0**, com `pm uninstall -k --user 0` e registro para restauração via `cmd package install-existing --user 0`. Isso só funcionará se Android e ADB permanecerem acessíveis.
10. Apps comuns removidos com `pm uninstall --user 0` podem ter os dados apagados; não há restauração deles no app. Apps bancários conhecidos e a própria família A01 Optimizer continuam protegidos na seção de apps comuns.
11. Teste bancos (Nubank, Banco do Brasil, PicPay, PagBank, Inter, Banestes), Wi-Fi, chamadas, SMS, Pix e biometria depois das mudanças. Desative a depuração sem fio ao terminar.

## Limites e riscos

- **NENHUM pacote vermelho pode ser considerado seguro só por aparecer na lista.** O catálogo de sugestões é hipotético e deve ser avaliado caso a caso.
- Desinstalar um pacote fundamental pode interromper a inicialização do Android ou o próprio ADB. Se isso acontecer, a restauração via app deixa de ser possível, podendo exigir recuperação por firmware oficial.
- Sem root não é possível apagar a partição original de sistema. `pm uninstall -k --user 0` apenas remove o app do usuário atual; os ganhos reais de armazenamento e RAM variam.
- O status **verde/amarelo/vermelho não substitui testes** no SM-A015M. Nenhuma compatibilidade bancária ou melhoria de velocidade está garantida.
- Como a build debug do GitHub Actions muda de assinatura, a 0.6 usa um novo identificador. **Mantenha as versões antigas** até restaurar os históricos de alterações feitos nelas. O pareamento deve ser refeito.
- Faça backup antes de remover qualquer app. Não há remoção automática, nem campo para comandos ADB arbitrários.
- Toda análise de memória e armazenamento é local; o inventário não é enviado para servidores.

## Compilação

Projeto Kotlin/Android com biblioteca [Kadb](https://github.com/flyfishxu/Kadb) (Apache-2.0). GitHub Actions gera o APK experimental.