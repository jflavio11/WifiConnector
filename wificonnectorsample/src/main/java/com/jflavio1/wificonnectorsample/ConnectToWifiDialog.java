/*
 * Created by Jose Flavio on 2/10/18 4:33 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnectorsample;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jflavio1.wificonnector.WifiConnector;

/**
 * ConnectToWifiDialog
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since 10/2/17
 */
public class ConnectToWifiDialog extends Dialog implements View.OnClickListener {

    private TextView wifiName;
    private TextView wifiSecurity;
    private EditText pass;
    private Button connect;
    private ScanResult scanResult;

    private DialogListener dialogListener;

    public ConnectToWifiDialog(@NonNull Context context, ScanResult scanResult) {
        super(context);
        this.scanResult = scanResult;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_connectwifi);
        wifiName = findViewById(R.id.dialog_wifiname);
        wifiSecurity = findViewById(R.id.dialog_security);
        pass = findViewById(R.id.dialog_et);
        connect = findViewById(R.id.dialog_btn);
        connect.setOnClickListener(this);
        fillData();
    }

    private void fillData() {
        wifiName.setText(scanResult.SSID);
        String sec = WifiConnector.getWifiSecurityType(scanResult);

        if (WifiConnector.SECURITY_NONE.equals(sec)) {
            pass.setVisibility(View.GONE);
        } else {
            pass.setVisibility(View.VISIBLE);
        }
        wifiSecurity.setText(sec);
    }

    public void setConnectButtonListener(DialogListener listener) {
        this.dialogListener = listener;
    }

    @Override
    public void onClick(View v) {
        this.dialogListener.onConnectClicked(scanResult, pass.getText().toString());
        dismiss();
    }

    interface DialogListener {
        void onConnectClicked(ScanResult scanResult, String password);
    }
}
