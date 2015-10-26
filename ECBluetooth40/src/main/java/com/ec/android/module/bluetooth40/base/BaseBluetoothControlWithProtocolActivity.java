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
import com.ec.android.module.bluetooth40.protocol.Packet;
import com.ec.android.module.bluetooth40.protocol.PacketTools;
import com.ec.android.module.bluetooth40.protocol.ProtocolUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 * <p/>
 * 使用自定义蓝牙传输协议
 */
public abstract class BaseBluetoothControlWithProtocolActivity extends BaseBluetoothIsOpenActivity {
    private final static String TAG = BaseBluetoothControlWithProtocolActivity.class.getSimpleName();
    //重试次数
    private static final int RETRY_TIMES = 3;
    //超时时间
    private static final int OVERTIME = 3000;
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
                //FIXME
                onReceiveData(data);
//                onActionDataAvailable(intent);
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
        initFuture();
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
    protected void writeContent(final String content) {
        //
        ListenableFuture<Object> future = mListeningExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                processSendContent(content);
                return null;
            }
        });
        //
    }

    /**
     * 装饰ExecutorService
     */
    private ListeningExecutorService mListeningExecutorService;

    private void initFuture() {
        //单一线程池
        ExecutorService mSingleExecutorService = Executors.newSingleThreadExecutor();

        mListeningExecutorService = MoreExecutors.listeningDecorator(mSingleExecutorService);
    }

    ///////
    private ProtocolDataUtils mProtocolDataUtils = new ProtocolDataUtils();

    /**
     * 处理相关发送数据
     *
     * @param content
     */
    private void processSendContent(String content) {
        try {
            //休眠一下，避免两个子线程间轮流太快
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //
        PacketTools packetTools = new PacketTools();
        ////
        mProtocolDataUtils.putPacketTools(packetTools.getSequenceId(), packetTools);
        ////
        packetTools.setSendContent(content);

        List<Byte[]> list = packetTools.translateBytes();

        if (!list.isEmpty()) {
            Packet.Builder builder = null;
            //
//            int retryTimes = 0;
            loops:
            for (int retry = 0; retry < RETRY_TIMES; retry++) {
                //
//                if (retryTimes > 3) {
//                    break loops;
//                }
                //
                for (int i = 0; i < list.size(); i++) {
                    //
                    Byte[] payloadBytes = list.get(i);

                    if (i == 0) {
                        builder = packetTools.newMorePacketFirstBuilder();
                        //
                    } else if (i == (list.size() - 1)) {
                        builder = packetTools.newMorePacketFinalBuilder();
                    } else {
                        builder = packetTools.newMorePacketCommonBuilder();
                    }
                    //
                    int length = payloadBytes.length;
                    builder.setPayloadLength((byte) length);
                    //
                    Short crc = ProtocolUtils.getCrc(builder);
                    builder.setCrc(crc);
                    //
                    builder.setPayloadBytes(payloadBytes);
                    //
                    Packet packet = builder.create();
                    //
                    if (mWriteCharacteristic != null) {
                        mWriteCharacteristic.setValue(ArrayUtils.toPrimitive(packet.translateBytes()));
                        mBluetoothLeService.writeCharacteristic(mWriteCharacteristic);
                    }
                    //
                    int overTime = 0;
                    while (true) {
                        Boolean errFlag = mProtocolDataUtils.getErrFlag(packetTools.getSequenceId());

                        if (errFlag == null) {
                            //还没有收到errFlag
                            overTime += 100;
                            //三秒超时 FIXME
                            if (overTime >= OVERTIME) {
                                //FIXME 超时重试
//                                retryTimes++;
                                mProtocolDataUtils.putErrFlag(packetTools.getSequenceId(), null);
                                continue loops;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else if (errFlag) {
                            //FIXME 错误重试
//                            retryTimes++;
                            mProtocolDataUtils.putErrFlag(packetTools.getSequenceId(), null);
                            continue loops;
                        } else if (!errFlag) {
                            // 收到errFlag，而且是正常的
                            mProtocolDataUtils.putErrFlag(packetTools.getSequenceId(), null);
                            break;
                        }
                    }
                    //

//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                }
                //
                break loops;
            }

        }
    }


    protected void writeContent(Byte[] bytes) {
        writeContent(new String(ArrayUtils.toPrimitive(bytes)));
    }

    /**
     * 接收到设备通知（消息）
     * TODO 尚无超时处理
     *
     * @param data
     */
    private void onReceiveData(byte[] data) {
        if (data.length < 7) {
            //小于7，说明连header都不完整，无法处理
            return;
        }
        //
        byte magic = data[0];

        if (magic != Packet.MAGIC_BYTE) {
            //必须相同
            return;
        }
        //
        byte dataMuti = data[1];
        byte errFlag = (byte) (dataMuti & 0x08);
        //
        int payloadLength = data[2];

        if (payloadLength < 0) {
            payloadLength = 256 + payloadLength;
        }
        ////
        byte sequenceIdHigh8 = data[3];
        byte sequenceIdLow8 = data[4];
        //
        int sequenceIdHigh8Int = sequenceIdHigh8;
        int sequenceIdLow8Int = sequenceIdLow8;
        //
        if (sequenceIdHigh8 < 0) {
            sequenceIdHigh8Int = (256 + sequenceIdHigh8);
        }
        if (sequenceIdLow8 < 0) {
            sequenceIdLow8Int = (256 + sequenceIdLow8);
        }
        //
//        short sequenceIdTemp = 0;
        //得到sequenceId，抛弃他相关的packet
        short sequenceId = (short) ((sequenceIdHigh8Int << 8) + sequenceIdLow8Int);
        //
        if (errFlag == 0x08) {
            //失败了，即位为1
            mProtocolDataUtils.putErrFlag(sequenceId, true);
            return;
        } else {
            //成功了，即位为0
            mProtocolDataUtils.putErrFlag(sequenceId, false);
        }
        //
        byte ackFlag = (byte) (dataMuti & 0x04);
        if (ackFlag == 0x04) {
            //即位为1
            //这是返回的packet，而不是其他通知

        } else {
            //比如查询状态，设备主动推送回来
            //获取开始和结束标志
            //
            byte beginFlag = (byte) (dataMuti & 0x02);
            byte endFlag = (byte) (dataMuti & 0x01);
            //先校验，校验不通过，直接丢完所有packet
            //
            int myCrc = magic ^ data[1] ^ payloadLength ^ sequenceId;

            int crcHigh = data[5];
            int crcLow = data[6];

            if (crcHigh < 0) {
                crcHigh = 256 + crcHigh;
            }
            if (crcLow < 0) {
                crcLow = 256 + crcLow;
            }

//            int crcTemp = 0x00;
            //
            int crc = (crcHigh << 8) + crcLow;

            if (crc != myCrc) {
                //校验不通过。丢弃
                mProtocolDataUtils.removeGetDataMap(sequenceId);
                return;
            }
            //获取包体字节数组
            byte[] bodyBytes = ArrayUtils.subarray(data, 7, data.length);
            //
            if (beginFlag == 0x02) {
                //即位为1，即这是第一个packet
                //第一个packet的话，就得先删除之前相关sequenceId的内容，避免重复，然后在重新加入
                mProtocolDataUtils.removeGetDataMap(sequenceId);
                mProtocolDataUtils.plusGetDataMap(sequenceId, ArrayUtils.toObject(bodyBytes));
            } else {
                //即位为0，即这不是第一个packet
                //这得判断之前是否已经有相关sequenceId的内容，如果没有，那肯定是错序或其它方面的数据包，丢弃并返回
                ArrayList<Byte[]> getDataMapList = mProtocolDataUtils.getGetDataMap(sequenceId);
                if (getDataMapList == null || getDataMapList.isEmpty()) {
                    return;
                } else {
                    //如果已经有数据，那就添加进去呗
                    mProtocolDataUtils.plusGetDataMap(sequenceId, ArrayUtils.toObject(bodyBytes));
                }
            }
            //
            if (endFlag == 0x01) {
                //即位为1，即这是最后一个packet
                //组装数据，回调给用户处理。
                ArrayList<Byte[]> getDataMapList = mProtocolDataUtils.getGetDataMap(sequenceId);

                if (getDataMapList != null) {
                    Byte[] myAllBytes = null;
                    for (Byte[] bytes : getDataMapList) {
                        myAllBytes = (Byte[]) ArrayUtils.addAll(myAllBytes, bytes);
                    }
                    //可以删除了
                    mProtocolDataUtils.removeGetDataMap(sequenceId);
                    //回调
                    onActionDataAvailable(myAllBytes);
                }
            }
        }
        //
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

    private static class ProtocolDataUtils {
        //
        private Map<Short, PacketTools> mPacketToolsMap = new HashMap<>();
        //返回ack的次数
        private Map<Short, Integer> mPacketToolsAckLengthMap = new HashMap<>();
        //返回Err
        private Map<Short, Boolean> mPacketToolsErrFlagMap = new HashMap<>();
        //重试的次数
        private Map<Short, Integer> mPacketToolsRetryMap = new HashMap<>();
        //接收到设备数据的缓存
        private Map<Short, ArrayList<Byte[]>> mGetDataMap = new HashMap<>();

        public void putPacketTools(short sequenceId, PacketTools packetTools) {
            mPacketToolsMap.put(sequenceId, packetTools);
        }

        public PacketTools getPacketTools(short sequenceId) {
            return mPacketToolsMap.get(sequenceId);
        }

        public void removePacketTools(short sequenceId) {
            mPacketToolsMap.put(sequenceId, null);
        }

        public void plusAckLength(short sequenceId) {
            Integer ackLength = mPacketToolsAckLengthMap.get(sequenceId);
            if (ackLength == null) {
                mPacketToolsAckLengthMap.put(sequenceId, 1);
            } else {
                mPacketToolsAckLengthMap.put(sequenceId, ackLength++);
            }
        }

        public int getAckLength(short sequenceId) {
            Integer ackLength = mPacketToolsAckLengthMap.get(sequenceId);
            if (ackLength == null) {
                return 0;
            }
            return ackLength;
        }

        public void removeAckLength(short sequenceId) {
            mPacketToolsAckLengthMap.put(sequenceId, null);
        }


        public void putErrFlag(short sequenceId, Boolean b) {
            mPacketToolsErrFlagMap.put(sequenceId, b);
        }

        public Boolean getErrFlag(short sequenceId) {
            return mPacketToolsErrFlagMap.get(sequenceId);

        }

        public void removeErrFlag(short sequenceId) {
            mPacketToolsErrFlagMap.put(sequenceId, null);
        }

        public void plusRetryMapLength(short sequenceId) {
            Integer ackLength = mPacketToolsRetryMap.get(sequenceId);
            if (ackLength == null) {
                mPacketToolsRetryMap.put(sequenceId, 1);
            } else {
                mPacketToolsRetryMap.put(sequenceId, ackLength++);
            }
        }

        public int getRetryMapLength(short sequenceId) {
            Integer length = mPacketToolsRetryMap.get(sequenceId);
            if (length == null) {
                return 0;
            }
            return length;
        }

        public void removeRetryMapLength(short sequenceId) {
            mPacketToolsRetryMap.put(sequenceId, null);
        }

        public void plusGetDataMap(short sequenceId, Byte[] bytes) {
            ArrayList<Byte[]> list = mGetDataMap.get(sequenceId);

            if (list == null) {
                list = new ArrayList<>();
            }
            //
            list.add(bytes);
            mGetDataMap.put(sequenceId, list);
        }

        public ArrayList<Byte[]> getGetDataMap(short sequenceId) {
            ArrayList<Byte[]> list = mGetDataMap.get(sequenceId);
            return list;
        }

        public void removeGetDataMap(short sequenceId) {
            mGetDataMap.put(sequenceId, null);
        }

    }
}
