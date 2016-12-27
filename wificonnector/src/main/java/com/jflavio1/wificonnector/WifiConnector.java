package com.jflavio1.wificonnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by JoseFlavio on 17/11/2016.
 * contact: jflavio90@gmail.com
 */

/**
 * WifiConnector object should be created inside service or intentService
 * only works for configuredNetworks that has been created by this app
 */
public class WifiConnector {

    /**
     * tag for log
     */
    private static final String TAG = WifiConnector.class.getName();

    /**
     * application context
     */
    private Context context;

    /**
     * wifiConfiguration object that will contain access point information
     */
    private WifiConfiguration wifiConfiguration;

    /**
     * wifiManager object to manage wifi connection
     */
    private WifiManager wifiManager;

    /**
     * interface that will be call its own methods when {@link WifiReceiver#onReceive(Context, Intent)} method is called
     */
    private ConnectionResultListener connectionResultListener;

    private ShowWifiListener showWifiListener;

    /**
     * intent filter to listen for wifi state
     */
    private IntentFilter chooseWifiFilter;

    /**
     * filter for showing wifiList
     */
    private IntentFilter showWifiListFilter;

    /**
     * inherits from Broadcast receiver, will listen for {@link WifiConnector#chooseWifiFilter}
     */
    private WifiReceiver receiverWifi;

    /**
     * broadcast receiver for showing wifiList
     */
    private ShowoWifiReceiver showWifiReceiver;

    /**
     * static object for getting all wifi list that were configured by your app
     */
    private static List<WifiConfiguration> confList;

    /**
     * codes for choosewifi
     */
    public static final int SUCCESS_CONNECTION = 2500;
    public static final int AUTHENTICATION_ERROR = 2501;
    public static final int NOT_FOUND_ERROR = 2502;
    public static final int SAME_NETWORK = 2503;
    public static final int ERROR_STILL_CONNECTED_TO = 2503;
    public static final int UNKOWN_ERROR = 2505;

    /**
     * codes for searching wifi
     */
    public static final int WIFI_NETWORKS_SUCCESS_FOUND = 2600;
    public static final int NO_WIFI_NETWORKS = 2601;

    /**
     * WIFI SECURITY TYPES
     */
    private static final String SECURITY_WEP   = "WEP";
    private static final String SECURITY_WPA   = "WPA";
    private static final String SECURITY_PSK   = "PSK";
    private static final String SECURITY_EAP   = "EAP";

    /**
     * for setting wifi access point security type
     */
    public static final String SECURITY_NONE = "NONE";

    public static String CURRENT_WIFI  = null;

    public WifiConnector(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        enableWifi();
    }

    public WifiConnector(WifiConfiguration wifiConfiguration, Context context) {
        this.wifiConfiguration = wifiConfiguration;
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        enableWifi();
    }

