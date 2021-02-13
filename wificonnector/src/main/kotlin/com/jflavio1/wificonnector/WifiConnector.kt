package com.jflavio1.wificonnector

import android.annotation.SuppressLint
import android.content.Context
import android.net.MacAddress
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import com.jflavio1.wificonnector.model.WiFiHotSpot
import com.jflavio1.wificonnector.model.WiFiSecurityType

/**
 * WifiConnector
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since  12/02/2021
 */
class WifiConnector2(private val context: Context) {

    private var wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // TODO for less than Q
    fun connectToWiFiHotSpot(wiFiHotSpot: WiFiHotSpot) {
        var networkId = getNetworkId(wiFiHotSpot.SSID)
        println("Network found with id: $networkId")
        if (networkId == -1) {
            networkId = wifiManager.addNetwork(buildNetworkConfiguration(wiFiHotSpot))
            println("Network was new. So now, the id is: $networkId")
        }
        if (networkId == -1) {
            println("Network id still -1... wtf?")
        }
        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
    }

    @SuppressLint("MissingPermission") // TODO add permission check by result
    private fun getNetworkId(SSID: String): Int {
        val configuredNetworks = wifiManager.configuredNetworks
        for (existingConfig in configuredNetworks) {
            if (trimQuotes(existingConfig.SSID) == trimQuotes(SSID)) {
                return existingConfig.networkId
            }
        }
        return -1
    }

    // TODO move to a helper
    private fun trimQuotes(str: String): String? {
        return if (str.isNotEmpty()) {
            str.replace("^\"*".toRegex(), "").replace("\"*$".toRegex(), "")
        } else str
    }

    @android.support.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun buildNetworkConfigurationForQ(wiFiHotSpot: WiFiHotSpot): WifiNetworkSpecifier {
        return WifiNetworkSpecifier.Builder().let {
            it.setSsid(wiFiHotSpot.SSID)
            it.setBssid(MacAddress.fromString(wiFiHotSpot.BSSID))
            it.setIsHiddenSsid(wiFiHotSpot.isHiddenNetwork)
            when (wiFiHotSpot.securityType) {
                is WiFiSecurityType.NONE -> Unit
                is WiFiSecurityType.WEP -> Unit // do not supported by WifiNetworkSpecifier
                is WiFiSecurityType.WPA2_PSK -> it.setWpa2Passphrase(wiFiHotSpot.password.orEmpty())
                is WiFiSecurityType.WPA3_SAE -> it.setWpa3Passphrase(wiFiHotSpot.password.orEmpty())
                else -> Unit
            }
            it.build()
        }
    }

    private fun buildNetworkConfiguration(wiFiHotSpot: WiFiHotSpot): WifiConfiguration {
        return WifiConfiguration().apply {
            this.SSID = wiFiHotSpot.SSID
            this.BSSID = wiFiHotSpot.BSSID
            this.hiddenSSID = wiFiHotSpot.isHiddenNetwork
            this.preSharedKey = "\"${wiFiHotSpot.password}\""
            when (wiFiHotSpot.securityType) {
                is WiFiSecurityType.NONE -> Unit
                is WiFiSecurityType.WEP -> Unit
                is WiFiSecurityType.WPA1 -> {
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                    allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                }
                is WiFiSecurityType.WPA2 -> {
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                }
                is WiFiSecurityType.WPA2_PSK -> {
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                }
                is WiFiSecurityType.WPA3_SAE -> Unit
                else -> Unit
            }
        }
    }

}