package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Gerenciamento manual de aplicativos via ADB sem fio já pareado.
 * Apps comuns: removidos do usuário 0, com perda potencial de dados.
 * Apps de fábrica: removidos somente do usuário 0, com dados preservados e restauração.
 * Nunca executa nomes arbitrários: pacote validado + allowlist de fábrica.
 */
class UninstallActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("a01", Context.MODE_PRIVATE) }
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var search: EditText
    private lateinit var userList: LinearLayout
    private lateinit var factoryList: LinearLayout
    private lateinit var userTitle: TextView
    private lateinit var factoryTitle: TextView
    private lateinit var scanButton: Button
    private lateinit var userButton: Button
    private lateinit var factoryButton: Button
    private lateinit var restoreButton: Button
    private lateinit var advancedButton: Button
    private lateinit var showProtectedButton: Button

    private var loaded = false
    private var busy = false
    private var supported = false
    private var advancedMode = false
    private var showProtected = false
    private var userApps = emptySet<String>()
    private var factoryApps = emptySet<String>()
    private var disabledApps = emptySet<String>()
    private var labels = emptyMap<String, String>()
    private val selectedUser = linkedSetOf<String>()
    private val selectedFactory = linkedSetOf<String>()
    private val userChecks = linkedMapOf<String, CheckBox>()
    private val factoryChecks = linkedMapOf<String, CheckBox>()

    private val optionalFactory by lazy {
        (SafeCatalog.entries + FactoryExtras.entries + AdvancedCatalog.entries).associateBy { it.name }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val outer = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 26.dp())
            setBackgroundColor(Color.rgb(245, 247, 250))
        }
        outer.addView(root)
        setContentView(outer)

        root.addView(text("Desinstalar aplicativos", 24f, true))
        root.addView(text("A01 Optimizer 0.5 • Galaxy A01", 13f))
        root.addView(text("O celular deve estar pareado e conectado à depuração sem fio. Use a porta de conexão que você informou na tela anterior.", 14f))
        space(root)
        status = text("Toque em Atualizar lista. Nenhuma alteração foi feita.", 15f, true)
        root.addView(status)
        scanButton = button("Atualizar lista de aplicativos") { scan() }
        root.addView(scanButton)
        space(root)

        root.addView(text("Pesquisar pelo nome ou identificador", 15f, true))
        search = EditText(this).apply {
            hint = "Ex.: YouTube, Samsung, Facebook..."
            setSingleLine(true)
        }
        root.addView(search)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = renderLists()
            override fun afterTextChanged(s: Editable?) = Unit
        })

        space(root)
        userTitle = text("1. Aplicativos instalados por você", 19f, true)
        root.addView(userTitle)
        root.addView(text("A remoção destes aplicativos pode apagar dados, conversas e configurações. Para recuperá-los, será preciso reinstalá-los. Bancos identificados ficam bloqueados.", 14f))
        userList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(userList)
        userButton = button("Revisar e desinstalar selecionados") { confirmUserRemoval() }
        root.addView(userButton)

        space(root)
        factoryTitle = text("2. Todos os aplicativos de fábrica", 19f, true)
        root.addView(factoryTitle)
        root.addView(text("VERDE: opcionais de menor risco. AMARELO: recursos que podem fazer falta e exigem modo avançado. VERMELHO: componentes protegidos que você pode consultar, mas não remover aqui. A remoção só vale para o usuário principal.", 14f))
        advancedButton = button("Ativar modo avançado (opções amarelas)") { toggleAdvanced() }
        root.addView(advancedButton)
        showProtectedButton = button("Mostrar componentes protegidos") {
            showProtected = !showProtected
            renderLists()
        }
        root.addView(showProtectedButton)
        factoryList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(factoryList)
        factoryButton = button("Revisar remoção dos apps de fábrica") { confirmFactoryRemoval() }
        root.addView(factoryButton)
        restoreButton = button("Restaurar apps de fábrica removidos por esta versão") { confirmRestore() }
        root.addView(restoreButton)
        space(root)
        root.addView(text("Não remova apps de banco, Play Services, teclado, telefonia, biometria, segurança ou outros componentes essenciais. Depois de qualquer mudança, teste seus aplicativos bancários e desligue a depuração sem fio.", 13f))

        renderLists()
        updateButtons()
    }

    private fun Int.dp() = (this * resources.displayMetrics.density + 0.5f).toInt()
    private fun space(v: LinearLayout) { v.addView(View(this), LinearLayout.LayoutParams(1, 16.dp())) }
    private fun text(value: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.rgb(31, 44, 60))
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        setPadding(0, 5.dp(), 0, 5.dp())
    }
    private fun header(value: String, rgb: Int) = text(value, 16f, true).apply {
        setTextColor(rgb)
        setPadding(0, 14.dp(), 0, 7.dp())
    }
    private fun button(title: String, callback: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        setOnClickListener { callback() }
    }
    private fun say(value: String) { status.text = value }

    private fun removedFactory(): MutableSet<String> =
        prefs.getStringSet("factory_removed", emptySet())?.toMutableSet() ?: mutableSetOf()
    private fun originallyDisabled(): MutableSet<String> =
        prefs.getStringSet("factory_was_disabled", emptySet())?.toMutableSet() ?: mutableSetOf()
    private fun saveFactoryHistory(removed: Set<String>, wasDisabled: Set<String>) {
        prefs.edit()
            .putStringSet("factory_removed", removed.toSet())
            .putStringSet("factory_was_disabled", wasDisabled.toSet())
            .commit()
    }

    private fun validName(name: String) =
        name.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))

    private fun parsePackages(output: String): Set<String> = output.lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("package:") }
        .map { it.removePrefix("package:").trim() }
        .filter(::validName)
        .toSet()

    private fun port(): Int {
        val n = prefs.getString("connection_port", "")?.toIntOrNull()
            ?: error("Volte à tela principal, informe a porta e toque em Conectar e analisar.")
        require(n in 1..65535) { "Porta ADB inválida. Reconecte na tela principal." }
        return n
    }

    private fun runTask(start: String, body: (Int) -> Unit) {
        if (busy) return
        val p = try { port() } catch (e: Exception) { say(e.message ?: "Porta inválida"); return }
        busy = true
        updateButtons()
        say(start)
        worker.execute {
            try { body(p) }
            catch (e: Exception) {
                runOnUiThread {
                    val detail = (e.message ?: "Conexão perdida").take(400)
                    say("Erro: $detail")
                    AlertDialog.Builder(this).setTitle("Operação interrompida")
                        .setMessage("$detail\n\nConfira a porta de conexão e atualize a lista.")
                        .setPositiveButton("OK", null).show()
                }
            } finally { runOnUiThread { busy = false; updateButtons() } }
        }
    }

    private fun readInventory(p: Int): Inventory {
        val model = AdbCore.command(this, p, "getprop ro.product.model").trim()
        val user = parsePackages(AdbCore.command(this, p, "pm list packages -3 --user 0"))
        val system = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
        val disabled = parsePackages(AdbCore.command(this, p, "pm list packages -d --user 0"))
        return Inventory(model, user, system, disabled)
    }

    private data class Inventory(
        val model: String,
        val user: Set<String>,
        val factory: Set<String>,
        val disabled: Set<String>
    )

    private fun scan() {
        runTask("Lendo aplicativos via ADB...") { p ->
            val inventory = readInventory(p)
            val available = inventory.user + inventory.factory
            val names = available.associateWith { id ->
                try {
                    val info = packageManager.getApplicationInfo(id, 0)
                    packageManager.getApplicationLabel(info).toString().ifBlank { id }
                } catch (_: Exception) { id }
            }
            runOnUiThread {
                loaded = true
                supported = inventory.model.startsWith("SM-A015M") ||
                    inventory.model.startsWith("SM-A015F")
                userApps = inventory.user
                factoryApps = inventory.factory
                disabledApps = inventory.disabled
                labels = names
                selectedUser.retainAll(userApps)
                selectedFactory.retainAll(factoryApps)
                renderLists()
                say(if (supported)
                    "${inventory.model}: ${userApps.size} apps instalados e ${factoryApps.size} de fábrica."
                else "${inventory.model}: operações bloqueadas fora do Galaxy A01.")
            }
        }
    }

    private fun searchMatch(id: String): Boolean {
        val term = search.text.toString().trim().lowercase(Locale.ROOT)
        return term.isEmpty() || id.lowercase(Locale.ROOT).contains(term) ||
            (labels[id] ?: "").lowercase(Locale.ROOT).contains(term)
    }

    private fun protected(id: String): Boolean {
        val p = id.lowercase(Locale.ROOT)
        val name = (labels[id] ?: "").lowercase(Locale.ROOT)
        if (p == packageName || p.startsWith("br.com.a01optimizer")) return true
        val bankNames = listOf(
            "bank", "banco", "nubank", "bradesco", "santander", "itau", "caixa",
            "intermedium", "picpay", "pagbank", "pagseguro", "banestes", "bb.android",
            "uol.ps", "mercadopago", "sicoob", "sicredi", "c6bank", "neon",
            "digio", "bmg", "willbank", "original", "btgpactual", "stone.banking"
        )
        return bankNames.any { p.contains(it) || name.contains(it) }
    }

    private fun authorizedFactory(id: String): Boolean {
        val entry = optionalFactory[id] ?: return false
        return !protected(id) && (entry.risk == Risk.LOWER || advancedMode)
    }

    private fun toggleAdvanced() {
        if (advancedMode) {
            advancedMode = false
            selectedFactory.removeAll { optionalFactory[it]?.risk == Risk.CONDITIONAL }
            renderLists()
            say("Modo avançado desligado. Opções amarelas desmarcadas.")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Ativar modo avançado?")
            .setMessage("As opções AMARELAS podem retirar recursos úteis: calendário, alarmes, voz, arquivos e sincronização. Os componentes VERMELHOS continuarão protegidos. Nada será removido automaticamente.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Ativar") { _, _ ->
                advancedMode = true
                renderLists()
                say("Modo avançado ativado. Revise as opções amarelas antes de selecionar.")
            }.show()
    }

    private fun renderLists() {
        if (!::userList.isInitialized || !::factoryList.isInitialized || !::search.isInitialized) return
        userChecks.clear()
        userList.removeAllViews()
        val users = userApps.filter(::searchMatch)
            .sortedBy { (labels[it] ?: it).lowercase(Locale.ROOT) }
        userTitle.text = "1. Aplicativos instalados por você (${userApps.size})"
        if (!loaded) userList.addView(text("Conecte e atualize para ver os aplicativos.", 14f))
        else if (users.isEmpty()) userList.addView(text("Nenhum aplicativo encontrado nesta busca.", 14f))
        users.forEach { id ->
            val locked = protected(id)
            val checkbox = CheckBox(this).apply {
                text = (labels[id] ?: id) + (if (locked) " — PROTEGIDO" else "") + "\n$id" +
                    (if (locked) "\n" + (if (id.startsWith("br.com.a01optimizer"))
                        "Ferramenta do A01 Optimizer; preserve o histórico de restauração."
                    else "Aplicativo bancário/financeiro identificado por nome; mantido protegido.") else "")
                textSize = 14f
                setPadding(0, 5.dp(), 0, 5.dp())
                isChecked = id in selectedUser
                isEnabled = !busy && supported && !locked
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedUser.add(id) else selectedUser.remove(id)
                    updateButtons()
                }
            }
            userChecks[id] = checkbox
            userList.addView(checkbox)
        }

        factoryChecks.clear()
        factoryList.removeAllViews()
        val eligible = optionalFactory.values.filter { it.name in factoryApps && !protected(it.name) }
        val red = factoryApps.filter { it !in optionalFactory || protected(it) }
            .sortedBy { (labels[it] ?: it).lowercase(Locale.ROOT) }
        val green = eligible.filter { it.risk == Risk.LOWER }
        val yellow = eligible.filter { it.risk == Risk.CONDITIONAL }
        factoryTitle.text = "2. Aplicativos de fábrica (${factoryApps.size}) • Verde: ${green.size} • Amarelo: ${yellow.size} • Vermelho: ${red.size}"
        showProtectedButton.text = if (showProtected) "Ocultar componentes protegidos (${red.size})"
            else "Mostrar componentes protegidos (${red.size})"
        advancedButton.text = if (advancedMode) "Desativar modo avançado (amarelo)"
            else "Ativar modo avançado (amarelo)"

        if (!loaded) {
            factoryList.addView(text("Conecte e atualize para ver todos os pacotes.", 14f))
        } else {
            for ((risk, group, title, color) in listOf(
                RiskGroup(Risk.LOWER, green, "VERDE • Menor risco", Color.rgb(30, 123, 65)),
                RiskGroup(Risk.CONDITIONAL, yellow, "AMARELO • Modo avançado", Color.rgb(146, 96, 0))
            )) {
                val matches = group.filter { searchMatch(it.name) }.sortedBy { it.label.lowercase(Locale.ROOT) }
                if (matches.isEmpty()) continue
                factoryList.addView(header("$title (${matches.size})", color))
                matches.forEach { entry ->
                    val id = entry.name
                    val checkbox = CheckBox(this).apply {
                        text = "${entry.label}\n$id\n${entry.explanation}" +
                            (if (id in disabledApps) "\n(Já está desativado.)" else "")
                        textSize = 14f
                        setPadding(0, 5.dp(), 0, 5.dp())
                        isChecked = id in selectedFactory
                        isEnabled = !busy && supported && (risk == Risk.LOWER || advancedMode)
                        setOnCheckedChangeListener { _, checked ->
                            if (checked) selectedFactory.add(id) else selectedFactory.remove(id)
                            updateButtons()
                        }
                    }
                    factoryChecks[id] = checkbox
                    factoryList.addView(checkbox)
                }
            }

            val redMatches = red.filter(::searchMatch)
            if (redMatches.isNotEmpty()) {
                factoryList.addView(header("VERMELHO • Protegidos (${redMatches.size})", Color.rgb(174, 39, 42)))
                if (showProtected || search.text.isNotBlank()) {
                    redMatches.forEach { id ->
                        val explanation = if (protected(id)) "App bancário/financeiro identificado; protegido."
                            else ProtectedInfo.reason(id)
                        factoryList.addView(text("${labels[id] ?: id} — PROTEGIDO\n$id\n$explanation" +
                            (if (id in disabledApps) "\n(Já está desativado.)" else ""), 13f))
                    }
                } else {
                    factoryList.addView(text("Toque em 'Mostrar componentes protegidos' para consultar nomes, identificadores e motivos. Não é possível selecioná-los.", 14f))
                }
            }
            if (eligible.none { searchMatch(it.name) } && redMatches.isEmpty()) {
                factoryList.addView(text("Nenhum pacote encontrado nesta busca.", 14f))
            }
        }
        updateButtons()
    }

    private data class RiskGroup(
        val risk: Risk,
        val entries: List<OptionalPackage>,
        val title: String,
        val color: Int
    )

    private fun updateButtons() {
        if (!::scanButton.isInitialized) return
        scanButton.isEnabled = !busy
        userButton.isEnabled = !busy && loaded && supported &&
            selectedUser.any { it in userApps && !protected(it) }
        factoryButton.isEnabled = !busy && loaded && supported &&
            selectedFactory.any { it in factoryApps && authorizedFactory(it) }
        restoreButton.isEnabled = !busy && loaded && supported && removedFactory().isNotEmpty()
        advancedButton.isEnabled = !busy && loaded && supported
        showProtectedButton.isEnabled = !busy && loaded && redCount() > 0
        userChecks.forEach { (id, check) -> check.isEnabled = !busy && supported && !protected(id) }
        factoryChecks.forEach { (id, check) -> check.isEnabled = !busy && supported && authorizedFactory(id) }
    }

    private fun redCount() = factoryApps.count { it !in optionalFactory || protected(it) }

    private fun confirmUserRemoval() {
        if (!loaded || !supported) return
        val targets = selectedUser.filter { it in userApps && !protected(it) && validName(it) }
        if (targets.isEmpty()) return
        val lines = targets.joinToString("\n") { "• ${labels[it] ?: it} ($it)" }
        AlertDialog.Builder(this).setTitle("Desinstalar ${targets.size} aplicativo(s)?")
            .setMessage("ESTA AÇÃO PODE APAGAR OS DADOS DOS APPS. Não há restauração automática para aplicativos instalados por você.\n\n$lines\n\nConfirma a desinstalação?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Desinstalar") { _, _ -> removeUserApps(targets) }.show()
    }

    private fun removeUserApps(targets: List<String>) {
        runTask("Desinstalando aplicativos selecionados...") { p ->
            var successes = 0
            var errors = 0
            for (id in targets) {
                if (!validName(id) || protected(id) || id !in userApps) continue
                try {
                    val response = AdbCore.command(this, p, "pm uninstall --user 0 $id")
                    if (!response.trim().startsWith("Success", ignoreCase = true)) error(response.take(120))
                    successes++
                } catch (_: Exception) { errors++ }
            }
            val now = readInventory(p)
            runOnUiThread {
                userApps = now.user
                factoryApps = now.factory
                disabledApps = now.disabled
                selectedUser.retainAll(now.user)
                selectedFactory.retainAll(now.factory)
                renderLists()
                say("Apps desinstalados: $successes • Falhas: $errors")
                AlertDialog.Builder(this).setTitle("Desinstalação concluída")
                    .setMessage("Removidos: $successes\nFalhas: $errors\n\nApps comuns podem perder dados. Se precisar, reinstale-os pela loja.")
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    private fun confirmFactoryRemoval() {
        if (!loaded || !supported) return
        val targets = selectedFactory.filter {
            it in factoryApps && authorizedFactory(it) && validName(it)
        }
        if (targets.isEmpty()) return
        val lines = targets.joinToString("\n") { "• ${optionalFactory[it]?.label ?: it} ($it)" }
        val advancedCount = targets.count { optionalFactory[it]?.risk == Risk.CONDITIONAL }
        AlertDialog.Builder(this).setTitle("Remover ${targets.size} app(s) de fábrica?")
            .setMessage("Os componentes deixarão de funcionar para o seu usuário. Não excluímos os arquivos da partição do sistema. Podemos restaurá-los nesta versão.\n\n$lines\n\nOpções amarelas nesta seleção: $advancedCount. Isso pode afetar funções do celular. Faça backup antes.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Remover") { _, _ -> removeFactoryApps(targets) }.show()
    }

    private fun removeFactoryApps(targets: List<String>) {
        runTask("Removendo opcionais de fábrica do usuário 0...") { p ->
            var successes = 0
            var errors = 0
            for (id in targets) {
                if (!validName(id) || id !in factoryApps || !authorizedFactory(id)) continue
                val history = removedFactory()
                val wasDisabled = originallyDisabled()
                history.add(id)
                if (id in disabledApps) wasDisabled.add(id) else wasDisabled.remove(id)
                saveFactoryHistory(history, wasDisabled)
                try {
                    val response = AdbCore.command(this, p, "pm uninstall -k --user 0 $id")
                    if (!response.trim().startsWith("Success", ignoreCase = true)) error(response.take(120))
                    successes++
                } catch (_: Exception) {
                    val stillPresent = try {
                        id in parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
                    } catch (_: Exception) { false }
                    if (stillPresent) {
                        val h = removedFactory()
                        val d = originallyDisabled()
                        h.remove(id); d.remove(id)
                        saveFactoryHistory(h, d)
                    }
                    errors++
                }
            }
            val now = readInventory(p)
            runOnUiThread {
                userApps = now.user
                factoryApps = now.factory
                disabledApps = now.disabled
                selectedFactory.retainAll(now.factory)
                renderLists()
                say("Fábrica removidos: $successes • Falhas: $errors")
                AlertDialog.Builder(this).setTitle("Remoção concluída")
                    .setMessage("Removidos para o usuário atual: $successes\nFalhas: $errors\n\nVocê pode usar o botão de restauração, sem reinstalar o Android.")
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    private fun confirmRestore() {
        val records = removedFactory().filter { optionalFactory.containsKey(it) && validName(it) }
        if (records.isEmpty() || !supported || !loaded) return
        AlertDialog.Builder(this).setTitle("Restaurar ${records.size} app(s) de fábrica?")
            .setMessage("Reinstala apenas para o usuário 0 os aplicativos de fábrica removidos por esta versão. Apps comuns desinstalados não podem ser restaurados por este botão.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Restaurar") { _, _ -> restoreFactory() }.show()
    }

    private fun restoreFactory() {
        runTask("Restaurando aplicativos de fábrica...") { p ->
            var count = 0
            var errors = 0
            for (id in removedFactory().filter { optionalFactory.containsKey(it) && validName(it) }) {
                try {
                    val response = AdbCore.command(this, p, "cmd package install-existing --user 0 $id")
                    if (response.contains("Failure", ignoreCase = true) || response.contains("Exception", ignoreCase = true)) {
                        error(response.take(120))
                    }
                    if (id in originallyDisabled()) {
                        AdbCore.command(this, p, "pm disable-user --user 0 $id")
                    }
                    val present = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
                    if (id !in present) error("App ainda não instalado para o usuário")
                    val h = removedFactory(); val d = originallyDisabled()
                    h.remove(id); d.remove(id)
                    saveFactoryHistory(h, d)
                    count++
                } catch (_: Exception) { errors++ }
            }
            val now = readInventory(p)
            runOnUiThread {
                userApps = now.user
                factoryApps = now.factory
                disabledApps = now.disabled
                renderLists()
                say("Fábrica restaurados: $count • Pendentes: ${removedFactory().size}")
                AlertDialog.Builder(this).setTitle("Restauração concluída")
                    .setMessage("Restaurados: $count\nNão restaurados nesta tentativa: $errors\n\nConfira os aplicativos e suas funções.")
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
