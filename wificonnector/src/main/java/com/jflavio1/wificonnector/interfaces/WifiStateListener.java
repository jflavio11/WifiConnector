package com.jflavio1.wificonnector.interfaces;

import com.jflavio1.wificonnector.WifiConnector;

/**
 * Created by joseflavio on 18/05/2017
 * email: jflavio90@gmail.com
 *
 * <h1>Wifi State Listener</h1>
 *
 * <p>
 *     WifiStateListener is an interface which method will be called when
 *     {@link com.jflavio1.wificonnector.WifiConnector#wifiStateListener} receive the broadcast for Wifi state from
 *     {@link android.net.wifi.WifiManager} when {@link WifiConnector#enableWifi()} is executed.
 * </p>
 *
 * <h4>States are detailed on {@link android.net.wifi.WifiManager} class, they are integer type:</h4>
 *
 * <ul><li>{@link android.net.wifi.WifiManager#WIFI_STATE_ENABLING}</li></ul>
 * <ul><li>{@link android.net.wifi.WifiManager#WIFI_STATE_ENABLED}</li></ul>
 * <ul><li>{@link android.net.wifi.WifiManager#WIFI_STATE_DISABLING}</li></ul>
 * <ul><li>{@link android.net.wifi.WifiManager#WIFI_STATE_DISABLED}</li></ul>
 *
 *
 * <h2>IMPORTANT!</h2>
 * <p>This interface must be used before any asynchronous operation such as scan wifi networks or connect to any access point. Moreover,
 * broadcast {@link WifiConnector#wifiStateReceiver} must be unregister from any service or activity that has created the WifiConnector
 * object using {@link WifiConnector#unregisterWifiStateListener()}
 * </p>
 *
 * @see android.net.wifi.WifiManager
 * @see WifiConnector
 */
public interface WifiStateListener{

    void onStateChange(int wifiState);

    void onWifiEnabled();

    void onWifiEnabling();

    void onWifiDisabling();

    void onWifiDisabled();

}