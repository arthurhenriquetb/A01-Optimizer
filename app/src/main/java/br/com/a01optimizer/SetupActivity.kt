package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors

/** A short, in-app setup flow: no terminal, IP, or ADB commands for the user. */
class SetupActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("a01", MODE_PRIVATE) }
    private val pt get() = prefs.getString("language", "en") == "pt"
    private fun tr(en: String, ptBr: String) = if (pt) ptBr else en
    private fun Int.dp() = (this * resources.displayMetrics.density + 0.5f).toInt()
    private var step = 0
    private var checking = false
    private var lastError = ""
    private val helperUrl =
        "https://github.com/arthurhenriquetb/DebloaToast/releases/download/v0.11/DebloaToast-Windows.zip"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        step = savedInstanceState?.getInt("step") ?: 0
        if (Build.VERSION.SDK_INT >= 30) {
            finish()
            return
        }
        draw()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("step", step)
        super.onSaveInstanceState(outState)
    }

    private fun view(text: String, size: Float = 16f, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.rgb(31, 45, 54))
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 8.dp(), 0, 8.dp())
        }

    private fun action(text: String, primary: Boolean = false, onClick: () -> Unit) =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            setTextColor(if (primary) Color.WHITE else Color.rgb(31, 45, 54))
            if (primary) backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.rgb(25, 106, 120))
            setOnClickListener { onClick() }
        }

    private fun draw() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(250, 251, 253))
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
        }
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        body.addView(view(tr("Full access setup", "Configuração do acesso completo"), 23f, true))
        body.addView(view(tr("Motorola / Android 10", "Motorola / Android 10"), 13f))
        if (step > 0) body.addView(view(
            tr("Step $step of 3", "Etapa $step de 3"), 13f, true
        ))
        when (step) {
            0 -> {
                body.addView(view(tr(
                    "Your apps are already visible. To manage factory apps, you only need to prepare this phone once with a Windows computer and a USB cable.",
                    "Seus apps já estão disponíveis. Para gerenciar aplicativos de fábrica, basta preparar este celular uma vez com um computador Windows e um cabo USB."
                )))
                body.addView(view(tr(
                    "The app will guide you. No commands, ports, or technical settings to type.",
                    "O aplicativo guiará você. Não será necessário digitar comandos, portas ou configurações técnicas."
                ), 14f))
                body.addView(action(tr("Get started", "Começar"), true) {
                    step = 1; draw()
                })
                body.addView(action(tr("Continue without full access", "Continuar no modo básico")) {
                    finish()
                })
            }
            1 -> {
                body.addView(view(tr(
                    "Unlock the developer menu on this phone.",
                    "Libere o menu de desenvolvedor neste celular."
                ), 18f, true))
                body.addView(view(tr(
                    "Open About phone and tap Build number seven times. If the menu is already enabled, just continue.",
                    "Abra Sobre o telefone e toque sete vezes em Número da versão. Se o menu já estiver ativado, apenas continue."
                )))
                body.addView(action(tr("Open About phone", "Abrir Sobre o telefone"), true) {
                    openSystemSettings(Settings.ACTION_DEVICE_INFO_SETTINGS)
                })
                body.addView(action(tr("Already done — continue", "Já fiz — continuar")) {
                    step = 2; draw()
                })
            }
            2 -> {
                body.addView(view(tr(
                    "Turn on USB debugging.",
                    "Ative a depuração USB."
                ), 18f, true))
                body.addView(view(tr(
                    "In Developer options, turn on USB debugging and confirm the Android warning. Return to this app afterward.",
                    "Em Opções do desenvolvedor, ative Depuração USB e confirme o aviso do Android. Depois, volte ao aplicativo."
                )))
                body.addView(action(tr("Open Developer options", "Abrir Opções do desenvolvedor"), true) {
                    openSystemSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                })
                body.addView(action(tr("USB debugging is on", "Depuração USB ativada")) {
                    step = 3; draw()
                })
            }
            else -> {
                body.addView(view(tr(
                    "Connect to a Windows computer.",
                    "Conecte a um computador Windows."
                ), 18f, true))
                body.addView(view(tr(
                    "Use a USB data cable. On the computer, run the DebloaToast Windows helper. It installs the official Android tools and prepares the connection automatically.",
                    "Use um cabo USB de dados. No computador, execute o assistente Windows do DebloaToast. Ele instala as ferramentas oficiais e prepara a conexão automaticamente."
                )))
                body.addView(action(tr("Download Windows helper", "Baixar assistente Windows"), true) {
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(helperUrl))) }
                    catch (_: Exception) { shareHelper() }
                })
                body.addView(action(tr("Send link to computer", "Enviar link ao computador")) {
                    shareHelper()
                })
                body.addView(view(tr(
                    "When the phone asks to allow USB debugging, tap Allow. When the computer says Ready, return here.",
                    "Quando o celular pedir autorização para depuração USB, toque em Permitir. Quando o computador indicar Pronto, volte aqui."
                ), 14f))
                if (lastError.isNotBlank()) body.addView(view(lastError, 14f, true))
                body.addView(action(
                    if (checking) tr("Checking…", "Verificando…")
                    else tr("Check connection", "Verificar conexão"),
                    true
                ) { checkConnection() }.apply { isEnabled = !checking })
                body.addView(view(tr(
                    "After restarting your phone, you may need to run the Windows helper again. Keep wireless debugging enabled only while needed.",
                    "Depois de reiniciar o celular, pode ser necessário executar o assistente Windows novamente. Mantenha a conexão de depuração ativa apenas quando precisar."
                ), 12f))
            }
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        if (step > 0) bottom.addView(action(tr("Back", "Voltar")) {
            step--; lastError = ""; draw()
        }, LinearLayout.LayoutParams(0, 48.dp(), 1f))
        bottom.addView(action(tr("Close", "Fechar")) { finish() },
            LinearLayout.LayoutParams(0, 48.dp(), 1f))
        root.addView(bottom)
        setContentView(root)
    }

    private fun openSystemSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: Exception) {
            try { startActivity(Intent(Settings.ACTION_SETTINGS)) }
            catch (_: Exception) {
                AlertDialog.Builder(this).setMessage(
                    tr("Settings unavailable on this device.", "Configurações indisponíveis neste dispositivo.")
                ).setPositiveButton("OK", null).show()
            }
        }
    }

    private fun shareHelper() {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DebloaToast — Windows")
            putExtra(Intent.EXTRA_TEXT, helperUrl)
        }
        startActivity(Intent.createChooser(share,
            tr("Send the download link to your computer", "Envie o link para seu computador")))
    }

    private fun checkConnection() {
        if (checking) return
        checking = true
        lastError = ""
        draw()
        worker.execute {
            var ok = false
            for (attempt in 0..2) {
                try {
                    val reply = AdbCore.command(applicationContext, 5555, "echo OPTIMIZER_READY")
                    if (reply.contains("OPTIMIZER_READY")) { ok = true; break }
                } catch (_: Exception) { }
                if (attempt < 2) Thread.sleep(1700)
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                checking = false
                if (ok) {
                    prefs.edit().putString("connection_port", "5555").apply()
                    setResult(RESULT_OK)
                    AlertDialog.Builder(this)
                        .setTitle(tr("Connected", "Conectado"))
                        .setMessage(tr(
                            "Full access is ready. You can now manage factory apps in DebloaToast.",
                            "O acesso completo está pronto. Agora você pode gerenciar os aplicativos de fábrica no DebloaToast."
                        ))
                        .setPositiveButton(tr("Finish", "Concluir")) { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                } else {
                    lastError = tr(
                        "Not connected yet. Make sure the Windows helper says Ready, accept any debugging prompt on the phone, then tap Check connection again.",
                        "Ainda não conectou. Confira se o assistente Windows mostrou Pronto, permita qualquer solicitação de depuração no celular e toque novamente em Verificar conexão."
                    )
                    draw()
                }
            }
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
