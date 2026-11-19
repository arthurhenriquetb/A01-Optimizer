package br.com.a01optimizer

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicBoolean

class AdbDiscovery(context: Context) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val nsd = app.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifi = app.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val active = AtomicBoolean(false)
    private var listener: NsdManager.DiscoveryListener? = null
    private var multicast: WifiManager.MulticastLock? = null
    private var timeoutAction: Runnable? = null
    private var callback: ((Int?) -> Unit)? = null

    fun discover(pairing: Boolean = false, timeoutMs: Long = 9000L, result: (Int?) -> Unit) {
        if (!active.compareAndSet(false, true)) return
        callback = result
        val type = if (pairing) "_adb-tls-pairing._tcp." else "_adb-tls-connect._tcp."
        try {
            val lock = wifi.createMulticastLock("a01-adb-discovery")
            lock.setReferenceCounted(false)
            lock.acquire()
            multicast = lock
            val receiver = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) = Unit
                override fun onDiscoveryStopped(serviceType: String) = Unit
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) = finish(null)
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
                override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit
                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    if (!active.get() || !serviceInfo.serviceType.contains("_adb-tls-")) return
                    try {
                        nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
                            override fun onServiceResolved(info: NsdServiceInfo) {
                                if (info.port in 1..65535 && isOwnAddress(info.host)) finish(info.port)
                            }
                        })
                    } catch (_: Exception) { }
                }
            }
            listener = receiver
            val timeout = Runnable { finish(null) }
            timeoutAction = timeout
            main.postDelayed(timeout, timeoutMs)
            nsd.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, receiver)
        } catch (_: Exception) { finish(null) }
    }

    private fun isOwnAddress(address: InetAddress?): Boolean {
        if (address == null) return false
        if (address.isLoopbackAddress) return true
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val device = interfaces.nextElement()
                val ips = device.inetAddresses
                while (ips.hasMoreElements()) {
                    if (address.address.contentEquals(ips.nextElement().address)) return true
                }
            }
            false
        } catch (_: Exception) { false }
    }

    private fun finish(port: Int?) {
        if (!active.compareAndSet(true, false)) return
        main.post {
            timeoutAction?.let(main::removeCallbacks)
            timeoutAction = null
            listener?.let { try { nsd.stopServiceDiscovery(it) } catch (_: Exception) { } }
            listener = null
            multicast?.let { try { if (it.isHeld) it.release() } catch (_: Exception) { } }
            multicast = null
            callback?.invoke(port)
            callback = null
        }
    }

    fun cancel() { finish(null) }
}
