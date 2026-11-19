package br.com.a01optimizer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.util.concurrent.Executors

class DashboardActivity : Activity() {
    private enum class Tab { APPS, GUIDE, TOOLS, SETTINGS }

    private val prefs by lazy { getSharedPreferences("a01", Context.MODE_PRIVATE) }
    private val worker = Executors.newSingleThreadExecutor()
    private val selected = linkedSetOf<String>()
    private var lang = "en"
    private var tab = Tab.APPS
    private var category = 0
    private var riskFilter = 0
    private var guideCategory = 0
    private var guideRisk = 0
    private var sort = 0
    private var query = ""
    private var connected = false
    private var busy = false
    private var model = ""
    private var statusText = ""
    private var systemApps = emptySet<String>()
    private var userApps = emptySet<String>()
    private var disabled = emptySet<String>()
    private var labels = emptyMap<String, String>()
    private var ram = emptyMap<String, Long>()
    private var sizes = emptyMap<String, Long>()
    private var sizeRequested = false
    private var adapter: AppsAdapter? = null
    private var uninstallButton: Button? = null
    private var appList: ListView? = null
    private var statusLabel: TextView? = null
    private var guideList: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = prefs.getString("language", "en") ?: "en"
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        if (connected && sort == 1 && !busy) maybeLoadSizes()
    }

    private fun tr(en: String, pt: String): String = if (lang == "pt") pt else en
    private fun Int.dp() = (this * resources.displayMetrics.density + 0.5f).toInt()
    private val ink get() = Color.rgb(34, 48, 59)
    private val accent get() = Color.rgb(25, 106, 120)

    private fun label(value: String, size: Float = 14f, strong: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(ink)
            if (strong) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4.dp(), 5.dp(), 4.dp(), 5.dp())
        }

    private fun button(title: String, onTap: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 13f
        setOnClickListener { onTap() }
    }

    private fun buildUi() {
        adapter = null
        appList = null
        uninstallButton = null
        guideList = null
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(250, 251, 253))
            setPadding(10.dp(), 6.dp(), 10.dp(), 8.dp())
        }
        val title = label("A01 Optimizer", 22f, true)
        root.addView(title)
        statusLabel = label(if (busy) tr("Working…", "Processando…") else if (connected)
            "${model.ifBlank { "Android" }} • ${userApps.size + systemApps.size} ${tr("apps", "apps")}"
        else statusText.ifBlank { tr("Connect in Tools to load apps", "Conecte em Ferramentas para listar os apps") }, 12f)
        root.addView(statusLabel)

        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        when (tab) {
            Tab.APPS -> showApps(content, root)
            Tab.GUIDE -> showGuide(content)
            Tab.TOOLS -> showTools(content)
            Tab.SETTINGS -> showSettings(content)
        }

        val credit = label(tr("Developed by Arthur Henrique Tostes Batista Aranha · 2026",
            "Desenvolvido por Arthur Henrique Tostes Batista Aranha · 2026"), 10f)
        credit.gravity = Gravity.CENTER
        root.addView(credit)

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val tabs = listOf(
            Tab.APPS to tr("Apps", "Apps"),
            Tab.GUIDE to tr("Guide", "Sugestões"),
            Tab.TOOLS to tr("Connect", "Conectar"),
            Tab.SETTINGS to tr("Settings", "Ajustes")
        )
        tabs.forEach { (destination, caption) ->
            val b = button(caption) { tab = destination; buildUi() }
            if (destination == tab) {
                b.backgroundTintList = ColorStateList.valueOf(accent)
                b.setTextColor(Color.WHITE)
            }
            nav.addView(b, LinearLayout.LayoutParams(0, 46.dp(), 1f))
        }
        root.addView(nav)
        setContentView(root)
        updateActions()
    }

    private fun showApps(content: LinearLayout, root: LinearLayout) {
        val search = EditText(this).apply {
            hint = tr("Search apps", "Buscar aplicativos")
            textSize = 15f
            setSingleLine(true)
            setText(query)
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString() ?: ""
                updateList()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        content.addView(search)

        val filters = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val categoryPicker = picker(AppGuide.categoryNames(lang == "pt"), category) {
            category = it; updateList()
        }
        filters.addView(categoryPicker, LinearLayout.LayoutParams(0, 44.dp(), 2f))
        val riskPicker = picker(arrayOf(tr("Any risk", "Todos os riscos"),
            tr("🟢 Green", "🟢 Verde"), tr("🟡 Yellow", "🟡 Amarelo"),
            tr("🔴 Red", "🔴 Vermelho")), riskFilter) {
            riskFilter = it; updateList()
        }
        filters.addView(riskPicker, LinearLayout.LayoutParams(0, 44.dp(), 1.35f))
        content.addView(filters)

        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val sorter = picker(arrayOf(tr("Name A–Z", "Nome A–Z"),
            tr("Size ↓", "Tamanho ↓"), "RAM ↓"), sort) {
            sort = it
            updateList()
            if (sort == 1) maybeLoadSizes()
        }
        controls.addView(sorter, LinearLayout.LayoutParams(0, 42.dp(), 1f))
        controls.addView(button(tr("Refresh", "Atualizar")) { refresh() },
            LinearLayout.LayoutParams(-2, 42.dp()))
        content.addView(controls)

        val list = ListView(this).apply {
            dividerHeight = 1.dp()
            cacheColorHint = Color.TRANSPARENT
        }
        adapter = AppsAdapter()
        list.adapter = adapter
        appList = list
        content.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        updateList()

        val action = button(tr("Uninstall", "Desinstalar")) { confirmUninstall() }
        action.backgroundTintList = ColorStateList.valueOf(Color.rgb(175, 56, 49))
        action.setTextColor(Color.WHITE)
        uninstallButton = action
        root.addView(action, LinearLayout.LayoutParams(-1, 45.dp()))
    }

    private fun picker(values: Array<String>, initial: Int, onSelect: (Int) -> Unit): Spinner =
        Spinner(this).apply {
            val a = ArrayAdapter(this@DashboardActivity,
                android.R.layout.simple_spinner_item, values)
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter = a
            setSelection(initial)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    onSelect(position)
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

    private fun showGuide(content: LinearLayout) {
        content.addView(label(tr("Suggestions", "Sugestões"), 18f, true))
        val filters = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        filters.addView(picker(AppGuide.categoryNames(lang == "pt"), guideCategory) {
            guideCategory = it; updateGuide()
        }, LinearLayout.LayoutParams(0, 44.dp(), 2f))
        filters.addView(picker(arrayOf(tr("All levels", "Todos"),
            tr("🟢 Green", "🟢 Verde"), tr("🟡 Yellow", "🟡 Amarelo"),
            tr("🔴 Red", "🔴 Vermelho")), guideRisk) {
            guideRisk = it; updateGuide()
        }, LinearLayout.LayoutParams(0, 44.dp(), 1.35f))
        content.addView(filters)
        val list = ListView(this).apply { dividerHeight = 1.dp() }
        guideList = list
        content.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        updateGuide()
    }

    private fun updateGuide() {
        val list = guideList ?: return
        val available = if (connected) systemApps + userApps else AppGuide.entries.keys + RiskSuggestions.notes.keys
        val suggestions = AppGuide.reviewedSuggestions(available).filter { id ->
            AppGuide.matches(guideCategory, id, id !in userApps) &&
                (guideRisk == 0 || AppGuide.level(id) == guideRisk - 1)
        }
        val display = suggestions.map { id ->
            val level = AppGuide.level(id)
            val title = AppGuide.name(id, lang == "pt", labels[id] ?: id)
            val tag = AppGuide.levelName(level, lang == "pt").uppercase(Locale.ROOT)
            val symbol = arrayOf("🟢", "🟡", "🔴")[level]
            val keep = if (AppGuide.adviseKeep(id)) tr(" · KEEP", " · MANTER") else ""
            "$symbol $tag$keep · $title\n${AppGuide.description(id, lang == "pt")}" 
        }
        val adapted = ArrayAdapter(this, android.R.layout.simple_list_item_1, display.ifEmpty {
            listOf(tr("No reviewed suggestions here", "Sem sugestões revisadas nesta categoria"))
        })
        list.adapter = adapted
        list.setOnItemClickListener { _, _, position, _ ->
            if (suggestions.isEmpty()) return@setOnItemClickListener
            val id = suggestions[position]
            if (AppGuide.adviseKeep(id)) {
                AlertDialog.Builder(this).setTitle(AppGuide.name(id, lang == "pt", labels[id] ?: id))
                    .setMessage(AppGuide.description(id, lang == "pt") + "\n\n" + tr(
                        "Not recommended for removal. It is still available in Apps.",
                        "Não recomendado para remoção. Continua disponível em Apps."))
                    .setPositiveButton("OK", null).show()
            } else if (!connected) {
                tab = Tab.TOOLS; buildUi()
                toast(tr("Connect to select apps", "Conecte para selecionar apps"))
            } else {
                selected.add(id)
                query = id
                category = AppGuide.ALL
                riskFilter = 0
                tab = Tab.APPS
                buildUi()
            }
        }
    }

    private fun showTools(content: LinearLayout) {
        val port = EditText(this).apply {
            hint = tr("Connection port", "Porta de conexão")
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
            setText(prefs.getString("connection_port", ""))
        }
        content.addView(label(tr("Wireless debugging", "Depuração sem fio"), 18f, true))
        content.addView(button(tr("Pair", "Parear")) {
            try {
                PairingReceiver.prepare(this)
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (e: Exception) { toast(e.message ?: tr("Could not pair", "Não foi possível parear")) }
        })
        content.addView(port)
        content.addView(button(tr("Connect", "Conectar")) {
            val p = port.text.toString().trim().toIntOrNull()
            if (p == null || p !in 1..65535) {
                toast(tr("Enter the connection port", "Informe a porta de conexão")); return@button
            }
            prefs.edit().putString("connection_port", p.toString()).apply()
            scan(p)
        })
        content.addView(label(tr("Use the connection port, not the pairing port.",
            "Use a porta de conexão, não a de pareamento."), 12f))
        content.addView(label(tr("Recovery", "Restauração"), 18f, true))
        content.addView(button(tr("Restore removed system apps", "Restaurar apps de fábrica")) {
            confirmRestore()
        })
        content.addView(label(tr("Only changes made by this version can be restored here.",
            "Somente alterações desta versão podem ser restauradas aqui."), 12f))
    }

    private fun showSettings(content: LinearLayout) {
        content.addView(label(tr("Language", "Idioma"), 18f, true))
        content.addView(button("English" + if (lang == "en") " ✓" else "") {
            setLanguage("en")
        })
        content.addView(button("Português" + if (lang == "pt") " ✓" else "") {
            setLanguage("pt")
        })
        content.addView(label(tr("Storage access", "Acesso ao armazenamento"), 18f, true))
        val granted = PackageMetrics.hasUsageAccess(this)
        content.addView(button(if (granted) tr("Access granted", "Acesso autorizado")
            else tr("Allow usage access", "Permitir acesso a dados de uso")) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        })
        content.addView(label(tr("Required to sort by full app size.",
            "Necessário para ordenar pelo tamanho completo."), 12f))
        content.addView(label("A01 Optimizer v0.7", 15f, true))
    }

    private fun setLanguage(code: String) {
        lang = code
        prefs.edit().putString("language", code).apply()
        buildUi()
    }

    private fun updateActions() {
        uninstallButton?.apply {
            text = tr("Uninstall", "Desinstalar") + " (${selected.size})"
            isEnabled = !busy && connected && selected.isNotEmpty()
        }
        statusLabel?.text = if (busy) tr("Working…", "Processando…")
            else if (connected) "$model · ${systemApps.size + userApps.size} ${tr("apps", "apps")}"
            else statusText.ifBlank { tr("Connect in Tools to load apps", "Conecte em Ferramentas para listar os apps") }
    }

    private fun updateList() {
        val all = systemApps + userApps
        val term = query.trim().lowercase(Locale.ROOT)
        val matches = all.filter { name ->
            AppGuide.matches(category, name, name in systemApps) &&
                (riskFilter == 0 || AppGuide.level(name) == riskFilter - 1) &&
                (term.isEmpty() || name.lowercase(Locale.ROOT).contains(term) ||
                    AppGuide.name(name, lang == "pt", labels[name] ?: name)
                        .lowercase(Locale.ROOT).contains(term))
        }
        val ordered = when (sort) {
            1 -> matches.sortedWith(compareByDescending<String> { sizes[it] ?: -1L }
                .thenBy { AppGuide.name(it, lang == "pt", labels[it] ?: it).lowercase(Locale.ROOT) })
            2 -> matches.sortedWith(compareByDescending<String> { ram[it] ?: -1L }
                .thenBy { AppGuide.name(it, lang == "pt", labels[it] ?: it).lowercase(Locale.ROOT) })
            else -> matches.sortedBy { AppGuide.name(it, lang == "pt", labels[it] ?: it).lowercase(Locale.ROOT) }
        }
        adapter?.items = ordered
        adapter?.notifyDataSetChanged()
        updateActions()
    }

    private inner class AppsAdapter : BaseAdapter() {
        var items = emptyList<String>()
        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val id = items[position]
            val checkbox = convertView as? CheckBox ?: CheckBox(this@DashboardActivity).apply {
                textSize = 14f
                setTextColor(ink)
                setPadding(9.dp(), 5.dp(), 8.dp(), 5.dp())
                minHeight = 58.dp()
            }
            val kind = if (id in systemApps) tr("system", "sistema") else tr("installed", "instalado")
            val size = PackageMetrics.bytesLabel(sizes[id])
            val memory = PackageMetrics.ramLabel(ram[id])
            val risk = AppGuide.level(id)
            val flag = arrayOf("🟢", "🟡", "🔴")[risk]
            val group = AppGuide.categoryNames(lang == "pt")[AppGuide.category(id)]
            checkbox.setOnCheckedChangeListener(null)
            checkbox.text = "${AppGuide.name(id, lang == "pt", labels[id] ?: id)}  $flag\n" +
                "$group · $kind · $size · RAM $memory\n$id"
            checkbox.isChecked = id in selected
            checkbox.setOnLongClickListener {
                AlertDialog.Builder(this@DashboardActivity)
                    .setTitle(AppGuide.name(id, lang == "pt", labels[id] ?: id))
                    .setMessage("$id\n\n" + AppGuide.description(id, lang == "pt") +
                        "\n\n" + tr("Risk", "Risco") + ": " + AppGuide.levelName(risk, lang == "pt"))
                    .setPositiveButton("OK", null).show()
                true
            }
            checkbox.setOnCheckedChangeListener { _, checked ->
                if (checked) selected.add(id) else selected.remove(id)
                updateActions()
            }
            return checkbox
        }
    }

    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()

    private fun isValidId(id: String): Boolean = id == "android" ||
        id.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))

    private fun parsePackages(text: String): Set<String> = text.lineSequence()
        .map { it.trim() }.filter { it.startsWith("package:") }
        .map { it.removePrefix("package:") }.filter(::isValidId).toSet()

    private fun port(): Int = prefs.getString("connection_port", "")?.toIntOrNull()
        ?.takeIf { it in 1..65535 } ?: error(tr("Connect in Tools first", "Conecte em Ferramentas primeiro"))

    private fun task(message: String, work: (Int) -> Unit) {
        if (busy) return
        val p = try { port() } catch (e: Exception) { toast(e.message ?: message); return }
        busy = true
        statusText = message
        updateActions()
        worker.execute {
            try { work(p) }
            catch (e: Exception) {
                runOnUiThread {
                    AlertDialog.Builder(this).setTitle(tr("Operation failed", "Operação não concluída"))
                        .setMessage((e.message ?: "ADB disconnected").take(400))
                        .setPositiveButton("OK", null).show()
                }
            } finally {
                runOnUiThread {
                    busy = false
                    updateActions()
                    if (tab == Tab.APPS && sort == 1 && connected) maybeLoadSizes()
                }
            }
        }
    }

    private fun scan(p: Int) {
        prefs.edit().putString("connection_port", p.toString()).apply()
        task(tr("Connecting…", "Conectando…")) { connection ->
            val currentModel = AdbCore.command(this, connection, "getprop ro.product.model").trim()
            val systems = parsePackages(AdbCore.command(this, connection, "pm list packages -s --user 0"))
            val users = parsePackages(AdbCore.command(this, connection, "pm list packages -3 --user 0"))
            val disabledNow = parsePackages(AdbCore.command(this, connection, "pm list packages -d --user 0"))
            val names = (systems + users).associateWith { id ->
                try {
                    val info = packageManager.getApplicationInfo(id, 0)
                    packageManager.getApplicationLabel(info).toString().ifBlank { id }
                } catch (_: Exception) { id }
            }
            val currentRam = try {
                PackageMetrics.ramPssKb(AdbCore.command(this, connection, "dumpsys meminfo"), systems + users)
            } catch (_: Exception) { emptyMap() }
            runOnUiThread {
                model = currentModel
                connected = true
                systemApps = systems
                userApps = users
                disabled = disabledNow
                labels = names
                ram = currentRam
                sizes = emptyMap()
                sizeRequested = false
                selected.retainAll(systems + users)
                tab = Tab.APPS
                buildUi()
            }
        }
    }

    private fun refresh() {
        if (!connected) { tab = Tab.TOOLS; buildUi(); return }
        scan(port())
    }

    private fun maybeLoadSizes() {
        if (sort != 1 || !connected || sizeRequested || !PackageMetrics.hasUsageAccess(this)) return
        sizeRequested = true
        task(tr("Reading storage…", "Lendo armazenamento…")) { _ ->
            val result = PackageMetrics.storageBytes(this, systemApps + userApps)
            runOnUiThread { sizes = result; updateList() }
        }
    }

    private fun removed(): MutableSet<String> = prefs.getStringSet("factory_removed", emptySet())
        ?.toMutableSet() ?: mutableSetOf()
    private fun wasDisabled(): MutableSet<String> = prefs.getStringSet("factory_was_disabled", emptySet())
        ?.toMutableSet() ?: mutableSetOf()
    private fun saveHistory(items: Set<String>, originalDisabled: Set<String>) {
        prefs.edit().putStringSet("factory_removed", items.toSet())
            .putStringSet("factory_was_disabled", originalDisabled.toSet()).commit()
    }

    private fun confirmUninstall() {
        val targets = selected.filter { it in systemApps || it in userApps }.filter(::isValidId)
        if (targets.isEmpty() || busy) return
        val systemCount = targets.count { it in systemApps }
        val preview = targets.take(7).joinToString("\n") { labels[it] ?: it }
        val more = if (targets.size > 7) "\n+${targets.size - 7}" else ""
        val warning = if (systemCount > 0)
            tr("\n\nWarning: removing system components can break Android, banking apps and ADB. Recovery is not guaranteed.",
                "\n\nAtenção: remover componentes do sistema pode quebrar o Android, bancos e ADB. A recuperação não é garantida.")
        else tr("\n\nInstalled apps may lose their data.", "\n\nApps instalados podem perder dados.")
        AlertDialog.Builder(this).setTitle(tr("Uninstall ${targets.size} apps?", "Desinstalar ${targets.size} apps?"))
            .setMessage(preview + more + warning)
            .setNegativeButton(tr("Cancel", "Cancelar"), null)
            .setPositiveButton(tr("Uninstall", "Desinstalar")) { _, _ -> uninstall(targets) }.show()
    }

    private fun uninstall(targets: List<String>) {
        task(tr("Uninstalling…", "Desinstalando…")) { p ->
            var successes = 0
            var failures = 0
            for (id in targets) {
                if (!isValidId(id)) continue
                val system = id in systemApps
                if (!system && id !in userApps) continue
                if (system) {
                    val history = removed()
                    val previous = wasDisabled()
                    history.add(id)
                    if (id in disabled) previous.add(id) else previous.remove(id)
                    saveHistory(history, previous)
                }
                try {
                    val command = if (system) "pm uninstall -k --user 0 $id"
                        else "pm uninstall --user 0 $id"
                    val response = AdbCore.command(this, p, command)
                    if (!response.trim().startsWith("Success", ignoreCase = true)) error(response.take(160))
                    successes++
                } catch (_: Exception) {
                    if (system) {
                        val stillInstalled = try {
                            id in parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
                        } catch (_: Exception) { false }
                        if (stillInstalled) {
                            val history = removed(); val previous = wasDisabled()
                            history.remove(id); previous.remove(id)
                            saveHistory(history, previous)
                        }
                    }
                    failures++
                }
            }
            runOnUiThread {
                selected.clear()
                toast(tr("Removed: $successes · Failed: $failures", "Removidos: $successes · Falhas: $failures"))
            }
            val systems = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
            val users = parsePackages(AdbCore.command(this, p, "pm list packages -3 --user 0"))
            runOnUiThread {
                systemApps = systems
                userApps = users
                buildUi()
            }
        }
    }

    private fun confirmRestore() {
        val targets = removed().filter(::isValidId)
        if (targets.isEmpty()) {
            toast(tr("Nothing to restore", "Nada para restaurar"))
            return
        }
        AlertDialog.Builder(this).setTitle(tr("Restore ${targets.size} system apps?",
            "Restaurar ${targets.size} apps de fábrica?"))
            .setNegativeButton(tr("Cancel", "Cancelar"), null)
            .setPositiveButton(tr("Restore", "Restaurar")) { _, _ -> restore(targets) }.show()
    }

    private fun restore(targets: List<String>) {
        task(tr("Restoring…", "Restaurando…")) { p ->
            var successes = 0
            for (id in targets) {
                try {
                    AdbCore.command(this, p, "cmd package install-existing --user 0 $id")
                    if (id in wasDisabled()) AdbCore.command(this, p, "pm disable-user --user 0 $id")
                    val present = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
                    if (id !in present) error("Not installed")
                    val history = removed(); val previous = wasDisabled()
                    history.remove(id); previous.remove(id)
                    saveHistory(history, previous)
                    successes++
                } catch (_: Exception) { }
            }
            runOnUiThread { toast(tr("Restored: $successes", "Restaurados: $successes")) }
            scanAfterRestore(p)
        }
    }

    private fun scanAfterRestore(p: Int) {
        val systems = parsePackages(AdbCore.command(this, p, "pm list packages -s --user 0"))
        val users = parsePackages(AdbCore.command(this, p, "pm list packages -3 --user 0"))
        runOnUiThread {
            systemApps = systems
            userApps = users
            buildUi()
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
