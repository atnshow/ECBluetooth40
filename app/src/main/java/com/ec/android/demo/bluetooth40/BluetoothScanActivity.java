/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ec.android.demo.bluetooth40;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ec.android.demo.bluetooth40.adapter.BluetoothScanAdapter;
import com.ec.android.module.bluetooth40.base.BaseBluetoothControlActivity;
import com.ec.android.module.bluetooth40.base.BaseBluetoothScanActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描蓝牙
 *
 * @author ZLJ
 */
public class BluetoothScanActivity extends BaseBluetoothScanActivity {

    private Toolbar mToolbar;
    //
    private MenuItem scanItem;
    //
    private ListView mLv;
    //
    private BluetoothScanAdapter mDataAdapter;

    private List<BluetoothDevice> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        //
        initToolbar();
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //
        // Initializes list view adapter.
        if (mDataAdapter == null) {
            mDataAdapter = new BluetoothScanAdapter(this, mDataList);
            mLv.setAdapter(mDataAdapter);
        }
        //
        startScanLeDevice();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(mToolbar);
        mToolbar.setTitle("扫描蓝牙");
        //
        mToolbar.inflateMenu(R.menu.menu_scan_list);
        //
        scanItem = mToolbar.getMenu().findItem(R.id.action_scan);
    }

    private void initView() {
        mLv = (ListView) findViewById(R.id.lv1);
        //
    }

    private void initListener() {
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice entity = mDataList.get(position);
                //
                //
                if (entity != null) {
                    //如果address不是符合mac地址格式，返回
                    if (!BluetoothAdapter.checkBluetoothAddress(entity.getAddress())) {
                        Toast.makeText(BluetoothScanActivity.this, "该蓝牙的MAC地址格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //连接蓝牙
                    Intent intent = new Intent(BluetoothScanActivity.this, DeviceControlActivity.class);

                    intent.putExtra(BaseBluetoothControlActivity.EXTRAS_DEVICE_NAME, entity.getName());
                    intent.putExtra(BaseBluetoothControlActivity.EXTRAS_DEVICE_NICK_NAME, entity.getName());
                    intent.putExtra(BaseBluetoothControlActivity.EXTRAS_DEVICE_ADDRESS, entity.getAddress());

                    startActivity(intent);
                }
                //
            }
        });
    }

    @Override
    protected void startScanLeDevice() {
        super.startScanLeDevice();
        scanItemSetting(true);
    }

    @Override
    protected void stopScanLeDevice() {
        super.stopScanLeDevice();
        scanItemSetting(false);
    }

    /**
     * @param nowScanFlag 当前是否正在扫描
     */
    private void scanItemSetting(boolean nowScanFlag) {
        if (scanItem != null) {
            if (nowScanFlag) {
                scanItem.setTitle("停止扫描");
                scanItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        stopScanLeDevice();
                        return false;
                    }
                });
            } else {
                scanItem.setTitle("重新扫描");
                scanItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startScanLeDevice();
                        //清空之前扫描得到的蓝牙
                        if (mDataList != null) {
                            mDataList.clear();
                            if (mDataAdapter != null) {
                                mDataAdapter.flushData(mDataList);
                            }
                        }

                        return false;
                    }
                });
            }
        }
    }

    @Override
    protected void onLeScanCallback4OnLeScanCallback(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        if (!mDataList.contains(bluetoothDevice)) {
            mDataList.add(bluetoothDevice);
            //
            mDataAdapter.flushData(mDataList);
        }
    }

}