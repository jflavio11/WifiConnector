/*
 * Created by Jose Flavio on 2/12/21 9:13 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnector.model

/**
 * WiFiSecurityType
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since  12/02/2021
 */
sealed class WiFiSecurityType {
    object NONE : WiFiSecurityType()
    object WEP : WiFiSecurityType()
    object WPA1 : WiFiSecurityType()
    object WPA2 : WiFiSecurityType()
    object WPA2_PSK : WiFiSecurityType()

    /**
     * WPA3 Simultaneous Authentication of Equals network.
     */
    object WPA3_SAE : WiFiSecurityType()

    /**
     * Enterprise WPA2 network. Not supported yet.
     */
    private object WPA2_EAP : WiFiSecurityType()

    /**
     * Not supported yet.
     */
    private object OWE : WiFiSecurityType()
}