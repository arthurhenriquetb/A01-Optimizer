# A01 Optimizer — experimental

Aplicativo Android para Samsung Galaxy A01 (SM-A015M/DS) com Android 12, desenvolvido para desativar **somente** alguns aplicativos opcionais por ADB sem fio, sem Shizuku, root ou computador durante o uso.

**Aviso:** projeto experimental. O APK compilado ainda não foi testado em um aparelho real. Nenhum desempenho ou funcionamento de aplicativo bancário é garantido.

## Utilização
1. Conecte o celular a uma rede Wi-Fi confiável.
2. No aplicativo, toque em **Preparar notificação de pareamento**.
3. Abra **Opções do desenvolvedor > Depuração sem fio > Parear dispositivo com código de pareamento**.
4. Puxe a barra de notificações e responda à notificação do aplicativo com a **porta de pareamento**, um espaço e o **código de seis números** (ex.: `37121 123456`).
5. Ao receber a confirmação, volte à tela principal de depuração sem fio e encontre **Endereço IP e porta**. No aplicativo, digite **somente a porta de conexão** e toque em **Conectar e analisar**.
6. Selecione os aplicativos opcionais que conhece e não usa. Leia o aviso e confirme a desativação.
7. Teste telefone, chamadas, internet, Nubank, BB, PicPay, PagBank, Inter, Banestes e outros serviços.
8. Se houver problemas, **Restaurar alterações** reativa os pacotes que o otimizador registrou. Pode ser necessário reconectar a depuração sem fio para restaurar.
9. Desligue a depuração sem fio após o uso.

## Segurança
- Lista fechada de pacotes não essenciais: não inclui pacotes bancários, Google, Knox ou autenticação.
- Não executa comandos ADB inseridos por usuários.
- Confirmação explícita antes de alterar o estado de qualquer pacote.
- Histórico local para reversão, sem excluir dados nem fazer root.
- Credencial ADB privada fica no armazenamento interno do aplicativo, sem backup.
- **Atenção:** a depuração sem fio deve ser desligada após as operações, sobretudo fora de uma rede confiável.

## Compilação
O GitHub Actions compila `app-debug.apk` automaticamente a cada push na branch `main`; veja a aba **Actions** e baixe o artefato `A01-Optimizer-APK-experimental` no fluxo concluído. Ele contém o APK dentro de um ZIP.

Dependência: [Kadb](https://github.com/flyfishxu/Kadb) (Apache-2.0). O código do aplicativo é experimental e deve ser validado no aparelho alvo antes de uso importante.
