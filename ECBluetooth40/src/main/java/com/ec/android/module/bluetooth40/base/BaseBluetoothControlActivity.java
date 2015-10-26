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

package com.ec.android.module.bluetooth40.base;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ec.android.module.bluetooth40.BluetoothLeService;

import org.apache.commons.lang.ArrayUtils;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public abstract class BaseBluetoothControlActivity extends BaseBluetoothIsOpenActivity {
    private final static String TAG = BaseBluetoothControlActivity.class.getSimpleName();
    //
    private LocalBroadcastManager mLocalBroadcastManager;
    //
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_NICK_NAME = "DEVICE_NICK_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    //    private TextView mConnectionState;
//    private TextView mDataField;
    protected String mDeviceName;
    protected String mNickName;
    protected String mDeviceAddress;
    //    private ExpandableListView mGattServicesList;
    protected BluetoothLeService mBluetoothLeService;

    protected boolean mConnected = false;
    //通知
    protected BluetoothGattCharacteristic mNotifyCharacteristic;
    //写
    protected BluetoothGattCharacteristic mWriteCharacteristic;
    //
//    protected BluetoothGattCharacteristic mNotifyCharacteristic;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    /*
    */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_ERROR.equals(action)) {
//                mConnected = true;
                onActionGattError(intent);
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                onActionGattConnected(intent);
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                onActionGattDisConnected(intent);
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //FIXME
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                onActionGattServicesDiscovered(intent);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Byte[] bytes = ArrayUtils.toObject(data);
                //FIXME
                onActionDataAvailable(bytes);
            }
        }
    };

    protected abstract void onActionGattError(Intent intent);

    protected abstract void onActionGattConnected(Intent intent);

    protected abstract void onActionGattDisConnected(Intent intent);

    protected abstract void onActionGattServicesDiscovered(Intent intent);

    protected abstract void onActionDataAvailable(Byte[] data);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        //
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mNickName = intent.getStringExtra(EXTRAS_DEVICE_NICK_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_ERROR);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * 写
     * FIXME
     *
     * @param content
     */
    protected void writeContent(String content) {
        writeContent(ArrayUtils.toObject(content.getBytes()));
    }

    protected void writeContent(Byte[] bytes) {

        if (mWriteCharacteristic != null) {
            mWriteCharacteristic.setValue(ArrayUtils.toPrimitive(bytes));
            mBluetoothLeService.writeCharacteristic(mWriteCharacteristic);
        }

    }

    /**
     * 初始化 通知 的writeCharacteristic
     */
    protected void initNotifyCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            return;
        }
        mNotifyCharacteristic = characteristic;
        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
    }

    /**
     * 初始化 写 的writeCharacteristic
     */
    protected void initWriteCharacteristic(BluetoothGattCharacteristic characteristic) {
        mWriteCharacteristic = characteristic;
    }

}
