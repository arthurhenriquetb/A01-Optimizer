# A01 Optimizer v0.5 — experimental

Aplicativo para o **Samsung Galaxy A01 SM-A015M/DS, Android 12**, sem root, Shizuku ou computador. Usa ADB por depuração sem fio, mediante pareamento e autorização manual.

## O que mudou

- **Inventário completo:** a tela de gerenciamento lista *todos os pacotes de sistema instalados para o usuário 0*, inclusive componentes protegidos (no relatório original eram 297). Os pacotes normais também são mostrados.
- **Pesquisa por nome ou identificador** em todas as categorias.
- **Verde — menor risco:** componentes opcionais que podem ser removidos manualmente.
- **Amarelo — avançado:** componentes que podem retirar funções úteis, como calendário, alarmes, serviços de voz e gerenciador de arquivos. Para marcar, o usuário precisa ativar o modo avançado e confirmar o alerta.
- **Vermelho — protegido:** todos os demais pacotes aparecem com explicação de proteção. São somente informativos, nunca selecionáveis para remoção. A lista de vermelhos começa recolhida para não sobrecarregar o Galaxy A01; o usuário pode expandi-la ou encontrar qualquer item pela busca.
- Catálogo de fábrica expandido de **51 para 61 opcionais** (35 em `SafeCatalog`, 16 em `FactoryExtras`, 10 em `AdvancedCatalog`). As opções de desativação reversível na tela principal continuam limitadas a 35.
- Bancos conhecidos e todas as versões do A01 Optimizer são protegidos na lista de aplicativos comuns.

## Como usar

1. Conecte o Galaxy A01 a um Wi-Fi privado e confiável.
2. No A01 Optimizer 0.5, prepare a notificação de pareamento e vá a **Configurações → Opções do desenvolvedor → Depuração sem fio → Parear dispositivo com código**.
3. Responda à notificação com **PORTA CÓDIGO**, separados por espaço.
4. Na tela Depuração sem fio, veja a **porta de conexão** após o IP (não a porta de pareamento). Informe no app.
5. Toque **Conectar e analisar**, depois **Abrir gerenciador completo de aplicativos → Atualizar lista**.
6. Pesquise um app pelo nome ou pacote. Para ver todos os protegidos, toque **Mostrar componentes protegidos**.
7. Para permitir selecionar itens amarelos, toque **Ativar modo avançado**, leia o aviso e confirme.
8. Selecione somente o que não usa; revise a lista no diálogo final e confirme.
9. Para reverter apps de fábrica removidos pela v0.5, use **Restaurar apps de fábrica removidos por esta versão**.
10. Teste Nubank, Banco do Brasil, PicPay, PagBank, Inter, Banestes e demais apps bancários, além de chamadas, SMS, Wi-Fi, biometria, Pix e anexos. Desative a depuração sem fio quando terminar.

## O que cada remoção faz

- **App instalado pelo usuário:** `pm uninstall --user 0 <pacote>`. Pode apagar dados, conversas e preferências. Não há botão de restauração para apps comuns.
- **App de fábrica opcional:** `pm uninstall -k --user 0 <pacote>`. Remove o app somente do usuário principal, preservando os arquivos originais do sistema. A v0.5 registra o pacote e tenta restaurá-lo com `cmd package install-existing --user 0 <pacote>`, preservando seu estado anterior de desativação quando possível.
- **App vermelho:** nenhuma remoção permitida pela ferramenta. Ter acesso ADB a um pacote não significa que seja seguro desinstalá-lo.

## Segurança e limites

- Sem remoção automática, sem comandos arbitrários e com confirmação manual para cada lote.
- Ainda não há garantia de compatibilidade de todos os bancos nem de ganho de desempenho. As classificações são conservadoras e não equivalem a testes práticos no SM-A015M.
- O bloqueio de bancos usa nomes conhecidos e heurísticas, podendo não reconhecer aplicativos pouco conhecidos. Revise sempre.
- `QUERY_ALL_PACKAGES` permite exibir nomes de apps instalados localmente. O inventário não é enviado a servidores.
- **A versão 0.5 é um aplicativo separado** da 0.4, porque as builds do GitHub Actions têm assinatura de debug diferente. Faça novo pareamento. Guarde 0.4, 0.3.1 e 0.1.1 enquanto precisar dos respectivos históricos de restauração.
- Faça backup antes de qualquer desinstalação. Remover app de fábrica sem root não libera o espaço da partição original do sistema.

## Compilação

Código em Kotlin. GitHub Actions gera o APK experimental usando a biblioteca [Kadb](https://github.com/flyfishxu/Kadb), licença Apache-2.0.