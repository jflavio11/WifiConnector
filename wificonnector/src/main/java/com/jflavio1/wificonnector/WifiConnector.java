package com.jflavio1.wificonnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * Created by JoseFlavio on 17/11/2016.
 * contact: jflavio90@gmail.com
 */

/**
 * WifiConnector object should be created inside service or intentService
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

    /**
     * intent filter to listen for wifi state
     */
    private IntentFilter mIntentFilter;

    /**
     * inherits from Broadcast receiver, will listen for {@link WifiConnector#mIntentFilter}
     */
    private WifiReceiver receiverWifi;

    /**
     * static object for getting all wifi list that were configured by your app
     */
    private static List<WifiConfiguration> confList;

    public static final int AUTHENTICATION_ERROR = 111;
    public static final int NOT_FOUND_ERROR      = 222;
    public static final int SUCCESS_CONNECTION   = 333;
    public static final int SAME_NETWORK         = 444;

    /**
     * for setting wifi access point security type
     */
    public static final String SECURITY_NONE            = "NONE";

    public WifiConnector(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public WifiConnector(WifiConfiguration wifiConfiguration, Context context) {
        this.wifiConfiguration = wifiConfiguration;
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public WifiConnector(Context context, String SSID, @Nullable String BSSID, String securityType, @Nullable String password){
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.wifiConfiguration = new WifiConfiguration();
        this.wifiConfiguration.SSID = SSID;
        this.wifiConfiguration.BSSID = BSSID;
        if(securityType.equals(SECURITY_NONE)){
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }else{
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
    }

    public boolean setPriority(int priority){
        try{
            this.wifiConfiguration.priority = priority;
            return true;
        }catch (NullPointerException e){
            return false;
        }
    }

    private void createBroadcastListener(){
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        receiverWifi = new WifiReceiver();
        try{this.context.registerReceiver(receiverWifi, mIntentFilter);}catch (Exception ignored){}
    }

    /**
     * tries to connect to specific wifi
     * @param connectionResultListener with methods of success and error
     */
    public void connectToWifi(ConnectionResultListener connectionResultListener){
        this.connectionResultListener = connectionResultListener;
        connectToWifi();
        createBroadcastListener();
        wifiManager.reconnect();
    }

    /**
     * allows to connect to specific Wifi access point
     * tries to get network id
     * if network id = -1 add network configuration
     * @return boolean value if connection was successfuly complete
     */
    private boolean connectToWifi(){
        int networkId = getNetworkId(wifiConfiguration.SSID);
        wifiLog("network id: " + networkId);
        if(networkId == -1){
            networkId = wifiManager.addNetwork(wifiConfiguration);
            wifiLog("networkId now: "+networkId);
        }
        return enableNetwork(networkId);
    }

    /**
     * search network id by given SSID
     * if network is totally new, it returns -1
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
        if(networkId == -1){
            wifiLog("So networkId still -1, there was an error... may be authentication?");
            connectionResultListener.errorConnect(AUTHENTICATION_ERROR);
            context.unregisterReceiver(receiverWifi);
            return false;
        }
        return connectWifiManager(networkId);
    }

    private boolean connectWifiManager(int networkId) {
        return wifiManager.enableNetwork(networkId, true);
    }

    private static String ssidFormat(String str){
        if(!str.isEmpty()) {
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

    public interface ConnectionResultListener{
        void successfulConnect();
        void errorConnect(int codeReason);
    }

    private void deleteWifiConf() {
        try {
            confList = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : confList) {
                if (i.SSID != null && i.SSID.equals(ssidFormat(wifiConfiguration.SSID))) {
                    wifiLog("Deleting wifi configuration: " + i.SSID);
                    wifiManager.removeNetwork(i.networkId);
                    wifiManager.saveConfiguration();
                }
            }
        }catch (Exception ignored){}
    }

    private void wifiLog(String text){
        Log.d(TAG, "WifiConnector: " + text);
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            String action  = intent.getAction();
            if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
                SupplicantState supl_state=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));

                if(supl_state.equals(SupplicantState.COMPLETED)){
                    String bssid = intent.getParcelableExtra(WifiManager.EXTRA_BSSID);
                    wifiLog("wifiManager completed connection");
                    wifiManager.saveConfiguration();
                    context.unregisterReceiver(receiverWifi);
                    connectionResultListener.successfulConnect();
                }else if(supl_state.equals(SupplicantState.DISCONNECTED)){
                    int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    if(supl_error == WifiManager.ERROR_AUTHENTICATING){
                        wifiLog("wifiManager authentication error");
                        context.unregisterReceiver(receiverWifi);
                        connectionResultListener.errorConnect(AUTHENTICATION_ERROR);
                        deleteWifiConf();
                    }
                }


            }
        }
    }

}
