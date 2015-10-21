package com.ec.android.demo.bluetooth40.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ec.android.demo.bluetooth40.R;

import java.util.List;

/**
 * 扫描到的蓝牙列表
 * @author ZLJ
 */
public class BluetoothScanAdapter extends BaseAdapter {
    private final Context mContext;
    private List<BluetoothDevice> dataList;

    public BluetoothScanAdapter(Context context, List<BluetoothDevice> list) {
        this.mContext = context;
        this.dataList = list;
    }

    public void flushData(List<BluetoothDevice> list) {
        this.dataList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (convertView == null) {
            viewHolder = new ViewHolder();
            //
            convertView = View.inflate(mContext, R.layout.bluetooth_scan_item, null);

            viewHolder.deviceNameTv = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceAddressTv = (TextView) convertView.findViewById(R.id.device_address);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //
        BluetoothDevice device = dataList.get(i);
        //
        final String deviceName = device.getName();
        if (!TextUtils.isEmpty(deviceName)) {
            viewHolder.deviceNameTv.setText(deviceName);
        } else {
            viewHolder.deviceNameTv.setText("未知设备");
        }
        //
        viewHolder.deviceAddressTv.setText(device.getAddress());

        return convertView;
    }

    private static class ViewHolder {
        TextView deviceNameTv;
        TextView deviceAddressTv;
    }
}
