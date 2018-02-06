/*
 * Created by Jose Flavio on 10/18/17 1:20 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnectorsample;

import android.net.wifi.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jflavio1.wificonnector.WifiConnector;
import com.jflavio1.wificonnector.interfaces.WifiConnectorModel;

public class MainActivity extends AppCompatActivity implements WifiConnectorModel {

    private WifiConnector wifiConnector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void createWifiConnectorObject() {
        wifiConnector = new WifiConnector(this);
    }

    @Override
    public void scanForWifiNetworks() {

    }

    @Override
    public void connectToWifiAccessPoint(ScanResult scanResult) {

    }

    @Override
    public void disconnectFromAccessPoint() {

    }
}
