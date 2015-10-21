# ECBluetooth40
整理Android 蓝牙4.0 BLE 项目示例，将相关代码提取成父类，采用继承的方法进行开发。

1、继承BaseBluetoothIsOpenActivity类，可以在没有打开蓝牙进行提示，如果拒绝打开蓝牙，Activity将会finish();

2、继承BaseBluetoothScanActivity类进行蓝牙扫描，默认15秒停止。

3、继承BaseBluetoothControlActivity类进行蓝牙通信。官方示例采用的Broadcast receiver被我替换成LocalBroadcastManager进行发送了。

4、请看app module，里面加了自动重试功能。另外BluetoothLeService的BluetoothGattCallback接口额外加了一些判断，处理了一些蓝牙可能无法连接的情况。
