# WifiConnector
---

## Open source library for Android to connect to Wifi Networks

### Requirements
* API > 16

### Instalation
***Using Gradle***
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