package br.com.a01optimizer

import android.content.Context
import com.flyfishxu.kadb.Kadb
import com.flyfishxu.kadb.cert.KadbCert
import com.flyfishxu.kadb.cert.KadbCertPolicy
import com.flyfishxu.kadb.cert.OkioFilePrivateKeyStore
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.io.File

object AdbCore {
    private var prepared = false
    @Synchronized private fun prepare(context: Context) {
        if (prepared) return
        val key = File(context.filesDir, "adb_host_private.pem")
        KadbCert.configure(
            store = OkioFilePrivateKeyStore(privateKeyPath = key.absolutePath.toPath()),
            policy = KadbCertPolicy(),
            additionalPrivateKeysPem = emptyList()
        )
        KadbCert.ensureReady()
        prepared = true
    }

    @Synchronized fun pair(context: Context, port: Int, pin: String) {
        require(port in 1..65535) { "Porta de pareamento inválida" }
        require(pin.matches(Regex("[0-9]{6}"))) { "O código precisa ter seis dígitos" }
        prepare(context.applicationContext)
        runBlocking { Kadb.pair("127.0.0.1", port, pin) }
    }

    @Synchronized fun command(context: Context, port: Int, cmd: String): String {
        require(port in 1..65535) { "Porta ADB inválida" }
        prepare(context.applicationContext)
        Kadb.create("127.0.0.1", port).use { adb ->
            val response = adb.shell(cmd)
            if (response.exitCode != 0) {
                error("ADB falhou (código " + response.exitCode + "): " + response.output.take(200))
            }
            return response.output
        }
    }
}
