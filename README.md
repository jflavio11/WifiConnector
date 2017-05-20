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
	WifiConnector connector = new WifiConnector(this, "NEW_SSID", "NEW_BSSID", "WEP", "wifiPassword");
	
	// Before any operation, you should be sure that wifi enabled
	connector.setWifiStateListener(new WifiStateListener() {
            @Override
            public void onStateChange(int wifiState) {

            }

            @Override
            public void onWifiEnabled() {
				// here you should be start your network operations
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
        });
		
	
	// For connecting to specific wifi network, third parameter (new_bssid) could be null
	connector.connectToWifi(new ConnectionResultListener() {
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
	
	// And do not forget to unregister your wifi state listener on the onStop() or onDestroy() method
	connector.unregisterWifiStateListener();
	
```


### Important!
**WifiConnector instance must be implemented on Service or IntentService**

Since 1.2-beta1 listeners are not inside WifiConnector class, so you must call them as a single class.

Remember, you have to put these permissions on your Manifest:
```
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```

All tests and suggestions are well received.
