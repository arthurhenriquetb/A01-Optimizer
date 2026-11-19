# A01 Diagnóstico — v0.2 experimental

Versão de diagnóstico para o Samsung Galaxy A01 SM-A015M com Android 12.
Analisa os pacotes **do sistema** pelo ADB sem fio, sem Shizuku nem root, e copia um inventário para análise manual.

**Não desativa nada.** Esta versão tem um `applicationId` diferente da versão 0.1.1, porque os APKs experimentais do GitHub usam certificados de depuração temporários diferentes e não conseguem atualizar uma instalação anterior. Ela pode ser instalada ao lado do A01 Optimizer 0.1.1.

**Não desinstale a versão 0.1.1 antes de restaurar o que ela modificou**, caso queira reverter alterações anteriores.

## Passos

1. Instale o APK A01 Diagnóstico.
2. Com o aparelho na rede Wi-Fi, prepare uma nova notificação de pareamento.
3. Abra Configurações → Opções do desenvolvedor → Depuração sem fio → Parear dispositivo com código de pareamento.
4. Responda à notificação com a porta de pareamento, espaço e código de seis dígitos, por exemplo `37121 123456`.
5. Volte a Depuração sem fio, copie apenas a **porta de conexão** exibida ao lado do endereço IP.
6. No app, digite-a em Porta de conexão, toque em Conectar e analisar e então em Copiar diagnóstico completo.
7. Cole o texto numa conversa para avaliar quais pacotes são desnecessários, sem alterar bancos, Google, autenticação ou sistema essencial.
8. Desligue a depuração sem fio quando terminar.

O app não envia dados para servidores. O relatório apenas é copiado para a área de transferência após um toque explícito. A lista de pacotes de sistema não inclui aplicativos instalados por você de forma comum (terceiros).

## Fonte da biblioteca

[Kadb](https://github.com/flyfishxu/Kadb) (Apache-2.0). Este APK é experimental e deve ser testado no dispositivo alvo.
