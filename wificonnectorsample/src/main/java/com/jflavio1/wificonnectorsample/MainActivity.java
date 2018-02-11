/*
 * Created by Jose Flavio on 10/18/17 1:20 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnectorsample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jflavio1.wificonnector.WifiConnector;
import com.jflavio1.wificonnector.interfaces.ConnectionResultListener;
import com.jflavio1.wificonnector.interfaces.RemoveWifiListener;
import com.jflavio1.wificonnector.interfaces.ShowWifiListener;
import com.jflavio1.wificonnector.interfaces.WifiConnectorModel;
import com.jflavio1.wificonnector.interfaces.WifiStateListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiConnectorModel {

    // ui
    private Switch mSwitch;
    private TextView mWifiActiveTxtView;
    private RecyclerView rv;

    private WifiListRvAdapter adapter;
    private WifiConnector wifiConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwitch = findViewById(R.id.wifiActivationSwitch);
        mWifiActiveTxtView = findViewById(R.id.wifiActivationTv);
        rv = findViewById(R.id.wifiRv);

        setLocationPermission();

        adapter = new WifiListRvAdapter(new WifiListRvAdapter.WifiItemListener() {
            @Override
            public void onWifiItemClicked(ScanResult scanResult) {
                openConnectDialog(scanResult);
            }

            @Override
            public void onWifiItemLongClick(ScanResult scanResult) {
                disconnectFromAccessPoint(scanResult);
            }
        });
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        createWifiConnectorObject();

    }

    @Override
    protected void onDestroy() {
        destroyWifiConnectorListeners();
        super.onDestroy();
    }

    @Override
    public void createWifiConnectorObject() {
        wifiConnector = new WifiConnector(this);
        wifiConnector.setLog(true);
        wifiConnector.registerWifiStateListener(new WifiStateListener() {
            @Override
            public void onStateChange(int wifiState) {

            }

            @Override
            public void onWifiEnabled() {
                MainActivity.this.onWifiEnabled();
            }

            @Override
            public void onWifiEnabling() {

            }

            @Override
            public void onWifiDisabling() {

            }

            @Override
            public void onWifiDisabled() {
                MainActivity.this.onWifiDisabled();
            }
        });

        if(wifiConnector.isWifiEnbled()){
            mSwitch.setChecked(true);
            onWifiEnabled();
        } else {
            mSwitch.setChecked(false);
            onWifiDisabled();
        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiConnector.enableWifi();
                } else {
                    wifiConnector.disableWifi();
                }
            }
        });

    }

    private void onWifiEnabled(){
        mWifiActiveTxtView.setText("Disable Wifi");
        if (permisionLocationOn()) {
            scanForWifiNetworks();
        } else {
            checkLocationTurnOn();
        }
    }

    private void onWifiDisabled(){
        mWifiActiveTxtView.setText("Enable Wifi");
        adapter.setScanResultList(new ArrayList<ScanResult>());
    }

    @Override
    public void scanForWifiNetworks() {
        wifiConnector.showWifiList(new ShowWifiListener() {
            @Override
            public void onNetworksFound(WifiManager wifiManager, List<ScanResult> wifiScanResult) {
                adapter.setScanResultList(wifiScanResult);
            }

            @Override
            public void onNetworksFound(JSONArray wifiList) {

            }

            @Override
            public void errorSearchingNetworks(int errorCode) {
                Toast.makeText(MainActivity.this, "Error on getting wifi list, error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openConnectDialog(ScanResult scanResult){
        ConnectToWifiDialog dialog = new ConnectToWifiDialog(MainActivity.this, scanResult);
        dialog.setConnectButtonListener(new ConnectToWifiDialog.DialogListener() {
            @Override
            public void onConnectClicked(ScanResult scanResult, String password) {
                connectToWifiAccessPoint(scanResult, password);
            }
        });
        dialog.show();
    }

    @Override
    public void connectToWifiAccessPoint(final ScanResult scanResult, String password) {
        this.wifiConnector.setScanResult(scanResult, password);
        this.wifiConnector.setLog(true);
        this.wifiConnector.connectToWifi(new ConnectionResultListener() {
            @Override
            public void successfulConnect(String SSID) {
                Toast.makeText(MainActivity.this, "You are connected to " + scanResult.SSID + "!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void errorConnect(int codeReason) {
                Toast.makeText(MainActivity.this, "Error on connecting to wifi: " + scanResult.SSID +"\nError code: "+ codeReason,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStateChange(SupplicantState supplicantState) {

            }
        });
    }

    @Override
    public void disconnectFromAccessPoint(ScanResult scanResult) {
        this.wifiConnector.removeWifiNetwork(scanResult, new RemoveWifiListener() {
            @Override
            public void onWifiNetworkRemoved() {
                Toast.makeText(MainActivity.this, "You have removed this wifi!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWifiNetworkRemoveError() {
                Toast.makeText(MainActivity.this, "Error on removing this network!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void destroyWifiConnectorListeners() {
        wifiConnector.unregisterWifiStateListener();
    }

    // region permission
    private Boolean permisionLocationOn() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    private Boolean checkLocationTurnOn() {
        boolean onLocation = true;
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionGranted) {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled) {
                onLocation = false;
                AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog));
                //android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
                dialog.setMessage("Please turn on your location");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
                dialog.show();
            }
        }
        return onLocation;
    }

    // endregion
}
