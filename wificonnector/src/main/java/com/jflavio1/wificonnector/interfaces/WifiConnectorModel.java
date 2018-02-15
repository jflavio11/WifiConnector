/*
 * Created by Jose Flavio on 2/5/18 6:14 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnector.interfaces;

import android.net.wifi.ScanResult;

/**
 * WifiConnectorModel
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since 5/2/17
 */
public interface WifiConnectorModel {

    void createWifiConnectorObject();

    void scanForWifiNetworks();

    void connectToWifiAccessPoint(ScanResult scanResult, String password);

    void disconnectFromAccessPoint(ScanResult scanResult);

    void destroyWifiConnectorListeners();

}
