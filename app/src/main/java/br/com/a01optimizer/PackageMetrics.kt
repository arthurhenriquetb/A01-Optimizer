package br.com.a01optimizer

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Process
import android.os.storage.StorageManager
import java.util.Locale

object PackageMetrics {
    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return ops.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    /** Tamanho aproximado do aplicativo + dados; requer Acesso a dados de uso.
     * Deve ser executado na thread de trabalho, nunca na interface.
     */
    fun storageBytes(context: Context, names: Set<String>): Map<String, Long> {
        if (!hasUsageAccess(context)) return emptyMap()
        val service = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val result = mutableMapOf<String, Long>()
        for (name in names) {
            try {
                val stats = service.queryStatsForPackage(
                    StorageManager.UUID_DEFAULT, name, Process.myUserHandle()
                )
                result[name] = stats.appBytes + stats.dataBytes
            } catch (_: Exception) { /* Pacote sem métricas disponíveis. */ }
        }
        return result
    }

    /** Soma o PSS de processos ativos atribuíveis aos pacotes conhecidos.
     * É uma fotografia, não média histórica nem RAM garantidamente liberada.
     */
    fun ramPssKb(meminfo: String, names: Set<String>): Map<String, Long> {
        val totals = mutableMapOf<String, Long>()
        val known = names.sortedByDescending { it.length }
        val row = Regex("""^\s*([\d,]+)\s*(?:K|kB|KB):\s+([^\s(]+)""")
        var inSection = false
        for (line in meminfo.lineSequence()) {
            if (line.contains("Total PSS by process:", ignoreCase = true)) {
                inSection = true
                continue
            }
            if (!inSection) continue
            if (line.trim().startsWith("Total PSS by ", ignoreCase = true)) break
            val match = row.find(line) ?: continue
            val kb = match.groupValues[1].replace(",", "").toLongOrNull() ?: continue
            val process = match.groupValues[2]
            val owner = known.firstOrNull { packageName ->
                process == packageName || process.startsWith("$packageName:") ||
                    process.startsWith("$packageName.")
            } ?: continue
            totals[owner] = (totals[owner] ?: 0L) + kb
        }
        return totals
    }

    fun bytesLabel(bytes: Long?): String = when {
        bytes == null -> "—"
        bytes < 1024 -> "$bytes B"
        bytes < 1024L * 1024 -> String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0)
    }

    fun ramLabel(kb: Long?): String = when {
        kb == null -> "—"
        kb < 1024 -> "$kb KB"
        else -> String.format(Locale.ROOT, "%.1f MB", kb / 1024.0)
    }
}
