# A01 Optimizer v0.3 — experimental

Esta versão foi construída a partir do inventário de **297 pacotes do SM-A015M, Android 12** enviado pelo proprietário. É um app independente, que executa comandos ADB via Wi-Fi com pareamento de seis dígitos. **Sem root, sem Shizuku, sem computador.**

A versão 0.3 **não desativa nada automaticamente**. Mostra uma lista fechada de 35 pacotes opcionais presentes no inventário do aparelho; muitos têm efeitos pequenos ou nulos sobre RAM e desempenho. Explica efeitos e riscos, exige seleção e confirmação e oferece restauração do que esta própria versão desativou.

## Antes de usar

- Conecte-se ao Wi-Fi privado de confiança.
- No app, toque em preparar notificação e abra Configurações → Opções do desenvolvedor → Depuração sem fio → Parear dispositivo com código.
- Responda à notificação com **porta de pareamento** + espaço + **código de seis dígitos**.
- Na tela inicial de Depuração sem fio, informe no app a **porta de conexão** (número após o sinal de dois-pontos).
- Toque em **Conectar e analisar**.
- Revise as caixas e, se quiser, use **Marcar sugestões de menor risco**. Leia cada descrição; não use a opção por impulso.
- Toque em **Revisar e desativar selecionados**, confirme e teste os recursos do telefone.
- Caso precise, toque em **Restaurar alterações** para reativar pacotes que a versão 0.3 registrou.
- A opção **Reativar Modo de emergência** tenta recuperar `com.sec.android.emergencylauncher`, já desativado no relatório.
- Desligue depuração sem fio ao terminar.

## Segurança e limitações

- Sem alterações no bootloader, root, Play Integrity, Google Play Services, Knox, autenticação, serviços de rede, chamadas ou bancos.
- Desativação reversível com `pm disable-user --user 0`; nunca exclui dados de aplicativos.
- Lista fechada validada contra inventário, sem campo para comandos ADB arbitrários.
- Pode reduzir funcionalidades; não garante ganho de desempenho nem compatibilidade bancária.
- **Nunca desinstale os apps das versões antigas antes de restaurar o histórico que guardam**, especialmente A01 Optimizer 0.1.1.
- Cada versão é um pacote Android separado por conta das assinaturas debug geradas pelo GitHub Actions. Não atualiza as versões anteriores in-place.
- Projeto experimental sem testes automatizados no SM-A015M. Faça backup dos dados importantes antes de testar.

## Código-fonte e compilação
GitHub Actions gera um APK debug na aba Actions. Biblioteca [Kadb](https://github.com/flyfishxu/Kadb) sob licença Apache-2.0.
