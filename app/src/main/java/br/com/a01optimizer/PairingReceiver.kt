package br.com.a01optimizer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val response = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence("code")?.toString()?.trim().orEmpty()
        Thread {
            try {
                val values = response.split(Regex("\\s+"))
                require(values.size == 2) { translated(context, "Enter PORT CODE", "Informe PORTA CÓDIGO") }
                val port = values[0].toIntOrNull() ?: error(translated(context, "Invalid port", "Porta inválida"))
                AdbCore.pair(context, port, values[1])
                context.getSharedPreferences("a01", Context.MODE_PRIVATE)
                    .edit().putBoolean("paired", true).apply()
                result(context, translated(context, "Paired", "Pareado"),
                    translated(context, "Return to A01 Optimizer and connect.",
                        "Volte ao A01 Optimizer e conecte."))
            } catch (ex: Exception) {
                result(context, translated(context, "Pairing failed", "Falha no pareamento"),
                    ex.message ?: translated(context, "Check port and code", "Confira porta e código"))
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
                .setLabel(translated(ctx, "PORT CODE", "PORTA CÓDIGO")).build()
            val action = Notification.Action.Builder(android.R.drawable.ic_menu_edit,
                translated(ctx, "Enter port and code", "Informar porta e código"), pending)
                .addRemoteInput(reply).build()
            manager(ctx).notify(PAIR_NOTIFICATION, Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(translated(ctx, "Pair A01 Optimizer", "Parear A01 Optimizer"))
                .setContentText(translated(ctx, "Reply with PORT CODE from Android settings.",
                    "Responda com PORTA CÓDIGO das configurações."))
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
