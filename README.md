# WifiConnector
---

## Open source library for Android to connect to Wifi Networks

### Requirements
* API > 16

### Instalation
**Using Gradle**

Add this on your project build.gradle
```
allprojects {
    repositories {
        maven { url "http://jofani.bintray.com/Wifi-connector" }
    }
}
```
And this to your app build.gradle
```
dependences{
	compile 'com.jflavio1.wificonnector:wifi-connector:1.0'
	}
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
* check if new ssid is the current wifi network

### Important!
**WifiConnector instance must be implemented on Service or IntentService**

All tests and suggestions are well received.