    public WifiConnector(Context context, String SSID, @Nullable String BSSID, String securityType, @Nullable String password) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.wifiConfiguration = new WifiConfiguration();
        this.wifiConfiguration.SSID = SSID;
        this.wifiConfiguration.BSSID = BSSID;
        if (securityType.equals(SECURITY_NONE)) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            wifiConfiguration.preSharedKey = ssidFormat(password);
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        }

        enableWifi();
    }

    public void enableWifi(){
        if(!wifiManager.isWifiEnabled()) {
            wifiLog("Wifi was not enable, enable it...");
            wifiManager.setWifiEnabled(true);
        }
    }

    public boolean setPriority(int priority) {
        try {
            this.wifiConfiguration.priority = priority;
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void createChooseWifiBroadcastListener() {
        chooseWifiFilter = new IntentFilter();
        chooseWifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        receiverWifi = new WifiReceiver();
        try {
            this.context.registerReceiver(receiverWifi, chooseWifiFilter);
        } catch (Exception e) {
            wifiLog("Register broadcast error (Choose): " + e.toString());
        }
    }

    private void createShowWifiBroadcastListener() {
        showWifiListFilter = new IntentFilter();
        showWifiListFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        showWifiReceiver = new ShowoWifiReceiver();
        try {
            this.context.registerReceiver(showWifiReceiver, showWifiListFilter);
        } catch (Exception e) {
            wifiLog("Register broadcast error (ShowWifi): " + e.toString());
        }
    }

    public void showWifiList(ShowWifiListener showWifiListener) {
        this.showWifiListener = showWifiListener;
        createShowWifiBroadcastListener();
        scanWifiNetworks();
    }

    private void scanWifiNetworks() {
        wifiManager.startScan();
    }

    private boolean isAlreadyConnected(String BSSID){
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();

        if(mWifi.getType() == ConnectivityManager.TYPE_WIFI && mWifi.isConnected()) {
            if (wifiManager.getConnectionInfo().getBSSID() != null &&
                    wifiManager.getConnectionInfo().getBSSID().equals(BSSID)) {
                wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID());
                return true;
            }
        }
        return false;
    }

    /**
     * tries to connect to specific wifi
     *
     * @param connectionResultListener with methods of success and error
     */
    public void connectToWifi(ConnectionResultListener connectionResultListener) {
        this.connectionResultListener = connectionResultListener;
        if(isAlreadyConnected(wifiConfiguration.BSSID)){
            connectionResultListener.errorConnect(SAME_NETWORK);
        }else {
            if(wifiManager.getConnectionInfo().getBSSID() != null){
                CURRENT_WIFI = wifiManager.getConnectionInfo().getSSID();
                wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() + "" +
                        "Now trying to connect to " + wifiConfiguration.SSID);
            }
            createChooseWifiBroadcastListener();
            connectToWifi();
            wifiManager.reconnect();
        }
    }

    /**
     * allows to connect to specific Wifi access point
     * tries to get network id
     * if network id = -1 add network configuration
     *
     * @return boolean value if connection was successfuly complete
     */
    private boolean connectToWifi() {
        int networkId = getNetworkId(wifiConfiguration.SSID);
        wifiLog("network id found: " + networkId);
        if (networkId == -1) {
            networkId = wifiManager.addNetwork(wifiConfiguration);
            wifiLog("networkId now: " + networkId);
        }
        return enableNetwork(networkId);
    }

    /**
     * search network id by given SSID
     * if network is totally new, it returns -1
     *
     * @param SSID name of wifi network
     * @return wifi network id
     */
    private int getNetworkId(String SSID) {
        confList = wifiManager.getConfiguredNetworks();
        if (confList != null && confList.size() > 0) {
            for (WifiConfiguration existingConfig : confList) {
                if (trimQuotes(existingConfig.SSID).equals(trimQuotes(SSID))) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    private boolean enableNetwork(int networkId) {
        if (networkId == -1) {
            wifiLog("So networkId still -1, there was an error... may be authentication?");
            connectionResultListener.errorConnect(AUTHENTICATION_ERROR);
            context.unregisterReceiver(receiverWifi);
            return false;
        }
        return connectWifiManager(networkId);
    }

    private boolean connectWifiManager(int networkId) {
        wifiManager.disconnect();
        return wifiManager.enableNetwork(networkId, true);
    }

    public static String ssidFormat(String str) {
        if (!str.isEmpty()) {
            return "\"" + str + "\"";
        }
        return str;
    }

    private static String trimQuotes(String str) {
        if (!str.isEmpty()) {
            return str.replaceAll("^\"*", "").replaceAll("\"*$", "");
        }
        return str;
    }

    public interface ConnectionResultListener {
        void successfulConnect();
        void errorConnect(int codeReason);
    }

    public interface ShowWifiListener {
        void onNetworksFound(JSONArray wifiList);
        void errorSearchingNetworks(int errorCode);
    }

    private boolean deleteWifiConf() {
        try {
            confList = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : confList) {
                if (i.SSID != null && i.SSID.equals(ssidFormat(wifiConfiguration.SSID))) {
                    wifiLog("Deleting wifi configuration: " + i.SSID);
                    wifiManager.removeNetwork(i.networkId);
                    return wifiManager.saveConfiguration();
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private void wifiLog(String text) {
        Log.d(TAG, "WifiConnector: " + text);
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {

                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                wifiLog("Broadcast action: " + state);

                switch (state) {
                    case COMPLETED:
                        wifiLog("Connection to Wifi was successfuly completed...\n" +
                                "Connected to bssid: " + wifiManager.getConnectionInfo().getBSSID());
                        if(wifiManager.getConnectionInfo().getBSSID() != null){
                            connectionResultListener.successfulConnect();
                            context.unregisterReceiver(receiverWifi);
                        }
                        break;
                    case DISCONNECTED:
                        int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                        wifiLog("Disconnected... Supplicant error: " + supl_error);
                        if (supl_error == WifiManager.ERROR_AUTHENTICATING ) {
                            wifiLog("Authentication error...");
                            if(deleteWifiConf()) {
                                connectionResultListener.errorConnect(AUTHENTICATION_ERROR);
                            }else{
                                connectionResultListener.errorConnect(UNKOWN_ERROR);
                            }
                            context.unregisterReceiver(receiverWifi);
                        }
                        break;
                    case AUTHENTICATING:
                        wifiLog("Authenticating...");
                        break;
                }

            }
        }
    }

    private class ShowoWifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            final JSONArray wifiList = new JSONArray();
            List<ScanResult> wifiScanResult = wifiManager.getScanResults();
            int scanSize = wifiScanResult.size();

            try {
                scanSize--;
                if (scanSize > 0) {
                    while (scanSize >= 0) {

                        if (!wifiScanResult.get(scanSize).SSID.isEmpty()) {
                            /**
                             * individual wifi item information
                             */
                            JSONObject wifiItem = new JSONObject();

                            wifiItem.put("SSID", wifiScanResult.get(scanSize).SSID);
                            wifiItem.put("BSSID", wifiScanResult.get(scanSize).BSSID);
                            wifiItem.put("INFO", wifiScanResult.get(scanSize).capabilities);

                            /**
                             * this check if device has a current WiFi connection
                             */
                            if (wifiScanResult.get(scanSize).BSSID.equals(wifiManager.getConnectionInfo().getBSSID())) {
                                wifiItem.put("CONNECTED", true);
                            } else {
                                wifiItem.put("CONNECTED", false);
                            }
                            wifiItem.put("SECURITY_TYPE", getWifiSecurityType(wifiScanResult.get(scanSize)));
                            wifiItem.put("LEVEL", WifiManager.calculateSignalLevel(wifiScanResult.get(scanSize).level, 100) + "%");

                            wifiList.put(wifiItem);
                        }

                        scanSize--;
                    }

                    showWifiListener.onNetworksFound(wifiList);

                } else {
                    showWifiListener.errorSearchingNetworks(NO_WIFI_NETWORKS);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            context.unregisterReceiver(this);

        }

        private String getWifiSecurityType(ScanResult result) {
            if (result.capabilities.contains("WEP")) {
                return SECURITY_WEP;
            } else if (result.capabilities.contains("WPA")) {
                return SECURITY_WPA;
            } else if (result.capabilities.contains("PSK")) {
                return SECURITY_PSK;
            } else if (result.capabilities.contains("EAP")) {
                return SECURITY_EAP;
            }
            return SECURITY_NONE;
        }

    }
}
