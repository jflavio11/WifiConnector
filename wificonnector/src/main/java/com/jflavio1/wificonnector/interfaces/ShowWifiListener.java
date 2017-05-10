package com.jflavio1.wificonnector.interfaces;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by jflav on 10/5/2017
 * email: jflavio90@gmail.com
 */
public interface ShowWifiListener {
    void onNetworksFound(WifiManager wifiManager, List<ScanResult> wifiScanResult);
    void onNetworksFound(JSONArray wifiList);
    void errorSearchingNetworks(int errorCode);
}