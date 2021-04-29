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
import android.util.Log;

import com.jflavio1.wificonnector.interfaces.ConnectionResultListener;
import com.jflavio1.wificonnector.interfaces.RemoveWifiListener;
import com.jflavio1.wificonnector.interfaces.ShowWifiListener;
import com.jflavio1.wificonnector.interfaces.WifiStateListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by JoseFlavio on 17/11/2016.
 * <p>
 * <h1>WifiConnector</h1>
 * <p>
 * WifiConnector object should be created inside service or intentService
 * you could instantiate it on any activity of fragment but network operations must work on a different thread from UI
 * <p>
 * only works for configured networks that has been created by current app but could configure any network if you are building
 * a signed and system application
 *
 * @author JoseFlavio
 *         contact: jflavio90@gmail.com
 * @since version 1.4 if you want to turn-on wifi <strong>you must call {@link #enableWifi()} method after
 * creating WifiConnector object.</strong>
 */
public class WifiConnector {

    /**
     * Tag for log
     */
    public static final String TAG = WifiConnector.class.getName();

    /**
     * For setting if log is going to be showed
     * This attribute is true as default. Set {@link #setLog(boolean)} as false for avoid logs
     */
    private boolean logOrNot = false;

    /**
     * Should be application context
     */
    private Context context;

    /**
     * WifiConfiguration object that will contain access point information
     */
    private WifiConfiguration wifiConfiguration;

    /**
     * WifiManager object to manage wifi connection
     */
    private WifiManager wifiManager;

    /**
     * Interface that must be used before any asynchronous operation. Methods are called on
     * {@link WifiStateReceiver#onReceive(Context, Intent)}
     */
    private WifiStateListener wifiStateListener;

    /**
     * Interface that will call its own methods when {@link WifiConnectionReceiver#onReceive(Context, Intent)} method is called
     */
    private ConnectionResultListener connectionResultListener;

    /**
     * Interface that will call its own methods when {@link ShowWifiListReceiver#onReceive(Context, Intent)} method is
     * called
     */
    private ShowWifiListener showWifiListListener;

    /**
     * Interface that will call its own method when
     * {@link WifiConnector#removeWifiNetwork(ScanResult, RemoveWifiListener)} methods are called
     */
    private RemoveWifiListener removeWifiListener;

    /**
     * Filter for showing wifi state after {@link #enableWifi()} is called
     */
    public IntentFilter wifiStateFilter;

    /**
     * Intent filter to listen status of connection to a wifi network
     */
    private IntentFilter chooseWifiFilter;

    /**
     * Filter for showing wifi list after {@link #scanWifiNetworks()} is called
     */
    private IntentFilter showWifiListFilter;

    /**
     * Broadcast receiver for listening wifi state
     *
     * @see WifiStateListener
     */
    public WifiStateReceiver wifiStateReceiver;

    /**
     * Inherits from Broadcast receiver, will listen for {@link WifiConnector#chooseWifiFilter}
     */
    public WifiConnectionReceiver wifiConnectionReceiver;

    /**
     * Broadcast receiver for showing wifiList
     */
    public ShowWifiListReceiver showWifiListReceiver;

    /**
     * Static object for getting all wifi list that were configured by your app
     */
    private static List<WifiConfiguration> confList;

    /**
     * Codes for choosewifi
     */
    public static final int SUCCESS_CONNECTION = 2500;
    public static final int AUTHENTICATION_ERROR = 2501;
    public static final int NOT_FOUND_ERROR = 2502;
    public static final int SAME_NETWORK = 2503;
    public static final int ERROR_STILL_CONNECTED_TO = 2504;
    public static final int UNKOWN_ERROR = 2505;

    /**
     * codes for searching wifi
     */
    public static final int WIFI_NETWORKS_SUCCESS_FOUND = 2600;
    public static final int NO_WIFI_NETWORKS = 2601;

    /**
     * WIFI SECURITY TYPES
     */
    public static final String SECURITY_WEP = "WEP";
    public static final String SECURITY_WPA = "WPA";
    public static final String SECURITY_PSK = "PSK";
    public static final String SECURITY_EAP = "EAP";

    /**
     * for setting wifi access point security type
     */
    public static final String SECURITY_NONE = "NONE";

    /**
     * String value for current connected Wi-Fi network
     */
    private String currentWifiSSID = null;

    /**
     * Static value to be acceded from anywhere
     */
    public static String CURRENT_WIFI = null;

    /**
     * String value for current connected Wi-Fi network
     */
    private String currentWifiBSSID = null;

    @Deprecated
    public WifiConnector(Context context, boolean enableWifi) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (enableWifi) {
            enableWifi();
        }
    }

    public WifiConnector(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public WifiConnector(Context context, ScanResult scanResult, String password) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setWifiConfiguration(scanResult.SSID, scanResult.BSSID, getWifiSecurityType(scanResult), password);
    }

    public WifiConnector(WifiConfiguration wifiConfiguration, Context context) {
        this.wifiConfiguration = wifiConfiguration;
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public WifiConnector(Context context, String SSID, String BSSID, String securityType, String password) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setWifiConfiguration(SSID, BSSID, securityType, password);
    }

    public void setScanResult(ScanResult scanResult, String password) {
        setWifiConfiguration(scanResult.SSID, scanResult.BSSID, getWifiSecurityType(scanResult), password);
    }

    public void setWifiConfiguration(String SSID, String BSSID, String securityType, String password) {
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
    }

    // region Receivers

    /**
     * This method must be called before any asynchronous operation because some methods will work only if
     * {@link WifiManager#getWifiState()} returns {@link WifiManager#WIFI_STATE_ENABLED}
     * <p>
     * IMPORTANT!
     * <strong>Do not forget to call {@link #unregisterWifiStateListener()} after finish all your operations.</strong>
     *
     * @param wifiStateListener Interface that will manage Wifi state
     * @see WifiStateListener
     */
    public WifiConnector registerWifiStateListener(WifiStateListener wifiStateListener) {
        createWifiStateBroadcast();
        this.wifiStateListener = wifiStateListener;
        return this;
    }

    /**
     * This method must be called after all your operations if {@link #registerWifiStateListener(WifiStateListener)}
     * was called. That means, on the onStop() or onDestroy() method of your Activity, Fragment or Service.
     *
     * @see WifiStateListener
     */
    public void unregisterWifiStateListener() {
        try {
            context.getApplicationContext().unregisterReceiver(wifiStateReceiver);
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi State Listener because may be it was never registered");
        }
    }

    WifiStateListener getWifiStateListener() {
        return wifiStateListener;
    }

    /**
     * This method allows to register {@link ConnectionResultListener} interface to know whether when you turn-on wifi it
     * is connecting to any wifi access point.
     *
     * @param connectionResultListener is the listener for knowing wifi supplicant state.
     * @return this wifiConnector object
     * @see SupplicantState
     */
    public WifiConnector registerWifiConnectionListener(ConnectionResultListener connectionResultListener) {
        createWifiConnectionBroadcastListener();
        this.connectionResultListener = connectionResultListener;
        return this;
    }

    /**
     * This method is for unregister {@link #connectionResultListener} object.
     * {@link WifiConnectionReceiver#onReceive(Context, Intent)} use this when {@link SupplicantState#COMPLETED} or
     * {@link SupplicantState#DISCONNECTED} states are set.
     * <p>
     * <strong>So you should not call it explicit if the connection state lifecycle is not known.</strong>
     */
    public synchronized void unregisterWifiConnectionListener() {
        try {
            context.getApplicationContext().unregisterReceiver(this.wifiConnectionReceiver);
            this.wifiConnectionReceiver = null;
            wifiLog("unregisterWifiConnectionListener");
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi Connection Listener because may be it was never registered");
        }
    }

    /**
     * @return the current {@link #connectionResultListener} object
     */
    ConnectionResultListener getConnectionResultListener() {
        return connectionResultListener;
    }

    /**
     * This method allows to listen the wifi 'finding networks' state
     *
     * @param showWifiListener is the listener for knowing searching state
     * @return this WifiConnector object
     */
    public WifiConnector registerShowWifiListListener(ShowWifiListener showWifiListener) {
        createShowWifiListBroadcastListener();
        this.showWifiListListener = showWifiListener;
        return this;
    }

    /**
     * For unregister {@link #showWifiListListener} object.
     * {@link ShowWifiListReceiver#onReceive(Context, Intent)} call this method when scan results are returned.
     */
    public synchronized void unregisterShowWifiListListener() {
        try {
            this.showWifiListListener = null;
            context.getApplicationContext().unregisterReceiver(showWifiListReceiver);
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi List Listener because may be it was never registered ");
        }
    }

    ShowWifiListener getShowWifiListListener() {
        return showWifiListListener;
    }

    /**
     * This method allows to listener actions of the {@link #removeWifiNetwork(ScanResult, RemoveWifiListener)}
     * method variations.
     *
     * @param removeWifiListener is the listener {@link RemoveWifiListener}
     * @return current WifiConnector object.
     */
    public WifiConnector registerWifiRemoveListener(RemoveWifiListener removeWifiListener) {
        createWifiStateBroadcast();
        this.removeWifiListener = removeWifiListener;
        return this;
    }

    /**
     * For unregister {@link #removeWifiListener} object.
     */
    public synchronized void unregisterWifiRemoveListener() {
        try {
            context.getApplicationContext().unregisterReceiver(wifiStateReceiver);
        } catch (Exception e) {
            wifiLog("Error unregistering Wifi Remove Listener because may be it was never registered");
        }
    }

    /**
     * Allows unregister any of the broadcastlisteners that WifiConnector could use.
     *
     * @param broadcastReceivers is an array of WifiConnector Broadcast Receivers
     */
    public synchronized void unregisterReceivers(Object... broadcastReceivers) {
        wifiLog("Unregistering wifi listener(s)");
        for (int i = 0; i < broadcastReceivers.length; i++) {

            try {
                context.getApplicationContext().unregisterReceiver((BroadcastReceiver) broadcastReceivers[i]);
            } catch (Exception e) {
                wifiLog("Error unregistering broadcast " + i + " because may be it was never registered");
            }

        }
    }

    // endregion

    /**
     * For enabling wifi
     * If you want to listen wifi states, should call {@link #registerWifiStateListener(WifiStateListener)} and wait for
     * callback to update User Interface of your application.
     * <p>
     * We call {@link #createWifiConnectionBroadcastListener()} to determine using {@link WifiConnectionReceiver} if we are connected to
     * any wifi network.
     *
     * @return this WifiConnector object for being used in any register callback method.
     */
    public WifiConnector enableWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            wifiLog("Wifi enabled, determining current connected wifi network if there is one...");
            setInitWifiInformation();
        } else {
            wifiLog("Wifi is already enable...");
        }
        return this;
    }

    /**
     * Just for having information about a current network if connected when wifi is enabled.
     */
    private void setInitWifiInformation() {
        connectionResultListener = new ConnectionResultListener() {
            @Override
            public void successfulConnect(String SSID) {

            }

            @Override
            public void errorConnect(int codeReason) {

            }

            @Override
            public void onStateChange(SupplicantState supplicantState) {

            }
        };
        createWifiConnectionBroadcastListener();
    }

    private void createWifiStateBroadcast() {
        wifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiStateReceiver = new WifiStateReceiver(this);
        try {
            context.getApplicationContext().registerReceiver(wifiStateReceiver, wifiStateFilter);
        } catch (Exception e) {
            wifiLog("Exception on registering broadcast for listening Wifi State: " + e.toString());
        }
    }

    /**
     * For disabling wifi
     * If you want to listen wifi states, should call {@link #unregisterWifiStateListener()} and wait for
     * callback to update User Interface on your application
     */
    public void disableWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        } else {
            wifiLog("Wifi is not enable...");
        }
    }

    /**
     * For knowing if wifi is enabled
     *
     * @return true if wifi is enabled
     */
    public boolean isWifiEnbled() {
        return wifiManager.isWifiEnabled();
    }

    private void setCurrentWifiInfo() {
        setCurrentWifiSSID(wifiManager.getConnectionInfo().getSSID());
        setCurrentWifiBSSID(wifiManager.getConnectionInfo().getBSSID());
    }

    public String getCurrentWifiSSID() {
        return currentWifiSSID;
    }

    public void setCurrentWifiSSID(String currentWifiSSID) {
        this.currentWifiSSID = currentWifiSSID;
        CURRENT_WIFI = getCurrentWifiSSID();
    }

    public String getCurrentWifiBSSID() {
        return currentWifiBSSID;
    }

    public void setCurrentWifiBSSID(String currentWifiBSSID) {
        this.currentWifiBSSID = currentWifiBSSID;
    }

    public boolean setPriority(int priority) {
        try {
            this.wifiConfiguration.priority = priority;
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    private synchronized void createWifiConnectionBroadcastListener() {
        if(wifiConnectionReceiver == null){
            wifiLog("creating wifiConnectionReceiver");
            chooseWifiFilter = new IntentFilter();
            chooseWifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            wifiConnectionReceiver = new WifiConnectionReceiver(this);
            try {
                context.getApplicationContext().registerReceiver(wifiConnectionReceiver, chooseWifiFilter);
            } catch (Exception e) {
                wifiLog("Register broadcast error (Choose): " + e.toString());
            }
        }else{
            wifiLog("wifiConnectionReceiver already exists");
        }
    }

    private void createShowWifiListBroadcastListener() {
        showWifiListFilter = new IntentFilter();
        showWifiListFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        showWifiListReceiver = new ShowWifiListReceiver(this);
        try {
            context.getApplicationContext().getApplicationContext().registerReceiver(showWifiListReceiver, showWifiListFilter);
        } catch (Exception e) {
            wifiLog("Register broadcast error (ShowWifi): " + e.toString());
        }
    }

    /**
     * Tries to scan available wifi networks. After {@link ShowWifiListReceiver} gets results (or not), it automatically  unregister the
     * current broadcast listener so is not necessary to explicitly call {@link #unregisterShowWifiListListener()}.
     *
     * @param showWifiListener interface that will give the results
     */
    public void showWifiList(ShowWifiListener showWifiListener) {
        this.showWifiListListener = showWifiListener;
        wifiLog("show wifi list");
        createShowWifiListBroadcastListener();
        scanWifiNetworks();
    }

    private void scanWifiNetworks() {
        wifiManager.startScan();
    }

    public boolean isAlreadyConnected(String BSSID) {
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();

        wifiLog("isAlreadyConnected: " + wifiManager.getConnectionInfo().getBSSID() + " " + BSSID);

        if (mWifi != null && mWifi.getType() == ConnectivityManager.TYPE_WIFI && mWifi.isConnected()) {
            isConnectedToBSSID(BSSID);
        } else {
            wifiLog("getActiveNetwork - NetworkInfo is null");
        }
        return false;
    }

    public boolean isConnectedToBSSID(String BSSID) {
        if (wifiManager.getConnectionInfo().getBSSID() != null &&
                wifiManager.getConnectionInfo().getBSSID().equals(BSSID)) {
            wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() +
                    "  BSSID: " + wifiManager.getConnectionInfo().getBSSID() + "  " + BSSID);
            return true;
        }
        return false;
    }

    /**
     * Tries to connect to specific wifi set on the constructor.
     *
     * @param connectionResultListener with methods of success and error
     */
    public void connectToWifi(ConnectionResultListener connectionResultListener) {
        this.connectionResultListener = connectionResultListener;
        if (isConnectedToBSSID(wifiConfiguration.BSSID)) {
            connectionResultListener.errorConnect(SAME_NETWORK);
        } else {
            if (wifiManager.getConnectionInfo().getBSSID() != null) {
                setCurrentWifiSSID(wifiManager.getConnectionInfo().getSSID());
                setCurrentWifiBSSID(wifiManager.getConnectionInfo().getBSSID());
                wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() + " " +
                        "Now trying to connect to " + wifiConfiguration.SSID);
            }
            connectToWifi();
        }
    }

    public void setConnectionResultListener(ConnectionResultListener connectionResultListener) {
        this.connectionResultListener = connectionResultListener;
        createWifiConnectionBroadcastListener();
    }

    /**
     * Tries to connect to specific wifi network set on the constructor.
     * <strong>Remember: you must be register {@link #connectionResultListener} object before.</strong>
     */
    public void connectToWifi() {
        if (isConnectedToBSSID(wifiConfiguration.BSSID)) {
            connectionResultListener.errorConnect(SAME_NETWORK);
        } else {
            if (wifiManager.getConnectionInfo().getBSSID() != null) {
                setCurrentWifiSSID(wifiManager.getConnectionInfo().getSSID());
                setCurrentWifiBSSID(wifiManager.getConnectionInfo().getBSSID());
                wifiLog("Already connected to: " + wifiManager.getConnectionInfo().getSSID() + " " +
                        "Now trying to connect to " + wifiConfiguration.SSID);
            }
            connectToWifiAccesPoint();
        }
    }

    /**
     * Allows to connect to specific Wifi access point.
     * <ul><li>Tries to get network id</li></ul>
     * <ul><li>if network id = -1 add network configuration</li></ul>
     *
     * @return boolean value if connection was successfully completed
     */
    private boolean connectToWifiAccesPoint() {
        createWifiConnectionBroadcastListener();
        int networkId = getNetworkId(wifiConfiguration.SSID);
        wifiLog("network id found: " + networkId);
        if (networkId == -1) {
            networkId = wifiManager.addNetwork(wifiConfiguration);
            wifiLog("networkId now: " + networkId);
        }
        return enableNetwork(networkId);
    }

    /**
     * Search network id by given SSID.
     * If network is totally new, it returns -1.
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
            wifiLog("So networkId still -1, there was an error... likely incorrect SSID or security type");
            connectionResultListener.errorConnect(NOT_FOUND_ERROR);
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

    public void removeCurrentWifiNetwork(RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(getCurrentWifiSSID(), getCurrentWifiBSSID());
    }

    public void removeWifiNetwork(WifiConfiguration wifiConfiguration, RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(wifiConfiguration.SSID, wifiConfiguration.BSSID);
    }

    public void removeWifiNetwork(String SSID, String BSSID, RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(SSID, BSSID);
    }

    public void removeWifiNetwork(ScanResult scanResult, RemoveWifiListener removeWifiListener) {
        this.removeWifiListener = removeWifiListener;
        removeWifiNetwork(scanResult.SSID, scanResult.BSSID);
    }

    // TODO show reason to remove failure!
    private void removeWifiNetwork(String SSID, String BSSID) {
        List<WifiConfiguration> list1 = wifiManager.getConfiguredNetworks();
        if (list1 != null && list1.size() > 0) {
            for (WifiConfiguration i : list1) {
                try {
                    if (SSID.equals(getCurrentWifiSSID()) || BSSID.equals(getCurrentWifiBSSID())) {
                        if (wifiManager.removeNetwork(i.networkId)) {
                            wifiLog("Network deleted: " + i.networkId + " " + i.SSID);
                            removeWifiListener.onWifiNetworkRemoved();
                        } else {
                            removeWifiListener.onWifiNetworkRemoveError();
                        }
                        wifiManager.saveConfiguration();
                    } else {
                        wifiLog("Unable to remove Wifi Network " + i.SSID);
                        removeWifiListener.onWifiNetworkRemoveError();
                    }
                } catch (NullPointerException e) {
                    wifiLog("Exception on removing wifi network: " + e.toString());
                }
            }
        } else {
            wifiLog("Empty Wifi List");
            removeWifiListener.onWifiNetworkRemoveError();
        }
    }

    /**
     * ForgetNetwork is a method that will only works if app is signed and run as system.
     * It will look for "forget" hidden method on WifiManager class.
     *
     * @param wifiManager current wifiManager
     * @param i           the wifiConfigured network to delete
     * @hide
     */
    public void forgetWifiNetwork(WifiManager wifiManager, WifiConfiguration i) {
        try {
            Method[] methods = wifiManager.getClass().getDeclaredMethods();
            Method forgetMEthod = null;
            for (Method method : methods) {
                if (method.getName().contains("forget")) {
                    forgetMEthod = method;
                    forgetMEthod.invoke(wifiManager, i.networkId, null);
                    wifiLog("Forgotten network " + i.SSID);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            wifiLog("Exception: " + e.toString());
        }
    }

    /**
     * Similar to {@link #forgetWifiNetwork(WifiManager, WifiConfiguration)} but this will run with any application
     * installed as user app and will only delete wifi configurations created by its own.
     *
     * @return true if delete configuration was successful
     */
    boolean deleteWifiConf() {
        try {
            confList = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : confList) {
                if (i.SSID != null && i.SSID.equals(wifiConfiguration.SSID)) {
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

    public void setLog(boolean log) {
        this.logOrNot = log;
    }

    public boolean isLogOrNot() {
        return logOrNot;
    }

    public static String getWifiSecurityType(ScanResult result) {
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

    private void wifiLog(String text) {
        if (logOrNot) Log.d(TAG, "WifiConnector: " + text);
    }

}
