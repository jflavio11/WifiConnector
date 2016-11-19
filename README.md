# WifiConnector
---

## Open source library for Android to connect to Wifi Networks

### Requirements
* API > 16
* Since Android 6, you are able to configure WifiNetworks that your app has created, **you cannot** edit wifi configurations from others apps.

## Instalation
**Using Gradle**

And this to your app build.gradle
```
compile 'com.jflavio1.wificonnector:wifi-connector:1.1'
```

**Using Maven**
```
<dependency> 
	<groupId>com.jflavio1.wificonnector</groupId> 
	<artifactId>wifi-connector</artifactId> 
	<version>1.0</version> 
	<type>pom</type> 
</dependency>
```

### Example
```
	// third and fith parameters could be null
	WifiConnector connector = new WifiConnector(this, "NEW_SSID", "NEW_BSSID", "WEP", "wifiPassword");
	connector.connectToWifi(new WifiConnector.ConnectionResultListener() {
	    @Override
	    public void successfulConnect() {
	        Toast.makeText(getApplicationContext(), "Successfuly connection to wifi!", Toast.LENGTH_SHORT).show();
	    }

	    @Override
	    public void errorConnect(int codeReason) {
	        Toast.makeText(getApplicationContext(), "There was an error connecting to wifi...", Toast.LENGTH_SHORT).show();
	    }
	});
```

### TODO
* scan wifi networks functionality

### Important!
**WifiConnector instance must be implemented on Service or IntentService**
Remember, you have to put these permissions on your Manifest:
```
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```

All tests and suggestions are well received.