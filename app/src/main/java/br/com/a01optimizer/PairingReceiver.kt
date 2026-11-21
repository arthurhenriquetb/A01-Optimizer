package br.com.a01optimizer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val connectionStage = intent.getBooleanExtra("connection_stage", false)
        val response = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(if (connectionStage) "connection_port" else "code")
            ?.toString()?.trim().orEmpty()
        Thread {
            try {
                if (connectionStage) {
                    val port = response.toIntOrNull()
                    require(port != null && port in 1..65535) {
                        translated(context,
                            "Enter the connection port from the main Wireless debugging screen.",
                            "Informe a porta de conexão exibida na tela principal de Depuração sem fio.")
                    }
                    context.getSharedPreferences("a01", Context.MODE_PRIVATE)
                        .edit().putString("connection_port", port.toString()).apply()
                    val model = AdbCore.command(context, port, "getprop ro.product.model").trim()
                    result(context, translated(context, "ADB connected", "ADB conectado"),
                        translated(context, "Connected to $model. Open DebloaToast.",
                            "Conectado a $model. Abra o DebloaToast."))
                    return@Thread
                }
                val values = response.split(Regex("\\s+"))
                require(values.size == 1 || values.size == 2) {
                    translated(context, "Enter CODE or PORT CODE", "Informe CÓDIGO ou PORTA CÓDIGO")
                }
                val code = values.last()
                val port = if (values.size == 2) values.first().toIntOrNull() else {
                    val latch = CountDownLatch(1)
                    var found: Int? = null
                    val discovery = AdbDiscovery(context)
                    discovery.discover(pairing = true) { p -> found = p; latch.countDown() }
                    latch.await(11, TimeUnit.SECONDS)
                    discovery.cancel()
                    found
                }
                require(port != null && port in 1..65535) {
                    translated(context, "Pairing port not found. Reply with PORT CODE.",
                        "Porta não encontrada. Responda com PORTA CÓDIGO.")
                }
                AdbCore.pair(context, port, code)
                context.getSharedPreferences("a01", Context.MODE_PRIVATE)
                    .edit().putBoolean("paired", true).apply()
                requestConnectionPort(context)
            } catch (ex: Exception) {
                if (connectionStage) {
                    requestConnectionPort(context)
                    manager(context).notify(RESULT_NOTIFICATION, Notification.Builder(context, CHANNEL)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle(translated(context, "Connection not ready", "Conexão não concluída"))
                        .setContentText((ex.message ?: translated(context, "Check the connection port", "Confira a porta de conexão")).take(120))
                        .setAutoCancel(true).build())
                } else {
                    result(context, translated(context, "Pairing failed", "Falha no pareamento"),
                        ex.message ?: translated(context, "Check port and code", "Confira porta e código"))
                }
            } finally { pending.finish() }
        }.start()
    }

    companion object {
        private const val CHANNEL = "adb_pairing"
        private const val PAIR_NOTIFICATION = 51
        private const val RESULT_NOTIFICATION = 52

        private fun manager(ctx: Context) =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        private fun translated(ctx: Context, en: String, pt: String): String =
            if (ctx.getSharedPreferences("a01", Context.MODE_PRIVATE)
                    .getString("language", "en") == "pt") pt else en

        fun prepare(ctx: Context) {
            manager(ctx).createNotificationChannel(NotificationChannel(CHANNEL,
                translated(ctx, "ADB pairing", "Pareamento ADB"), NotificationManager.IMPORTANCE_HIGH))
            val pending = PendingIntent.getBroadcast(ctx, 0,
                Intent(ctx, PairingReceiver::class.java),
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val reply = RemoteInput.Builder("code")
                .setLabel(translated(ctx, "CODE", "CÓDIGO")).build()
            val action = Notification.Action.Builder(android.R.drawable.ic_menu_edit,
                translated(ctx, "Enter pairing code", "Informar código de pareamento"), pending)
                .addRemoteInput(reply).build()
            manager(ctx).notify(PAIR_NOTIFICATION, Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(translated(ctx, "Pair DebloaToast", "Parear DebloaToast"))
                .setContentText(translated(ctx, "Reply with CODE (or PORT CODE if needed).",
                    "Responda com CÓDIGO (ou PORTA CÓDIGO)."))
                .addAction(action)
                .setOngoing(true)
                .build())
        }

        private fun requestConnectionPort(ctx: Context) {
            val pending = PendingIntent.getBroadcast(ctx, 1,
                Intent(ctx, PairingReceiver::class.java).putExtra("connection_stage", true),
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val reply = RemoteInput.Builder("connection_port")
                .setLabel(translated(ctx, "CONNECTION PORT", "PORTA DE CONEXÃO"))
                .build()
            val action = Notification.Action.Builder(android.R.drawable.ic_menu_edit,
                translated(ctx, "Enter connection port", "Informar porta de conexão"), pending)
                .addRemoteInput(reply).build()
            val open = PendingIntent.getActivity(ctx, 0,
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            manager(ctx).notify(PAIR_NOTIFICATION, Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(translated(ctx, "Paired! One last step", "Pareado! Falta uma etapa"))
                .setContentText(translated(ctx,
                    "Wireless debugging → IP address & Port. Reply with the port after ':'",
                    "Depuração sem fio → Endereço IP e porta. Digite o número após ':'"))
                .setStyle(Notification.BigTextStyle().bigText(translated(ctx,
                    "Open Developer options → Wireless debugging. On the main screen, find IP address & Port (example: 192.168.1.5:37123). Reply with 37123. This is NOT the pairing port.",
                    "Abra Opções do desenvolvedor → Depuração sem fio. Na tela principal, procure Endereço IP e porta (ex.: 192.168.1.5:37123). Responda 37123. NÃO é a porta de pareamento.")))
                .setContentIntent(open)
                .addAction(action)
                .setOngoing(true)
                .build())
        }

        private fun result(ctx: Context, title: String, message: String) {
            manager(ctx).cancel(PAIR_NOTIFICATION)
            val open = PendingIntent.getActivity(ctx, 0,
                Intent(ctx, DashboardActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            manager(ctx).notify(RESULT_NOTIFICATION, Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(open)
                .setAutoCancel(true)
                .build())
        }
    }
}
