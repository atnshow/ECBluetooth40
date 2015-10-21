package com.ec.android.demo.bluetooth40;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ec.android.module.bluetooth40.BluetoothLeService;
import com.ec.android.module.bluetooth40.SampleGattAttributes;
import com.ec.android.module.bluetooth40.base.BaseBluetoothControlActivity;

import java.util.List;


public class DeviceControlActivity extends BaseBluetoothControlActivity {
    private static final String TAG = DeviceControlActivity.class.getSimpleName();
    private static final boolean DEBUG = true;
    //
    private Toolbar mToolbar;
    //
    private Handler mHandler;
    //通知
    protected BluetoothGattCharacteristic mNotifyCharacteristic;
    //写
    protected BluetoothGattCharacteristic mWriteCharacteristic;
    //
    private static final int mAutoTime = 2500;

    private static boolean mAutoFlag = false;
    //
    Runnable mAutoConnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) {
                Toast.makeText(DeviceControlActivity.this, "自动连接中", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "自动连接中");
            }
            if (mAutoFlag) {
                if (mBluetoothLeService != null && !TextUtils.isEmpty(mDeviceAddress)) {
                    mBluetoothLeService.connect(mDeviceAddress);
                    //
                    mHandler.postDelayed(this, mAutoTime);
                }
            }
            //
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        //
        mHandler = new Handler();
        //
        initToolbar();
        initView();
        initListener();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        mAutoFlag = false;
        if (mHandler != null && mAutoConnectRunnable != null) {
            mHandler.removeCallbacks(mAutoConnectRunnable);
        }
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(mNickName);
        //
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_scan:

                        break;
                }

                return true;
            }
        });
        //
//        mToolbar.inflateMenu(R.menu.menu_device_list);
    }

    private void initView() {
        final EditText et1 = (EditText) findViewById(R.id.et1);

        View send_btn = findViewById(R.id.send_btn);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ss = et1.getText().toString();
                byte[] bytes = ss.getBytes();
                //
                if (mWriteCharacteristic != null) {
                    mWriteCharacteristic.setValue(bytes);
                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic);
                }
            }
        });
    }

    private void initListener() {

    }

    private void initData() {

    }

    @Override
    protected void onActionGattError(Intent intent) {
        Toast.makeText(this, "连接设备失败，请确保设备在范围内", Toast.LENGTH_SHORT).show();
        //
        autoConnect(true);
    }

    @Override
    protected void onActionGattConnected(Intent intent) {
        Toast.makeText(this, "已经连接设备", Toast.LENGTH_SHORT).show();
        //
        autoConnect(false);
    }

    @Override
    protected void onActionGattDisConnected(Intent intent) {
        Toast.makeText(this, "已经断开设备", Toast.LENGTH_SHORT).show();
        autoConnect(true);
    }

    @Override
    protected void onActionGattServicesDiscovered(Intent intent) {
        processGattServices(mBluetoothLeService.getSupportedGattServices());
    }

    @Override
    protected void onActionDataAvailable(Intent intent) {
        byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            String s = data.toString() + "\n" + stringBuilder.toString();
            Log.d(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 自动连接
     */
    private void autoConnect(boolean flag) {
        if (mHandler != null && mAutoConnectRunnable != null) {
            if (flag) {
                //重连
                mAutoFlag = true;
                mHandler.postDelayed(mAutoConnectRunnable, 100);
            } else {
                mAutoFlag = false;
                mHandler.removeCallbacks(mAutoConnectRunnable);
            }
        }
    }

    /**
     * @param gattServices
     */
    private void processGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        //
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            //
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                String uuid = characteristic.getUuid().toString();
                //通知
                if (uuid.equals(SampleGattAttributes.NOTIFY_CHARACTERISTIC_CONFIG)) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                }
                //写
                if (uuid.equals(SampleGattAttributes.WRITE_CHARACTERISTIC_CONFIG)) {
                    mWriteCharacteristic = characteristic;
                }
                //
            }
        }
        //成功发现服务
        if (DEBUG) {
            Toast.makeText(this, "成功发现服务!!", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "成功发现服务!!");
        }
    }

}
