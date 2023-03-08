package com.microtech.aidexx.ble;

import static com.microtech.aidexx.ble.PendingIntentReceiver.REQUEST_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;

import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.microtech.aidexx.AidexxApp;
import com.microtech.aidexx.ble.device.TransmitterManager;
import com.microtech.aidexx.ble.device.model.DeviceModel;
import com.microtech.aidexx.ble.device.work.StartScanWorker;
import com.microtech.aidexx.ble.device.work.StopScanWorker;
import com.microtech.aidexx.utils.LogUtil;
import com.microtech.aidexx.utils.StringUtils;
import com.microtech.aidexx.utils.TimeUtils;
import com.microtech.aidexx.utils.eventbus.EventBusKey;
import com.microtech.aidexx.utils.eventbus.EventBusManager;
import com.microtechmd.blecomm.BleAdapter;
import com.microtechmd.blecomm.BluetoothDeviceStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * APP-SRC-A-105
 */
public class AidexBleAdapter extends BleAdapter {
    private static final int DISCOVER_TIME_OUT_SECONDS = 30;
    Handler mWorkHandler;
    private static final UUID UUID_SERVICE = UUID.fromString("0000181F-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_CHARACTERISTIC = UUID.fromString("00002AFF-0000-1000-8000-00805F9B34FB");
    //蓝牙特征值
    BluetoothGattCharacteristic mWriteGattCharacteristic;
    //蓝牙设备
    private BluetoothDevice mBluetoothDevice;

    //蓝牙服务
    private BluetoothGatt mBluetoothGatt;

    private static final int LINK_TIME_OUT = 1000;
    public static final int START_SCAN = 1001;
    private static final int STOP_SCAN = 1002;
    private static final int CONNECT_GATT = 1003;
    private static final int DISCOVER_SERVICES = 1004;
    private static final int DISCONNECT_GATT = 1005;
    private static final int CLOSE_GATT = 1006;
    private static final int SEND_DATA = 1007;
    private static final int CLOSE_GATT_ONLY = 1011;
    private static final int RECEIVER_DATA = 2000; //处理返回的数据

    private static final int CONNECT_DISCONNECTED = 1008;
    private static final int CONNECT_FAILURE = 1009;
    private static final int CONNECT_SUCCESS = 1010;
    private static final int FOUND_SERVER = 1012;
    private static final int BLE_CONNECT_TIME_OUT = 1100; //连接超时
    private static final long BLE_CONNECT_TIME_LIMIT = 30 * 1000; //连接超时30S
    long lastDisConnectTime; //上一次断开的时间
    //设置蓝牙扫描过滤器集合
    private List<ScanFilter> scanFilterList;
    //设置蓝牙扫描过滤器
    private ScanFilter.Builder scanFilterBuilder;
    //设置蓝牙扫描设置
    private ScanSettings.Builder scanSettingBuilder;

    private final BluetoothDeviceStore bluetoothDeviceStore = new BluetoothDeviceStore();

    boolean isScaning;
    long TIME_BETWEEN_CONNECT = 2L; //断开到连接的时间间隔

    PendingIntent pendingIntent;
    private boolean isOnConnectState = false;

    int retryNum = 0;

    private TimerTask timerTask;

    public static void init(Context context) {
        if (instance == null) {
            instance = new AidexBleAdapter().initialize(context);
        }
    }

    @Override
    public BluetoothDeviceStore getDeviceStore() {
        return bluetoothDeviceStore;
    }

    protected AidexBleAdapter() {
        super();
    }

    public void onScanBack(ScanResult result) {
        byte[] scanRecord = result.getScanRecord().getBytes();
        bluetoothDeviceStore.add(result);
        String address = result.getDevice().getAddress();
        int rssi = result.getRssi() + 130;
        onScanRespond(address, rssi, scanRecord);
        onAdvertise(address, rssi, scanRecord);
    }

    final android.bluetooth.le.ScanCallback scanCallback = new android.bluetooth.le.ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bluetoothDeviceStore.add(result);
            byte[] scanRecord = result.getScanRecord().getBytes();
            String address = result.getDevice().getAddress();
            int rssi = result.getRssi() + 130;
            onScanRespond(address, rssi, scanRecord);
            onAdvertise(address, rssi, scanRecord);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            LogUtil.eAiDEX("onScanFailed errorCode:" + errorCode);
        }
    };

    private BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();//用默认的
    }

    void refreshConnectState(Boolean isConnect) {
        isOnConnectState = isConnect;
    }

    public AidexBleAdapter initialize(Context context) {
        setDiscoverTimeoutSeconds(DISCOVER_TIME_OUT_SECONDS);
        mWorkHandler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case BLE_CONNECT_TIME_OUT:

                    case CONNECT_FAILURE:
                        mWorkHandler.removeMessages(BLE_CONNECT_TIME_OUT);
                        refreshConnectState(false);
                        onConnectFailure();
                        break;

                    case DISCONNECT_GATT:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(AidexxApp.instance, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                LogUtil.eAiDEX("permission denied ----> Manifest.permission.BLUETOOTH_CONNECT");
                                return;
                            }
                        }
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.disconnect();
                        }
                        break;
                    case CONNECT_DISCONNECTED:
                        refreshConnectState(false);
                        onDisconnected();
                        break;
                    case CONNECT_SUCCESS:
                        mWorkHandler.removeMessages(BLE_CONNECT_TIME_OUT);
                        onConnectSuccess();
                        break;
                    case START_SCAN:
                        startBtScan(true);
                        break;
                    case FOUND_SERVER:
                        int status = (int) msg.obj;
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            //根据指定的服务uuid获取指定的服务
                            BluetoothGattService gattService = mBluetoothGatt.getService(UUID_SERVICE);
                            //根据指定特征值uuid获取指定的特征值
                            if (gattService != null) {
                                mWriteGattCharacteristic = gattService.getCharacteristic(UUID_CHARACTERISTIC);
                                if (mWriteGattCharacteristic == null) {
                                    LogUtil.eAiDEX("specific characteristic not found");
                                    return;
                                }
                                //设置特征值通知,即设备的值有变化时会通知该特征值，即回调方法onCharacteristicChanged会有该通知
                                boolean enable = mBluetoothGatt.setCharacteristicNotification(mWriteGattCharacteristic, true);
                                LogUtil.eAiDEX("setCharacteristicNotification:" + enable);
                                if (enable) {
                                    for (BluetoothGattDescriptor dp : mWriteGattCharacteristic.getDescriptors()) {
                                        if ((mWriteGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        }
                                        mBluetoothGatt.writeDescriptor(dp);
                                    }
                                }
                            }
                            return;
                        }
                        if (status == 133) {//需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                            mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                            retry();
                        }
                        break;
                    case CONNECT_GATT:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(AidexxApp.instance, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                LogUtil.eAiDEX("permission denied ----> Manifest.permission.BLUETOOTH_CONNECT");
                                return;
                            }
                        }
                        mWorkHandler.removeMessages(BLE_CONNECT_TIME_OUT);
                        mWorkHandler.sendEmptyMessageDelayed(BLE_CONNECT_TIME_OUT, BLE_CONNECT_TIME_LIMIT);
                        LogUtil.eAiDEX("start connect" + mBluetoothDevice.getAddress());
                        closeGatt();
                        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                        break;
                    case DISCOVER_SERVICES:
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.discoverServices();
                        }
                        break;
                    case CLOSE_GATT://需要disconnect()方法后回调onConnectionStateChange，再调用close()，
                        refreshConnectState(false);
                        closeGatt();
                        break;
                    case SEND_DATA:
                        sendData((byte[]) msg.obj);
                        break;
                    case RECEIVER_DATA:
                        onReceiveData((byte[]) msg.obj);
                        break;
                }
            }
        };
        return this;
    }

    @SuppressLint("MissingPermission")
    private void closeGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private synchronized void sendData(byte[] data) {
        try {
            if (data.length <= 20) {
                if (mWriteGattCharacteristic == null) {
                    LogUtil.eAiDEX("send data error ----> characteristic is null");
                    return;
                }
                if (mBluetoothGatt == null) {
                    LogUtil.eAiDEX("send data error ----> gatt is null");
                    return;
                }
                sleep();
                mWriteGattCharacteristic.setValue(data);
                mBluetoothGatt.writeCharacteristic(mWriteGattCharacteristic);
                if (timerTask != null) {
                    timerTask.cancel();
                    timerTask = null;
                }
                Timer timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        executeDisconnect();
                    }
                };
                timer.schedule(timerTask, 1000);
            } else {
                byte[] b1 = new byte[20];
                byte[] b2 = new byte[data.length - 20];
                System.arraycopy(data, 0, b1, 0, 20);
                System.arraycopy(data, 20, b2, 0, data.length - 20);
                sendData(b1);
                sendData(b2);
            }
        } catch (Exception e) {
            LogUtil.eAiDEX("send data error ----> " + e);
            e.printStackTrace();
        }
    }

    public List<ScanFilter> buildScanFilters() {
        if (scanFilterList == null) {
            scanFilterList = new ArrayList<>();
            // 通过服务 uuid 过滤自己要连接的设备过滤器搜索GATT服务UUID
            scanFilterBuilder = new ScanFilter.Builder();
            ParcelUuid parcelUuidMask = ParcelUuid.fromString("0000181F-0000-1000-8000-00805F9B34FB");
            ParcelUuid parcelUuid = ParcelUuid.fromString("00002AFF-0000-1000-8000-00805F9B34FB");
//        scanFilterBuilder.setServiceUuid(parcelUuid, parcelUuidMask);
            scanFilterList.add(scanFilterBuilder.build());
        }
        return scanFilterList;
    }

    public ScanSettings buildScanSettings() {
        if (scanSettingBuilder == null) {
            scanSettingBuilder = new ScanSettings.Builder();
            //设置蓝牙LE扫描的扫描模式。
            //使用最高占空比进行扫描。建议只在应用程序处于此模式时使用此模式在前台运行
            scanSettingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
            //在主动模式下，即使信号强度较弱，hw也会更快地确定匹配.在一段时间内很少有目击/匹配。
            scanSettingBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            //设置蓝牙LE扫描的回调类型
            //为每一个匹配过滤条件的蓝牙广告触发一个回调。如果没有过滤器是活动的，所有的广告包被报告
            if (BluetoothAdapter.getDefaultAdapter().isOffloadedScanBatchingSupported()) {
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                //设置为0以立即通知结果
                scanSettingBuilder.setReportDelay(0L);
            }
            scanSettingBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        }
        return scanSettingBuilder.build();
    }

    @Override
    public void executeStartScan() {
        startBtScan(true);
    }

    @Override
    public void startBtScan(Boolean isPeriodic) {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter == null || bluetoothAdapter.getBluetoothLeScanner() == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(AidexxApp.instance, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                LogUtil.eAiDEX("permission denied ----> Manifest.permission.BLUETOOTH_SCAN");
                return;
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Intent intent = new Intent(AidexxApp.instance, PendingIntentReceiver.class);
//            intent.setAction(PendingIntentReceiver.ACTION);
//            pendingIntent = PendingIntent.getBroadcast(AidexxApp.instance, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//            bluetoothAdapter.getBluetoothLeScanner().startScan(buildScanFilters(), buildScanSettings(), pendingIntent);
//        } else {
            bluetoothAdapter.getBluetoothLeScanner().startScan(buildScanFilters(), buildScanSettings(), scanCallback);
//        }
        if (!isOnConnectState && isPeriodic) {
            WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(String.valueOf(STOP_SCAN));
            if (TransmitterManager.Companion.instance().getDefault() != null) {
                OneTimeWorkRequest scanWorker = new OneTimeWorkRequest.Builder(StopScanWorker.class).setInitialDelay(30, TimeUnit.SECONDS).addTag(String.valueOf(STOP_SCAN)).build(); //30s以后停止扫描
                WorkManager.getInstance(AidexxApp.instance).enqueue(scanWorker);
            }
        }
    }

    @Override
    public void stopBtScan(Boolean isPeriodic) {
        BluetoothLeScanner bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        if (bluetoothLeScanner != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(AidexxApp.instance, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    LogUtil.eAiDEX("permission denied---->Manifest.permission.BLUETOOTH_SCAN");
                    return;
                }
            }
            bluetoothLeScanner.stopScan(scanCallback);
        }
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(String.valueOf(START_SCAN));
        DeviceModel aDefault = TransmitterManager.Companion.instance().getDefault();
        if (aDefault != null && aDefault.getEntity().getAccessId() != null && !isOnConnectState && isPeriodic) {
            OneTimeWorkRequest scanWorker = new OneTimeWorkRequest.Builder(StartScanWorker.class).setInitialDelay(2, TimeUnit.SECONDS).addTag(String.valueOf(START_SCAN)).build(); //2S后开启扫描
            WorkManager.getInstance(AidexxApp.instance).enqueue(scanWorker);
        }
    }


    @Override
    public void executeStopScan() {
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(String.valueOf(STOP_SCAN));
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(String.valueOf(START_SCAN));
        stopBtScan(true);
    }

    @Override
    public boolean isReadyToConnect(String mac) {
        ScanResult result = bluetoothDeviceStore.getDeviceMap().get(mac);
        BluetoothDevice bluetoothLeDevice = null;
        if (result != null) {
            bluetoothLeDevice = result.getDevice();
        }
        LogUtil.eAiDEX("Device:" + mac + " isReadyToConnect :" + (bluetoothLeDevice != null));
        return bluetoothLeDevice != null;
    }

    @Override
    public void executeConnect(String mac) {
        refreshConnectState(true);
        LogUtil.eAiDEX("Connecting to:" + mac);
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(String.valueOf(START_SCAN));
        ScanResult result = bluetoothDeviceStore.getDeviceMap().get(mac);
        if (result != null) {
            mBluetoothDevice = result.getDevice();
        }
        long duration = TimeUtils.INSTANCE.getCurrentTimeMillis() / 1000 - lastDisConnectTime;
        if (duration >= TIME_BETWEEN_CONNECT) {
            mWorkHandler.sendEmptyMessage(CONNECT_GATT);
        } else {
            mWorkHandler.sendEmptyMessageDelayed(CONNECT_GATT, (TIME_BETWEEN_CONNECT - duration) * 1000);
        }
    }

    @Override
    public void executeDisconnect() {
        LogUtil.eAiDEX("Disconnecting");
        if (mBluetoothGatt != null) {
            mWorkHandler.sendEmptyMessage(DISCONNECT_GATT);
        } else {
            LogUtil.eAiDEX("Gatt is null,call onDisconnected directly");
            onDisconnected();
        }
    }

    @Override
    public void executeWrite(byte[] data) {
        Message message = Message.obtain();
        message.what = SEND_DATA;
        message.obj = data;
        mWorkHandler.sendMessage(message);
    }


    /**
     * 回调都是在子线程中，不可做更新 UI 操作
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        //status-->操作是否成功，如连接成功这个操作是否成功。会返回异常码
        //newState-->新的连接的状态。共四种：STATE_DISCONNECTED，STATE_CONNECTING，STATE_CONNECTED，STATE_DISCONNECTING
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                        lastDisConnectTime = TimeUtils.INSTANCE.getCurrentTimeMillis() / 1000;
                        mWorkHandler.sendEmptyMessageDelayed(CONNECT_DISCONNECTED, 2000);
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        mWorkHandler.sendEmptyMessage(DISCOVER_SERVICES);
                        break;
                }
                retryNum = 0;
                return;
            }
            if (status == 257) {
                mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                mWorkHandler.sendEmptyMessage(CONNECT_DISCONNECTED);
                EventBusManager.INSTANCE.send(EventBusKey.EVENT_RESTART_BLUETOOTH, true);
            }
            if (status == 133) {
                if (retryNum < 2) {//需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                    mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                    retry();
                } else {
                    retryNum = 0;
                    mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                    mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
                }
                return;
            }
            mWorkHandler.sendEmptyMessage(CLOSE_GATT);
            mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
        }


        //发现服务成功后，会触发该回调方法。status：远程设备探索是否成功
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Message message = Message.obtain();
                message.what = FOUND_SERVER;
                message.obj = status;
                mWorkHandler.sendMessage(message);
            } else {
                mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            LogUtil.eAiDEX("onCharacteristicRead -->" + StringUtils.INSTANCE.binaryToHexString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.eAiDEX("Send data success -->" + StringUtils.INSTANCE.binaryToHexString(characteristic.getValue()));
            } else LogUtil.eAiDEX("Send data failure");
        }

        //设备的值有变化时会主动返回
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            LogUtil.eAiDEX("onCharacteristicChanged -->" + StringUtils.INSTANCE.binaryToHexString(characteristic.getValue()));
            if (mBluetoothGatt == null) {
                LogUtil.eAiDEX("onCharacteristicChanged --> Gatt is null");
                return;
            }
            Message message = Message.obtain();
            message.what = RECEIVER_DATA;
            message.obj = characteristic.getValue();
            mWorkHandler.sendMessage(message);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            LogUtil.eAiDEX("onDescriptorRead -->" + "status:" + status + " uuid" + descriptor.getUuid());
        }

        //设置Descriptor后回调
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.eAiDEX("onDescriptorWrite -->" + "Descriptor enable success. uuid:" + descriptor.getUuid());
                mWorkHandler.sendEmptyMessage(CONNECT_SUCCESS);
            } else {
                mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
                LogUtil.eAiDEX("onDescriptorWrite -->" + "Descriptor enable fail");
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private void retry() {
        retryNum++;
        mWorkHandler.sendEmptyMessage(CONNECT_GATT);
    }
}



