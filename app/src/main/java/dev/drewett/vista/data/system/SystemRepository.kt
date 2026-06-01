package dev.drewett.vista.data.system

import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager

data class BtDevice(val name: String, val connected: Boolean)

/** Reads Wi-Fi and Bluetooth state for the quick-settings panel. */
class SystemRepository(private val context: Context) {

    /** The connected Wi-Fi SSID, or null. Needs ACCESS_FINE_LOCATION + location enabled. */
    fun wifiSsid(): String? {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        @Suppress("DEPRECATION")
        val ssid = runCatching { wifi.connectionInfo?.ssid }.getOrNull()?.trim('"')
        return ssid?.takeIf { it.isNotBlank() && it != "<unknown ssid>" && it != "0x" }
    }

    /** Bonded Bluetooth devices, connected ones first. Needs BLUETOOTH_CONNECT. */
    fun bluetoothDevices(): List<BtDevice> {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager ?: return emptyList()
        val adapter = manager.adapter ?: return emptyList()
        return runCatching {
            adapter.bondedDevices.map { device ->
                val connected = runCatching {
                    device.javaClass.getMethod("isConnected").invoke(device) as Boolean
                }.getOrDefault(false)
                BtDevice(name = device.name ?: device.address, connected = connected)
            }.sortedByDescending { it.connected }
        }.getOrDefault(emptyList())
    }
}
