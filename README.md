# WifiConnector
---

## Open source library for Android to connect to Wifi Networks

### Requirements
* API > 16
* Since Android 6, you are able to configure WifiNetworks that your app has created, **you cannot** edit wifi configurations from others apps.

### Example
```
	// third and fith parameters could be null
	WifiConnector connector = new WifiConnector(this, "NEW_SSID", "NEW_BSSID", "WEP", "wifiPassword");
	connector.connectToWifi(new WifiConnector.ConnectionResultListener() {
	    @Override
                public void successfulConnect(String SSID) {
                    // toast!
                }

                @Override
                public void errorConnect(int codeReason) {
                    // toast!
                }

                @Override
                public void onStateChange(SupplicantState supplicantState) {
		// update UI!
                }
	});
```


### Important!
**WifiConnector instance must be implemented on Service or IntentService**
Remember, you have to put these permissions on your Manifest:
```
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```

All tests and suggestions are well received.
