/*
 * Created by Jose Flavio on 2/5/18 7:50 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.jflavio1.wificonnectorsample;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * WifiListRv
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since 5/2/17
 */
public class WifiListRvAdapter extends RecyclerView.Adapter<WifiListRvAdapter.WifiItem> {

    private List<ScanResult> scanResultList = new ArrayList<>();
    private WifiItemListener wifiItemListener;

    public WifiListRvAdapter(WifiItemListener wifiItemListener) {
        this.wifiItemListener = wifiItemListener;
    }

    public void setScanResultList(List<ScanResult> scanResultList) {
        this.scanResultList = scanResultList;
        notifyDataSetChanged();
    }

    @Override
    public WifiItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WifiItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.accesspoint_item, parent, false));
    }

    @Override
    public void onBindViewHolder(WifiItem holder, final int position) {
        holder.fill(scanResultList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiItemListener.onWifiItemClicked(scanResultList.get(position));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                wifiItemListener.onWifiItemLongClick(scanResultList.get(position));
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.scanResultList.size();
    }

    static class WifiItem extends RecyclerView.ViewHolder {

        private TextView wifiName;
        private TextView wifiIntensity;

        public WifiItem(View itemView) {
            super(itemView);
            wifiName = itemView.findViewById(R.id.apItem_name);
            wifiIntensity = itemView.findViewById(R.id.apItem_intensity);
        }

        @SuppressLint("SetTextI18n")
        public void fill(ScanResult scanResult) {
            wifiName.setText(scanResult.SSID);
            wifiIntensity.setText(WifiManager.calculateSignalLevel(scanResult.level, 100) + "%");
        }

    }

    interface WifiItemListener {
        void onWifiItemClicked(ScanResult scanResult);

        void onWifiItemLongClick(ScanResult scanResult);
    }

}
