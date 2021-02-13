/*
 * Created by Jose Flavio on 2/12/21 9:13 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnector.model

/**
 * WiFiHotSpot
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since  12/02/2021
 */
data class WiFiHotSpot(
        val SSID: String,
        val BSSID: String = "",
        val password: String? = null,
        val securityType: WiFiSecurityType = WiFiSecurityType.NONE,
        val isHiddenNetwork: Boolean = false
)