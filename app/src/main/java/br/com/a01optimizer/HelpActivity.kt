package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.webkit.WebView
import android.graphics.Color

/** All connection instructions live here instead of cluttering the main screen. */
class HelpActivity : Activity() {
    private val pt get() = getSharedPreferences("a01", MODE_PRIVATE)
        .getString("language", "en") == "pt"
    private fun tr(en: String, br: String) = if (pt) br else en
    private lateinit var browser: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(12, 8, 12, 8)
        }
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun action(text: String, handler: () -> Unit): Button = Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 12f
            setOnClickListener { handler() }
        }
        buttons.addView(action(tr("Back", "Voltar")) { finish() },
            LinearLayout.LayoutParams(0, -2, 1f))
        buttons.addView(action(tr("Save PDF", "Salvar PDF")) { printGuide() },
            LinearLayout.LayoutParams(0, -2, 1f))
        buttons.addView(action(tr("Share", "Compartilhar")) { shareGuide() },
            LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(buttons)
        browser = WebView(this)
        browser.settings.javaScriptEnabled = false
        browser.loadDataWithBaseURL(null, html(), "text/html", "UTF-8", null)
        root.addView(browser, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun guide(): String = if (pt) """
DEBLOATOAST EXPERT v0.11 — AJUDA

ANTES DE COMEÇAR
Você pode consultar aplicativos e desinstalar apps comuns sem ADB. Para remover aplicativos de fábrica, precisa autorizar a conexão ADB. Não remova pacotes críticos sem entender os riscos: o telefone pode parar de iniciar, perder funções e dados, e não ser possível recuperá-lo.

1. ATIVE AS OPÇÕES DO DESENVOLVEDOR
Abra Configurações do Android. Entre em Sobre o telefone (em alguns modelos: Sistema → Sobre o telefone). Procure Número da versão ou Número da compilação. Toque sete vezes; se solicitado, digite o PIN. Volte às Configurações e procure Opções do desenvolvedor, geralmente em Sistema ou Configurações adicionais. Os nomes variam conforme o fabricante.

2. ANDROID 11 OU MAIS RECENTE — PAREAMENTO SEM COMPUTADOR
Conecte-se a uma rede Wi-Fi confiável. Em Opções do desenvolvedor, ative Depuração sem fio e entre nessa opção. No DebloaToast, abra Conectar e toque Parear. Isso abre as configurações do Android e cria uma notificação do DebloaToast.
Na tela Depuração sem fio, toque Parear dispositivo com código de pareamento. O Android mostrará um código de seis dígitos e uma porta de pareamento temporária. Desça a barra de notificações e responda à notificação do DebloaToast com o código de seis dígitos. Se a descoberta automática da porta falhar, responda com PORTA CÓDIGO, separados por espaço, por exemplo 38541 123456.
Depois do pareamento, aparecerá outra notificação solicitando a PORTA DE CONEXÃO. Volte à tela principal Depuração sem fio e localize Endereço IP e porta, como 192.168.1.5:37123. Responda somente 37123, o número após os dois-pontos. Essa porta NÃO é a porta de pareamento.
Toque na notificação Pronto para conectar. Ela abrirá o DebloaToast na aba Conectar. Pressione o botão vermelho CONECTAR. Se o botão estiver inativo, confira se a segunda notificação foi respondida e tente novamente. Se a conexão falhar, confira se a depuração sem fio continua ativada; a porta pode mudar depois de reiniciar o telefone. A opção Buscar conexão automaticamente também pode ajudar.

3. ANDROID 10 — CONFIGURAÇÃO COM WINDOWS
Em Opções do desenvolvedor, ative Depuração USB. Abra Conectar no DebloaToast e escolha Configurar com Windows. O assistente oferece o arquivo DebloaToast-Windows.zip. Baixe no telefone, transfira para um computador Windows e extraia o ZIP. Conecte o telefone ao computador com cabo USB de dados. Execute Conectar-Windows.cmd no computador; ele baixa as ferramentas oficiais ADB do Google e prepara a conexão. Autorize a depuração USB na tela do telefone. Quando o assistente indicar que está pronto, volte ao DebloaToast e toque CONECTAR. Depois de estabelecida a conexão Wi-Fi, você pode retirar o cabo. Após reiniciar, talvez precise repetir o processo. Use somente redes confiáveis.

4. USO E RESTAURAÇÃO
Em Apps, consulte os aplicativos e selecione o que deseja remover. As cores verde, amarela, vermelha e preta indicam níveis estimados de risco; PRETO é crítico. A versão Expert permite tentar remover componentes essenciais. A confirmação final não garante segurança. Faça backup antes de qualquer remoção de sistema. Em Conectar → Restaurar apps de fábrica, você pode tentar restaurar pacotes removidos pelo DebloaToast, mas a restauração depende de o Android continuar funcionando e pode falhar.

5. PROBLEMAS COMUNS
Sem notificação: verifique as notificações do aplicativo e procure a notificação do DebloaToast na barra do Android.
Código recusado: abra novamente Parear dispositivo com código de pareamento e use o novo código de seis dígitos.
Porta recusada: não confunda a porta temporária de pareamento com a porta de conexão mostrada na tela principal Depuração sem fio.
Não conecta: mantenha o Wi-Fi e a depuração sem fio ativos. Confira a porta atual. Depois de reiniciar o telefone, repita o pareamento ou a configuração do Windows se necessário.
Para salvar este tutorial, toque Salvar PDF e escolha Salvar como PDF na janela de impressão do Android.
""" else """
DEBLOATOAST EXPERT v0.11 — HELP

BEFORE YOU START
You can browse apps and uninstall ordinary user apps without ADB. Managing factory apps requires an authorized ADB connection. Removing critical packages can prevent Android from starting, disable features, cause data loss, and make recovery impossible.

1. ENABLE DEVELOPER OPTIONS
Open Android Settings → About phone (sometimes System → About phone). Find Build number and tap it seven times. Enter your PIN if requested. Return to Settings and open Developer options, usually under System or Additional settings. Menu names differ between manufacturers.

2. ANDROID 11 OR LATER — PAIR WITHOUT A COMPUTER
Join a trusted Wi-Fi network. In Developer options, turn on Wireless debugging and open its settings. In DebloaToast, open Connect and tap Pair. This opens Android settings and creates a DebloaToast notification.
Tap Pair device with pairing code in Wireless debugging. Android shows a temporary six-digit code and pairing port. Pull down notifications and reply to the DebloaToast notification with the six-digit code. If automatic pairing-port discovery fails, reply with PORT CODE separated by a space, for example 38541 123456.
After pairing, a second notification asks for the CONNECTION PORT. Return to the main Wireless debugging screen and find IP address & Port, for example 192.168.1.5:37123. Reply with just 37123, the number after the colon. This is NOT the temporary pairing port.
Tap the Ready to connect notification to open DebloaToast directly in Connect. Press the large red CONNECT button. If it is disabled, check that you replied to the second notification. If connection fails, verify Wireless debugging is still enabled; its port can change after restarting. Find connection automatically may also help.

3. ANDROID 10 — WINDOWS SETUP
Enable USB debugging under Developer options. Open Connect → Set up with Windows in DebloaToast. Download DebloaToast-Windows.zip to your phone, transfer it to a Windows computer and extract it. Connect the phone using a USB data cable. Run Conectar-Windows.cmd on the computer; it downloads Google's official ADB tools and prepares the connection. Approve USB debugging on your phone. When the helper reports Ready, return to DebloaToast and press CONNECT. Once the Wi-Fi connection works, unplug the USB cable. You may need to repeat setup after a restart. Use only trusted networks.

4. REMOVAL AND RESTORATION
Open Apps and select packages for removal. Green, yellow, red and black indicate estimated risk; BLACK is critical. Expert can attempt to remove essential Android packages. The final confirmation does not make the operation safe. Back up important data. Connect → Restore removed system apps may restore packages previously removed by DebloaToast, but restoration depends on Android still working and is not guaranteed.

5. TROUBLESHOOTING
No notification: check DebloaToast notification permissions and the Android notification shade.
Pairing code rejected: open Pair device with pairing code again and use the new six-digit code.
Wrong port: distinguish the temporary pairing port from the connection port on the main Wireless debugging screen.
Connection fails: keep Wi-Fi and Wireless debugging enabled; check the current connection port. After a restart, you may need to pair again or rerun the Windows helper.
To save this guide, tap Save PDF and choose Save as PDF in Android's print dialog.
"""

    private fun html(): String {
        val escaped = guide().replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\n", "<br>")
        return """<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1">
        <style>body{font-family:sans-serif;color:#24343d;padding:12px;line-height:1.55;font-size:15px}
        h1{color:#196a78} @media print{body{font-size:11pt}}</style></head>
        <body><h1>DebloaToast</h1><p>$escaped</p></body></html>"""
    }

    private fun printGuide() {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print("DebloaToast-Guide",
            browser.createPrintDocumentAdapter("DebloaToast-Guide"),
            PrintAttributes.Builder().build())
    }

    private fun shareGuide() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DebloaToast Expert v0.11 — Guide")
            putExtra(Intent.EXTRA_TEXT, guide())
        }
        startActivity(Intent.createChooser(send, tr("Share guide", "Compartilhar tutorial")))
    }

    override fun onDestroy() {
        browser.destroy()
        super.onDestroy()
    }
}
