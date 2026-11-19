package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors

/**
 * A01 Optimizer: somente comandos de leitura e uma lista fechada de desativações reversíveis.
 * Não altera banco, Knox, serviços Google, Play Integrity, root ou bootloader.
 */
class MainActivity : Activity() {
    private val preferences by lazy { getSharedPreferences("a01", Context.MODE_PRIVATE) }
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var connectionPort: EditText
    private lateinit var list: LinearLayout
    private lateinit var connectButton: Button
    private lateinit var disableButton: Button
    private lateinit var restoreButton: Button
    private lateinit var reportButton: Button
    private var systemPackages = emptySet<String>()
    private val chosen = linkedSetOf<String>()
    private var installed = emptySet<String>()
    private var disabled = emptySet<String>()
    private var supported = false
    private var connected = false
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 20.dp(), 18.dp(), 22.dp())
            setBackgroundColor(Color.rgb(245, 247, 250))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        root.addView(label("A01 Optimizer", 27f, true))
        root.addView(label("Android 12 • " + Build.MODEL + " • versão experimental 0.2 — diagnóstico", 13f))
        gap(root)
        status = label("Ainda não conectado", 16f, true)
        root.addView(status)
        gap(root)

        root.addView(label("1. Parear sem computador", 19f, true))
        root.addView(label("Conecte-se a uma rede Wi-Fi confiável. Toque em Preparar, abra Depuração sem fio e escolha Parear dispositivo com código. Sem fechar a tela, puxe as notificações e responda com PORTA CÓDIGO (separados por espaço).", 14f))
        root.addView(action("Preparar notificação de pareamento") {
            try {
                PairingReceiver.prepare(this)
                message("Notificação preparada. Abra Depuração sem fio nas Configurações.")
            } catch (ex: Exception) { message("Não foi possível criar a notificação: " + ex.message) }
        })
        root.addView(action("Abrir Opções do desenvolvedor") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        })
        gap(root)

        root.addView(label("2. Conectar e analisar", 19f, true))
        root.addView(label("Após parear, volte à página Depuração sem fio. No campo Endereço IP e porta, copie apenas o número que aparece depois de dois-pontos. Essa NÃO é a porta de pareamento.", 14f))
        connectionPort = EditText(this).apply {
            hint = "Porta de conexão (ex.: 38437)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
            setText(preferences.getString("connection_port", ""))
        }
        root.addView(connectionPort)
        connectButton = action("Conectar e analisar") { connect() }
        root.addView(connectButton)
        gap(root)

        root.addView(label("3. Inventário do sistema", 19f, true))
        root.addView(label("Esta versão lê os pacotes pré-instalados, sem modificar nada. O relatório é copiado apenas quando você tocar no botão; ele não é enviado automaticamente.", 14f))
        reportButton = action("Copiar diagnóstico completo") { copyDiagnostic() }
        root.addView(reportButton)
        gap(root)

        root.addView(label("4. Aplicativos opcionais encontrados", 19f, true))
        root.addView(label("Somente leitura nesta versão: os itens abaixo são os que a versão anterior conhecia. Nenhum aplicativo será desativado aqui.", 14f))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        list.addView(label("Conecte o ADB para identificar os aplicativos.", 14f))
        root.addView(list)
        disableButton = action("Desativar selecionados") { confirmDisable() }
        disableButton.visibility = View.GONE
        root.addView(disableButton)
        gap(root)

        root.addView(label("5. Restauração na versão anterior", 19f, true))
        root.addView(label("Não desinstale o A01 Optimizer 0.1.1: a versão anterior guarda o histórico de desativações. O diagnóstico não faz alterações.", 14f))
        restoreButton = action("Restaurar alterações") { confirmRestore() }
        restoreButton.visibility = View.GONE
        root.addView(restoreButton)
        gap(root)
        root.addView(label("Depois de copiar, cole o texto no ChatGPT para uma análise do seu modelo. Desligue a depuração sem fio quando terminar.", 13f))
        updateButtons()
    }

    private fun Int.dp() = (this * resources.displayMetrics.density + 0.5f).toInt()
    private fun gap(layout: LinearLayout) {
        layout.addView(View(this), LinearLayout.LayoutParams(1, 18.dp()))
    }
    private fun label(value: String, size: Float, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.rgb(31, 44, 60))
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 5.dp(), 0, 5.dp())
        }
    private fun action(value: String, click: () -> Unit) =
        Button(this).apply {
            text = value
            isAllCaps = false
            setOnClickListener { click() }
        }
    private fun message(value: String) { status.text = value }
    private fun saved(): MutableSet<String> =
        preferences.getStringSet("modified", emptySet())?.toMutableSet() ?: mutableSetOf()
    private fun save(value: Set<String>) {
        preferences.edit().putStringSet("modified", value.toSet()).commit()
    }
    private fun updateButtons() {
        connectButton.isEnabled = !busy
        reportButton.isEnabled = !busy && connected && systemPackages.isNotEmpty()
        disableButton.isEnabled = !busy && connected && supported && chosen.isNotEmpty()
        restoreButton.isEnabled = !busy && connected && saved().isNotEmpty()
    }
    private fun port(): Int {
        val p = connectionPort.text.toString().trim().toIntOrNull() ?: error("Digite uma porta numérica.")
        require(p in 1..65535) { "Porta de conexão inválida" }
        return p
    }
    private fun packages(output: String) = output.lines()
        .map { it.trim() }.filter { it.startsWith("package:") }
        .map { it.removePrefix("package:").trim() }.toSet()

    private fun task(start: String, body: () -> Unit) {
        if (busy) return
        busy = true
        updateButtons()
        message(start)
        worker.execute {
            try { body() }
            catch (ex: Exception) {
                runOnUiThread {
                    message("Erro: " + (ex.message ?: "não identificado").take(180))
                    AlertDialog.Builder(this).setTitle("Operação não concluída")
                        .setMessage((ex.message ?: "Confira a conexão ADB").take(500))
                        .setPositiveButton("OK", null).show()
                }
            } finally {
                runOnUiThread { busy = false; updateButtons() }
            }
        }
    }

    private fun connect() {
        val p = try { port() } catch (ex: Exception) { message(ex.message ?: "Porta inválida"); return }
        preferences.edit().putString("connection_port", p.toString()).apply()
        task("Conectando ao ADB local...") {
            val model = AdbCore.command(this, p, "getprop ro.product.model").trim()
            val present = packages(AdbCore.command(this, p, "pm list packages"))
            val deactivated = packages(AdbCore.command(this, p, "pm list packages -d"))
            val system = packages(AdbCore.command(this, p, "pm list packages -s"))
            runOnUiThread {
                supported = model.startsWith("SM-A015M") || model.startsWith("SM-A015F")
                connected = true
                installed = present
                disabled = deactivated
                systemPackages = system
                showCatalog()
                if (supported) {
                    val n = SafeCatalog.entries.count { it.name in present }
                    message("Conectado: " + model + ". " + system.size + " pacotes do sistema detectados; " + n + " do catálogo antigo.")
                } else {
                    message("Conectado: " + model + ". Desativação bloqueada fora do Galaxy A01.")
                }
            }
        }
    }

    private fun showCatalog() {
        chosen.clear()
        list.removeAllViews()
        val options = SafeCatalog.entries.filter { it.name in installed }
        if (options.isEmpty()) {
            list.addView(label("Nenhum item do catálogo encontrado. Nada para otimizar nesta versão.", 14f))
        }
        for (item in options) {
            val isDisabled = item.name in disabled
            val mark = if (isDisabled) " (já desativado)" else ""
            val check = CheckBox(this).apply {
                text = item.label + mark + "\n" + item.explanation
                textSize = 15f
                isEnabled = supported && !isDisabled
                isChecked = false
                setPadding(0, 8.dp(), 0, 8.dp())
                setOnCheckedChangeListener { _, checked ->
                    if (checked) chosen.add(item.name) else chosen.remove(item.name)
                    updateButtons()
                }
            }
            list.addView(check)
        }
        updateButtons()
    }


    private fun copyDiagnostic() {
        if (!connected || systemPackages.isEmpty()) {
            message("Conecte e analise o celular primeiro.")
            return
        }
        val active = systemPackages.subtract(disabled)
        val inactive = systemPackages.intersect(disabled)
        val report = buildString {
            appendLine("A01 DIAGNOSTICO V0.2 - SOMENTE PACOTES DO SISTEMA")
            appendLine("Modelo: " + Build.MODEL)
            appendLine("Android: " + Build.VERSION.RELEASE)
            appendLine("Pacotes de sistema: " + systemPackages.size)
            appendLine("Ativos: " + active.size + " / Desativados: " + inactive.size)
            appendLine()
            appendLine("=== DESATIVADOS ===")
            inactive.sorted().forEach { appendLine(it) }
            appendLine()
            appendLine("=== ATIVOS ===")
            active.sorted().forEach { appendLine(it) }
            appendLine()
            appendLine("Relatório copiado manualmente. Nenhuma mudança foi feita no aparelho.")
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Inventário A01", report))
        AlertDialog.Builder(this)
            .setTitle("Diagnóstico copiado")
            .setMessage("Copiei " + systemPackages.size + " pacotes do sistema para a área de transferência. Abra a conversa com o ChatGPT e cole o texto. Nenhum pacote foi desativado.")
            .setPositiveButton("OK", null).show()
    }

    private fun confirmDisable() {
        if (!supported || !connected || chosen.isEmpty()) return
        val selected = chosen.filter { SafeCatalog.allowed(it) && it in installed && it !in disabled }
        if (selected.isEmpty()) return
        val names = SafeCatalog.entries.filter { it.name in selected }.joinToString("\n") { it.label }
        AlertDialog.Builder(this).setTitle("Confirmar desativação")
            .setMessage("Desativar os aplicativos abaixo?\n\n" + names +
                "\n\nPode ser desfeito. Não há garantia de melhora de desempenho. Teste seus bancos depois.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Desativar") { _, _ -> disable(selected) }.show()
    }

    private fun disable(items: List<String>) {
        val p = try { port() } catch (ex: Exception) { message("Porta inválida"); return }
        task("Desativando pacotes autorizados...") {
            var successful = 0
            var failed = 0
            for (name in items) {
                if (!SafeCatalog.allowed(name)) continue
                val previouslyDisabled = packages(AdbCore.command(this, p, "pm list packages -d"))
                if (name in previouslyDisabled) continue
                val history = saved()
                history.add(name)
                save(history)
                try {
                    val out = AdbCore.command(this, p, "pm disable-user --user 0 " + name)
                    if (!out.contains("disabled", ignoreCase = true)) error("Sem confirmação")
                    successful++
                } catch (ex: Exception) {
                    val stillDisabled = try {
                        name in packages(AdbCore.command(this, p, "pm list packages -d"))
                    } catch (_: Exception) { true }
                    if (!stillDisabled) {
                        val undo = saved()
                        undo.remove(name)
                        save(undo)
                    }
                    failed++
                }
            }
            val result = packages(AdbCore.command(this, p, "pm list packages -d"))
            runOnUiThread {
                disabled = result
                showCatalog()
                message("Desativados: " + successful + " • Falhas: " + failed)
                AlertDialog.Builder(this).setTitle("Resultado")
                    .setMessage("Desativados: " + successful + "\nFalhas: " + failed +
                        "\n\nTeste seus bancos e as funções do celular.")
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    private fun confirmRestore() {
        if (!connected || saved().isEmpty()) return
        AlertDialog.Builder(this).setTitle("Restaurar alterações?")
            .setMessage("Todos os pacotes que este aplicativo registrou serão reativados. Nenhum dado será apagado.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Restaurar") { _, _ -> restore() }.show()
    }
    private fun restore() {
        val p = try { port() } catch (ex: Exception) { message("Porta inválida"); return }
        task("Restaurando pacotes...") {
            var count = 0
            for (name in saved().filter { SafeCatalog.allowed(it) }) {
                try {
                    if (name in packages(AdbCore.command(this, p, "pm list packages -d"))) {
                        AdbCore.command(this, p, "pm enable --user 0 " + name)
                    }
                    val stillDisabled = name in packages(AdbCore.command(this, p, "pm list packages -d"))
                    if (!stillDisabled) {
                        val history = saved()
                        history.remove(name)
                        save(history)
                        count++
                    }
                } catch (_: Exception) { /* Keep in history for retry. */ }
            }
            val current = packages(AdbCore.command(this, p, "pm list packages -d"))
            runOnUiThread {
                disabled = current
                showCatalog()
                message("Restaurados: " + count + " • Pendentes: " + saved().size)
            }
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
