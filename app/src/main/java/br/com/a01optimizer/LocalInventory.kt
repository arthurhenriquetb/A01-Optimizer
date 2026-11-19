package br.com.a01optimizer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build

data class LocalApps(
    val system: Set<String>,
    val installed: Set<String>,
    val names: Map<String, String>,
    val disabled: Set<String>,
    val device: String
)

/** Lê a lista real do próprio telefone, sem precisar de ADB nem da marca.
 * Acesso aos pacotes segue as regras de visibilidade do Android.
 */
object LocalInventory {
    fun read(context: Context): LocalApps {
        val manager = context.packageManager
        val system = linkedSetOf<String>()
        val user = linkedSetOf<String>()
        val names = mutableMapOf<String, String>()
        val disabled = linkedSetOf<String>()
        for (item in manager.getInstalledPackages(0)) {
            val info = item.applicationInfo ?: continue
            val id = item.packageName
            if (id.isBlank()) continue
            val isSystem = info.flags and (ApplicationInfo.FLAG_SYSTEM or
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (isSystem) system.add(id) else user.add(id)
            names[id] = try {
                manager.getApplicationLabel(info).toString().ifBlank { id }
            } catch (_: Exception) { id }
            if (!info.enabled) disabled.add(id)
        }
        return LocalApps(system, user, names, disabled,
            "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}")
    }
}
