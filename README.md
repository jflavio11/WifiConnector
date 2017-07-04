# WifiConnector
---

## Open source library for Android to connect and manage Wifi Networks

### Requirements
* API > 19
* Since Android 6, you are able to configure WifiNetworks that your app has created, **you cannot** edit wifi configurations from others apps (Unless you are developing a system application).

### Import
#### Using Gradle
* Add this on your root build.gradle of your project:

	```
	allprojects {
		repositories {
				...
		    maven { url 'https://jitpack.io' }
		}
	}
	```
	
* And add the dependency:

	```
	compile 'com.github.jflavio1:WifiConnector:v1.3'
	```

#### Using Maven
* Add to you build file

	```
	<repositories>
		<repository>
		   <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
	```
* And the dependency

	```
	<dependency>
		<groupId>com.github.jflavio1</groupId>
		<artifactId>WifiConnector</artifactId>
		<version>v1.3</version>
	</dependency>
	```


### Example
```
	// First initializate a WifiConnector object
	WifiConnector connector = new WifiConnector(this, "NEW_SSID", "NEW_BSSID", "WEP", "wifiPassword")
	// you could register wifi state listener
	.registerWifiStateListener(new WifiStateListener() {
            @Override
            public void onStateChange(int wifiState) {
                
            }

            @Override
            public void onWifiEnabled() {
                // here you should start your network operations
            }

            @Override
            public void onWifiEnabling() {
                
            }

            @Override
            public void onWifiDisabling() {
                
            }

            @Override
            public void onWifiDisabled() {
                
            }
        })
		// and register wifi connection listener
		.registerWifiConnectionListener(new ConnectionResultListener() {
            @Override
            public void successfulConnect(String SSID) {
                Log.d("MyTag", "Success connecting to Access Point " + SSID);
            }

            @Override
            public void errorConnect(int codeReason) {
                
            }

            @Override
            public void onStateChange(SupplicantState supplicantState) {
                
            }
        })
		// and after register all listeners you want, you sould enable wifi and connect to the access point
		.enableWifi().connectToWifi();
	
		
		**OR SIMPLY**
		WifiConnector connector = new WifiConnector(this, "NEW_SSID", "NEW_BSSID", "WEP", "wifiPassword");
		
		connector.enableWifi();
		
		connector.connectToWifi(new ConnectionResultListener() {
            @Override
            public void successfulConnect(String SSID) {
                
            }

            @Override
            public void errorConnect(int codeReason) {

            }

            @Override
            public void onStateChange(SupplicantState supplicantState) {

            }
        });
		
		
		
	// And do not forget to unregister your wifi listeners on the onStop() or onDestroy() method
	connector.unregisterListeners(wifiConnector.wifiStateReceiver, wifiConnector.wifiConnectionReceiver);
	
```


### Important!
**WifiConnector instance must be implemented on Service or IntentService**

**Since 1.4 enableWifi() method is not on constructors anymore, you must call it explicity**
Since 1.2-beta1 listeners are not inside WifiConnector class, so you must call them as a single class.

Remember, you have to put these permissions on your Manifest:
```
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```

All tests and suggestions are well received.
