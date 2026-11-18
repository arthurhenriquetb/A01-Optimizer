package br.com.a01optimizer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Resposta rápida para parear sem fechar as Configurações. */
class PairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val answer = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence("code")?.toString()?.trim().orEmpty()
        Thread {
            try {
                val values = answer.split(Regex("\\s+"))
                require(values.size == 2) { "Digite PORTA CÓDIGO, separados por espaço" }
                val port = values[0].toIntOrNull() ?: error("Porta inválida")
                AdbCore.pair(context, port, values[1])
                context.getSharedPreferences("a01", Context.MODE_PRIVATE)
                    .edit().putBoolean("paired", true).apply()
                result(context, "Pareamento concluído", "Volte ao A01 Optimizer para conectar.")
            } catch (ex: Exception) {
                result(context, "Pareamento não concluído", ex.message ?: "Confira porta e código")
            } finally { pending.finish() }
        }.start()
    }

    companion object {
        private const val CHANNEL = "adb_pairing"
        private const val PAIR_NOTIFICATION = 51
        private const val RESULT_NOTIFICATION = 52
        private fun manager(ctx: Context) =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun prepare(ctx: Context) {
            manager(ctx).createNotificationChannel(
                NotificationChannel(CHANNEL, "Pareamento ADB", NotificationManager.IMPORTANCE_HIGH)
            )
            val intent = Intent(ctx, PairingReceiver::class.java)
            val pending = PendingIntent.getBroadcast(ctx, 0, intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val reply = RemoteInput.Builder("code")
                .setLabel("PORTA CÓDIGO").build()
            val action = Notification.Action.Builder(
                android.R.drawable.ic_menu_edit, "Informar porta e código", pending
            ).addRemoteInput(reply).build()
            manager(ctx).notify(PAIR_NOTIFICATION,
                Notification.Builder(ctx, CHANNEL)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setContentTitle("Parear A01 Optimizer")
                    .setContentText("Responda com PORTA CÓDIGO usando os números das Configurações.")
                    .addAction(action)
                    .setOngoing(true)
                    .build())
        }

        private fun result(ctx: Context, title: String, message: String) {
            manager(ctx).cancel(PAIR_NOTIFICATION)
            val intent = PendingIntent.getActivity(ctx, 0,
                Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            manager(ctx).notify(RESULT_NOTIFICATION,
                Notification.Builder(ctx, CHANNEL)
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .setAutoCancel(true)
                    .build())
        }
    }
}
