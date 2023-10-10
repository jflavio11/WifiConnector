/*
 * Created by Jose Flavio on 10/18/17 12:49 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */
package com.jflavio1.wificonnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * WifiConnectionReceiver
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since 18/10/17
 */
class WifiConnectionReceiver extends BroadcastReceiver {

    private WifiConnector wifiConnector;

    public WifiConnectionReceiver(WifiConnector wifiConnector) {
        this.wifiConnector = wifiConnector;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (wifiConnector.getConnectionResultListener() == null) return;
        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {

            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

            wifiLog("Connection state: " + state);

            wifiConnector.getConnectionResultListener().onStateChange(state);

            switch (state) {
                case COMPLETED:
                    wifiLog("Connection to Wifi was successfully completed...\n" +
                            "Connected to BSSID: " + wifiConnector.getWifiManager().getConnectionInfo().getBSSID() +
                            " And SSID: " + wifiConnector.getWifiManager().getConnectionInfo().getSSID());
                    if (wifiConnector.getWifiManager().getConnectionInfo().getBSSID() != null) {
                        wifiConnector.setCurrentWifiSSID(wifiConnector.getWifiManager().getConnectionInfo().getSSID());
                        wifiConnector.setCurrentWifiBSSID(wifiConnector.getWifiManager().getConnectionInfo().getBSSID());
                        wifiConnector.getConnectionResultListener().successfulConnect(wifiConnector.getCurrentWifiSSID());
                        wifiConnector.unregisterWifiConnectionListener();
                    }
                    // if BSSID is null, may be is still triying to get information about the access point
                    break;

                case DISCONNECTED:
                    int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    wifiLog("Disconnected... Supplicant error: " + supl_error);

                    // only remove broadcast listener if error was ERROR_AUTHENTICATING
                    if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                        wifiLog("Authentication error...");
                        wifiConnector.deleteWifiConf();
                        wifiConnector.getConnectionResultListener().errorConnect(WifiConnector.AUTHENTICATION_ERROR);
                    }else{
                        wifiLog("Other error..." + supl_error);
                    }
                    break;

                case AUTHENTICATING:
                    wifiLog("Authenticating...");
                    break;
            }

        }
    }

    private void wifiLog(String text) {
        if (wifiConnector.isLogOrNot()) Log.d(WifiConnector.TAG, "ConnectionReceiver: " + text);
    }

}