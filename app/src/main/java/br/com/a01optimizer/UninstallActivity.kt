package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Gerenciamento manual de aplicativos via ADB sem fio já pareado.
 * Apps comuns: removidos do usuário 0, com perda potencial de dados.
 * Apps de fábrica: removidos somente do usuário 0, com dados preservados e restauração.
 * Nenhum pacote pode ser inventado: só pacotes obtidos do inventário ADB e
 * identificadores previamente validados. Risco alto exige confirmação exata.
 */
class UninstallActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("a01", Context.MODE_PRIVATE) }
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var connectionPort: EditText
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
    private lateinit var expertButton: Button
    private lateinit var sortSpinner: Spinner
    private lateinit var usageAccessButton: Button
    private lateinit var sizeButton: Button
    private lateinit var ramButton: Button

    private var loaded = false
    private var busy = false
    private var supported = false
    private var advancedMode = false
    private var expertMode = false
    private var sortChoice = 0
    private var userApps = emptySet<String>()
    private var factoryApps = emptySet<String>()
    private var disabledApps = emptySet<String>()
    private var labels = emptyMap<String, String>()
    private var ramKb = emptyMap<String, Long>()
    private var sizeBytes = emptyMap<String, Long>()
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

        root.addView(text("Todos os aplicativos", 24f, true))
        root.addView(text("A01 Optimizer 0.6 • Galaxy A01", 13f))
        root.addView(text("1. Ative Depuração sem fio. Para o primeiro uso desta versão, prepare a notificação, escolha Parear com código e responda à notificação com PORTA CÓDIGO. Depois informe abaixo a porta de conexão (não a de pareamento).", 14f))
        root.addView(button("Preparar notificação de pareamento") {
            try {
                PairingReceiver.prepare(this)
                say("Notificação preparada. Abra Depuração sem fio e faça o pareamento.")
            } catch (e: Exception) { say("Erro ao preparar pareamento: ${e.message}") }
        })
        root.addView(button("Abrir Opções do desenvolvedor") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        })
        connectionPort = EditText(this).apply {
            hint = "Porta de conexão (ex.: 38437)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
            setText(prefs.getString("connection_port", ""))
        }
        root.addView(connectionPort)
        root.addView(button("Abrir diagnóstico e desativação reversível") {
            startActivity(Intent(this, MainActivity::class.java))
        })
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

        root.addView(text("Ordenar os aplicativos por", 15f, true))
        sortSpinner = Spinner(this)
        val sortNames = arrayOf("Nome (A–Z)", "Tamanho (maior primeiro)", "RAM agora (maior primeiro)")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortNames)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sortChoice = position
                renderLists()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        root.addView(sortSpinner)
        root.addView(text("Tamanho: aplicativo + dados, quando o Android permitir acesso. RAM: memória PSS dos processos ativos, em uma fotografia do momento. '—' significa que não há medição; não significa zero.", 12f))
        usageAccessButton = button("Permitir acesso aos tamanhos") {
            try { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
            catch (ex: Exception) { say("Não foi possível abrir a permissão: ${ex.message}") }
        }
        root.addView(usageAccessButton)
        sizeButton = button("Calcular tamanhos (pode demorar)") { updateSizes() }
        root.addView(sizeButton)
        ramButton = button("Atualizar RAM agora") { updateRam() }
        root.addView(ramButton)

        space(root)
        factoryTitle = text("1. Todos os aplicativos de fábrica", 19f, true)
        root.addView(factoryTitle)
        root.addView(text("TODOS os pacotes aparecem abaixo, sem grupos escondidos. Samsung tem seção própria. VERDE é opcional; AMARELO exige modo avançado; VERMELHO tem risco alto e exige confirmação reforçada. Uma remoção errada pode impedir o celular de iniciar ou funcionar corretamente.", 14f))
        root.addView(text("Possíveis candidatos no grupo vermelho, SE você não usa a função: papéis de parede animados, fundos de chamadas, seletores de toque, fila de impressão e histórico de localização. São hipóteses, não garantias; procure a etiqueta POSSÍVEL CANDIDATO.", 13f))
        advancedButton = button("Ativar modo avançado (opções amarelas)") { toggleAdvanced() }
        root.addView(advancedButton)
        expertButton = button("Desbloquear remoção de alto risco (vermelho)") { toggleExpert() }
        root.addView(expertButton)
        factoryList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(factoryList)
        factoryButton = button("Revisar remoção dos apps de fábrica") { confirmFactoryRemoval() }
        root.addView(factoryButton)
        restoreButton = button("Restaurar apps de fábrica removidos por esta versão") { confirmRestore() }
        root.addView(restoreButton)
        space(root)

        userTitle = text("2. Aplicativos instalados por você", 19f, true)
        root.addView(userTitle)
        root.addView(text("Apps comuns podem perder dados, conversas e configurações ao serem desinstalados. Bancos identificados e as versões do A01 Optimizer permanecem protegidos.", 14f))
        userList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(userList)
        userButton = button("Revisar e desinstalar apps comuns") { confirmUserRemoval() }
        root.addView(userButton)
        space(root)
        root.addView(text("Não há remoção automática. Não recomendo apagar Play Services, WebView, teclado, telefonia, biometria ou segurança: bancos e funções essenciais podem parar de funcionar. Desligue a depuração sem fio quando terminar.", 13f))
        space(root)
        root.addView(text("Desenvolvido por Arthur Henrique, em 2026", 14f, true))

        renderLists()
        updateButtons()
    }

    override fun onResume() {
        super.onResume()
        if (::sizeButton.isInitialized) updateButtons()
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
        name == "android" || name.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))

    private fun parsePackages(output: String): Set<String> = output.lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("package:") }
        .map { it.removePrefix("package:").trim() }
        .filter(::validName)
        .toSet()

    private fun port(): Int {
        val n = connectionPort.text.toString().trim().toIntOrNull()
            ?: error("Informe a porta de conexão mostrada na tela Depuração sem fio.")
        require(n in 1..65535) { "Porta ADB inválida. Confira a porta de conexão." }
        prefs.edit().putString("connection_port", n.toString()).apply()
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
                KnownNames.label(id) ?: optionalFactory[id]?.label ?: try {
                    val info = packageManager.getApplicationInfo(id, 0)
                    packageManager.getApplicationLabel(info).toString().ifBlank { id }
                } catch (_: Exception) { id }
            }
            val ramSnapshot = try {
                PackageMetrics.ramPssKb(AdbCore.command(this, p, "dumpsys meminfo"), available)
            } catch (_: Exception) { emptyMap() }
            runOnUiThread {
                loaded = true
                supported = inventory.model.startsWith("SM-A015M") ||
                    inventory.model.startsWith("SM-A015F")
                userApps = inventory.user
                factoryApps = inventory.factory
                disabledApps = inventory.disabled
                labels = names
                ramKb = ramSnapshot
                sizeBytes = emptyMap()
                selectedUser.retainAll(userApps)
                selectedFactory.retainAll(factoryApps)
                renderLists()
                say(if (supported)
                    "${inventory.model}: ${userApps.size} apps instalados e ${factoryApps.size} de fábrica. RAM medida agora; tamanhos sob consulta."
                else "${inventory.model}: operações bloqueadas fora do Galaxy A01.")
            }
        }
    }

    private fun updateSizes() {
        if (!loaded || !supported) return
        if (!PackageMetrics.hasUsageAccess(this)) {
            say("Conceda primeiro o acesso a dados de uso nas Configurações.")
            return
        }
        runTask("Calculando espaço ocupado pelos aplicativos; pode demorar...") { _ ->
            val sizes = PackageMetrics.storageBytes(this, userApps + factoryApps)
            runOnUiThread {
                sizeBytes = sizes
                renderLists()
                say("Tamanhos disponíveis para ${sizes.size} aplicativos. É espaço do app e seus dados, não espaço que a remoção de fábrica necessariamente libera.")
            }
        }
    }

    private fun updateRam() {
        if (!loaded || !supported) return
        runTask("Atualizando memória dos processos ativos...") { p ->
            val updated = PackageMetrics.ramPssKb(
                AdbCore.command(this, p, "dumpsys meminfo"), userApps + factoryApps
            )
            runOnUiThread {
                ramKb = updated
                renderLists()
                say("RAM atualizada para ${updated.size} pacotes com processos identificados. A ausência de PSS não significa que o app nunca consome RAM.")
            }
        }
    }

    private fun ordered(ids: List<String>): List<String> = when (sortChoice) {
        1 -> ids.sortedWith(compareByDescending<String> { sizeBytes[it] ?: -1L }
            .thenBy { (labels[it] ?: it).lowercase(Locale.ROOT) })
        2 -> ids.sortedWith(compareByDescending<String> { ramKb[it] ?: -1L }
            .thenBy { (labels[it] ?: it).lowercase(Locale.ROOT) })
        else -> ids.sortedBy { (labels[it] ?: it).lowercase(Locale.ROOT) }
    }

    private fun measurements(id: String): String =
        "Tamanho: ${PackageMetrics.bytesLabel(sizeBytes[id])} • RAM agora: ${PackageMetrics.ramLabel(ramKb[id])}"

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
        if (id !in factoryApps || !validName(id) || ProtectedInfo.isCritical(id)) return false
        val entry = optionalFactory[id]
        return if (entry == null || protected(id)) expertMode
            else entry.risk == Risk.LOWER || advancedMode
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
            .setMessage("As opções AMARELAS podem retirar recursos úteis: calendário, alarmes, voz, arquivos e sincronização. Os VERMELHOS exigem desbloqueio separado. Nada será removido automaticamente.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Ativar") { _, _ ->
                advancedMode = true
                renderLists()
                say("Modo avançado ativado. Revise as opções amarelas antes de selecionar.")
            }.show()
    }

    private fun toggleExpert() {
        if (expertMode) {
            expertMode = false
            selectedFactory.removeAll { it !in optionalFactory || protected(it) }
            renderLists()
            say("Remoção de alto risco bloqueada novamente.")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Desbloquear remoção de alto risco?")
            .setMessage("ATENÇÃO: você poderá remover pacotes que não foram validados no Galaxy A01. Isso pode quebrar bancos, chamadas, Wi-Fi ou até a inicialização do Android. Restaurar pelo aplicativo só funciona se o Android e o ADB continuarem acessíveis. Serviços absolutamente críticos continuarão bloqueados para preservar a recuperação. Cada pacote vermelho deverá ser confirmado individualmente, digitando seu identificador completo.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Entendi os riscos") { _, _ ->
                expertMode = true
                renderLists()
                say("Modo de alto risco ativo. Nenhum pacote foi marcado automaticamente.")
            }.show()
    }

    private fun renderLists() {
        if (!::userList.isInitialized || !::factoryList.isInitialized || !::search.isInitialized) return
        userChecks.clear()
        userList.removeAllViews()
        val users = ordered(userApps.filter(::searchMatch))
        userTitle.text = "2. Aplicativos instalados por você (${userApps.size})"
        if (!loaded) userList.addView(text("Conecte e atualize para ver os aplicativos.", 14f))
        else if (users.isEmpty()) userList.addView(text("Nenhum aplicativo encontrado nesta busca.", 14f))
        users.forEach { id ->
            val locked = protected(id)
            val checkbox = CheckBox(this).apply {
                text = (labels[id] ?: id) + (if (locked) " — PROTEGIDO" else "") +
                    "\n$id\n${measurements(id)}" +
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
        val greenCount = factoryApps.count { optionalFactory[it]?.risk == Risk.LOWER && !protected(it) }
        val yellowCount = factoryApps.count { optionalFactory[it]?.risk == Risk.CONDITIONAL && !protected(it) }
        val redCount = factoryApps.size - greenCount - yellowCount
        factoryTitle.text = "1. Apps de fábrica (${factoryApps.size}) • Verde: $greenCount • Amarelo: $yellowCount • Vermelho: $redCount"
        advancedButton.text = if (advancedMode) "Desativar modo avançado (amarelo)"
            else "Ativar modo avançado (amarelo)"
        expertButton.text = if (expertMode) "Bloquear novamente a remoção de alto risco"
            else "Desbloquear remoção de alto risco (vermelho)"

        if (!loaded) {
            factoryList.addView(text("Conecte e atualize para ver todos os pacotes.", 14f))
        } else {
            val matching = factoryApps.filter(::searchMatch)
            for (group in listOf("SAMSUNG", "ANDROID / GOOGLE", "OUTROS / OPERADORA")) {
                val items = ordered(matching.filter { KnownNames.group(it) == group })
                if (items.isEmpty()) continue
                factoryList.addView(header("$group (${items.size})", Color.rgb(30, 72, 124)))
                items.forEach { id ->
                    val entry = optionalFactory[id]
                    val category = when {
                        ProtectedInfo.isCritical(id) -> "CRÍTICO — BLOQUEADO"
                        entry?.risk == Risk.LOWER && !protected(id) -> "VERDE — MENOR RISCO"
                        entry?.risk == Risk.CONDITIONAL && !protected(id) -> "AMARELO — AVANÇADO"
                        else -> "VERMELHO — ALTO RISCO"
                    }
                    val explanation = entry?.explanation ?: ProtectedInfo.reason(id)
                    val suggestion = RiskSuggestions.notes[id]?.let { "\nPOSSÍVEL CANDIDATO: $it" } ?: ""
                    val disabledNote = if (id in disabledApps) "\n(Já está desativado.)" else ""
                    val checkbox = CheckBox(this).apply {
                        text = "${labels[id] ?: id} [$category]\n$id\n${measurements(id)}\n$explanation$suggestion$disabledNote"
                        textSize = 14f
                        setPadding(0, 5.dp(), 0, 5.dp())
                        isChecked = id in selectedFactory
                        isEnabled = !busy && supported && authorizedFactory(id)
                        setOnCheckedChangeListener { _, checked ->
                            if (checked) selectedFactory.add(id) else selectedFactory.remove(id)
                            updateButtons()
                        }
                    }
                    factoryChecks[id] = checkbox
                    factoryList.addView(checkbox)
                }
            }
            if (matching.isEmpty()) {
                factoryList.addView(text("Nenhum pacote encontrado nesta busca.", 14f))
            }
        }
        updateButtons()
    }

    private fun updateButtons() {
        if (!::scanButton.isInitialized) return
        scanButton.isEnabled = !busy
        userButton.isEnabled = !busy && loaded && supported &&
            selectedUser.any { it in userApps && !protected(it) }
        factoryButton.isEnabled = !busy && loaded && supported &&
            selectedFactory.any { it in factoryApps && authorizedFactory(it) }
        restoreButton.isEnabled = !busy && loaded && supported && removedFactory().isNotEmpty()
        advancedButton.isEnabled = !busy && loaded && supported
        expertButton.isEnabled = !busy && loaded && supported
        val usageGranted = PackageMetrics.hasUsageAccess(this)
        usageAccessButton.text = if (usageGranted) "Acesso a tamanhos autorizado"
            else "Permitir acesso aos tamanhos"
        usageAccessButton.isEnabled = !busy && !usageGranted
        sizeButton.isEnabled = !busy && loaded && supported && usageGranted
        ramButton.isEnabled = !busy && loaded && supported
        sortSpinner.isEnabled = !busy
        userChecks.forEach { (id, check) -> check.isEnabled = !busy && supported && !protected(id) }
        factoryChecks.forEach { (id, check) -> check.isEnabled = !busy && supported && authorizedFactory(id) }
    }

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
        val lines = targets.joinToString("\n") { "• ${labels[it] ?: it} ($it)" }
        val redTargets = targets.filter { it !in optionalFactory || protected(it) }
        if (redTargets.isNotEmpty()) {
            if (targets.size != 1) {
                AlertDialog.Builder(this).setTitle("Confirmação individual obrigatória")
                    .setMessage("Pacotes VERMELHOS devem ser removidos um de cada vez. Desmarque os demais pacotes e selecione apenas um de alto risco.")
                    .setPositiveButton("OK", null).show()
                return
            }
            val id = redTargets.single()
            val type = if (RiskSuggestions.notes.containsKey(id))
                "Há uma hipótese de que seja dispensável caso você não use a função descrita. ISSO NÃO FOI TESTADO NO SEU APARELHO."
            else "Não há validação de segurança para remover esse pacote."
            val exact = EditText(this).apply {
                hint = "Digite exatamente: $id"
                setSingleLine(true)
            }
            AlertDialog.Builder(this).setTitle("RISCO ALTO: $id")
                .setMessage("$type\n\n${ProtectedInfo.reason(id)}\n\nPode impedir bancos, recursos básicos ou a inicialização. Se o Android ou ADB pararem, o botão de restaurar pode não funcionar. Para confirmar, digite o IDENTIFICADOR COMPLETO abaixo.")
                .setView(exact)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover mesmo assim") { _, _ ->
                    if (exact.text.toString().trim() == id) removeFactoryApps(listOf(id))
                    else say("Identificador diferente. A remoção de alto risco foi cancelada.")
                }.show()
            return
        }
        val advancedCount = targets.count { optionalFactory[it]?.risk == Risk.CONDITIONAL }
        AlertDialog.Builder(this).setTitle("Remover ${targets.size} app(s) de fábrica?")
            .setMessage("Os componentes deixarão de funcionar para o seu usuário. Não excluímos os arquivos da partição do sistema. Podemos restaurá-los nesta versão.\n\n$lines\n\nOpções amarelas nesta seleção: $advancedCount. Isso pode afetar funções do celular. Faça backup antes.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Remover") { _, _ -> removeFactoryApps(targets) }.show()
    }

    private fun removeFactoryApps(targets: List<String>) {
        runTask("Removendo aplicativos de fábrica do usuário 0...") { p ->
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
                    .setMessage("Removidos para o usuário atual: $successes\nFalhas: $errors\n\nA restauração só é possível pelo app se o Android e o ADB continuarem funcionando. Teste as funções essenciais.")
                    .setPositiveButton("OK", null).show()
            }
        }
    }

    private fun confirmRestore() {
        val records = removedFactory().filter(::validName)
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
            for (id in removedFactory().filter(::validName)) {
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
