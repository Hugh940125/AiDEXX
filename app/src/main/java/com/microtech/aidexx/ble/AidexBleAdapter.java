package com.microtech.aidexx.ble;

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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.microtech.aidexx.AidexxApp;
import com.microtech.aidexx.utils.LogUtil;
import com.microtechmd.blecomm.BleAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private String TAG = "BLE";

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
    long TIME_BETWEEN_CONNECT = 3L; //断开到连接的时间间隔

    PendingIntent pendingIntent;
    private boolean isOnConnectState = false;

    public static void init(Context context) {
        if (instance == null) {
            instance = new AidexBleAdapter().initialize(context);
        }
    }

    protected AidexBleAdapter() {
        super();
    }


    final android.bluetooth.le.ScanCallback scanCallback = new android.bluetooth.le.ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bluetoothDeviceStore.add(result.getDevice());
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
                        startScan();
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
                            gattError133("onServicesDiscovered");
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

    TimerTask timerTask;

    private void sleep(long time) {
        try {
            Thread.sleep(time);
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
                sleep(20);
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
        startScan();
    }

    private void startScan() {
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
        bluetoothAdapter.getBluetoothLeScanner().startScan(buildScanFilters(), buildScanSettings(), scanCallback);
        LogUtil.eAiDEX("start scan");
        if (!isOnConnectState) {
            WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(String.valueOf(STOP_SCAN));
            if (TransmitterManager.instance().getDefaultModel() != null) {
                OneTimeWorkRequest scanWorker = new OneTimeWorkRequest.Builder(BleStopScanWorker.class).setInitialDelay(30, TimeUnit.SECONDS).addTag(String.valueOf(STOP_SCAN)).build(); //30s以后停止扫描
                WorkManager.getInstance(CgmsApplication.instance).enqueue(scanWorker);
            }
        }
    }

    private void stopScan() {
        BluetoothLeScanner bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        if (bluetoothLeScanner != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(CgmsApplication.instance, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    LogUtils.eAiDex("缺少权限---->Manifest.permission.BLUETOOTH_SCAN");
                    return;
                }
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (pendingIntent != null) {
//                    bluetoothLeScanner.stopScan(pendingIntent);
//                    pendingIntent.cancel();
//                    pendingIntent = null;
//                }
//            } else {
            bluetoothLeScanner.stopScan(scanCallback);
//            }
        }
        WorkManager.getInstance(CgmsApplication.instance).cancelAllWorkByTag(String.valueOf(START_SCAN));
        if (TransmitterManager.instance().getDefaultModel() != null && Objects.requireNonNull(TransmitterManager.instance().getDefaultModel()).getEntity().getAccessId() != null && !isOnConnectState) {
            LogUtils.debug("2S后开启扫描");
            OneTimeWorkRequest scanWorker = new OneTimeWorkRequest.Builder(BleScanWorker.class).setInitialDelay(2, TimeUnit.SECONDS).addTag(String.valueOf(START_SCAN)).build(); //2S后开启扫描
            WorkManager.getInstance(CgmsApplication.instance).enqueue(scanWorker);
        }
    }


    @Override
    public void executeStopScan() {
        LogUtils.error("BLE", "停止扫描");
        WorkManager.getInstance(CgmsApplication.instance).cancelAllWorkByTag(String.valueOf(STOP_SCAN));
        WorkManager.getInstance(CgmsApplication.instance).cancelAllWorkByTag(String.valueOf(START_SCAN));
        stopScan();
    }

    @Override
    public boolean isReadyToConnect(String mac) {
        BluetoothDevice bluetoothLeDevice = bluetoothLeDeviceStore.getDeviceMap().get(mac);
        LogUtils.error("BLE" + mac, "BLE isReadyToConnect :" + (bluetoothLeDevice != null) + "--" + bluetoothLeDeviceStore.toString());
        return bluetoothLeDevice != null;
    }

    @Override
    public void executeConnect(String mac) {
        isOnConnectState = true;
        LogUtils.eAiDex(" 开始连接: " + mac);
        WorkManager.getInstance(CgmsApplication.instance).cancelAllWorkByTag(String.valueOf(START_SCAN));
        mBluetoothDevice = bluetoothLeDeviceStore.getDeviceMap().get(mac);
        long duration = new Date().getTime() / 1000 - lastDisConnectTime;

        if (duration >= TIME_BETWEEN_CONNECT) {
            mWorkHandler.sendEmptyMessage(CONNECT_GATT);
        } else {
            LogUtils.error("延长发送消息 " + (TIME_BETWEEN_CONNECT - duration) * 1000);
            mWorkHandler.sendEmptyMessageDelayed(CONNECT_GATT, (TIME_BETWEEN_CONNECT - duration) * 1000);
        }
    }

    @Override
    public void executeDisconnect() {

        LogUtils.error("BLE", "BLE executeDisconnect");

        if (mBluetoothGatt != null) {
            mWorkHandler.sendEmptyMessage(DISCONNECT_GATT);
        } else {
            LogUtils.error("BLE executeDisconnect  mBluetoothGatt NULL");
            onConnectFailure();
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
            Log.d(TAG, "onPhyUpdate");
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.d(TAG, "onPhyRead");
        }

        //
//status-->操作是否成功，如连接成功这个操作是否成功。会返回异常码
//newState-->新的连接的状态。共四种：STATE_DISCONNECTED，STATE_CONNECTING，STATE_CONNECTED，STATE_DISCONNECTING
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.e(TAG, "BluetoothGattCallback：onConnectionStateChange-->" + "status：" + status + "操作成功；" + " newState：" + newState + " 已断开连接状态");
//                        workHandler.sendEmptyMessage(DISCONNECT_GATT);
                        mWorkHandler.sendEmptyMessage(CLOSE_GATT);

                        LogUtils.eAiDex("断开连接成功...");
                        lastDisConnectTime = new Date().getTime() / 1000;
                        mWorkHandler.sendEmptyMessageDelayed(CONNECT_DISCONNECTED, 2000);

                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.d(TAG, "BluetoothGattCallback：onConnectionStateChange-->" + "status：" + status + "操作成功；" + " newState：" + newState + " 已连接状态，可进行发现服务");
                        //发现服务
                        mWorkHandler.sendEmptyMessage(DISCOVER_SERVICES);
                        break;
                }
                retryNum = 0;
                return;
            }


            if (status == 257) {
                LogUtils.error("蓝牙超过连接次数了,需要重启");
                mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                mWorkHandler.sendEmptyMessage(CONNECT_DISCONNECTED);
                LiveEventBus.get(EventKey.MESSAGE_COME).post(new com.microtechmd.cgms.entity.Message(ERROR_BLE, Constant.MESSAGE_BLE_ERROR));
            }

            if ((status != 133) && newState == 0) {
                LogUtils.error("status 59 自然断开");
                mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
                return;
            }

            if (status == 133) {
                if (retryNum < 2) {//需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                    mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                    gattError133("onConnectionStateChange");
                } else {
                    retryNum = 0;
                    mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                    mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
                }
                return;
            }

            if (status != 0) {
                mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
            }

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


        //接收到的数据，不一定会回调该方法
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead-->" + characteristic.getValue().toString());
        }

        //发送数据后的回调，可以在此检测发送的数据包是否有异常
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.eAiDex("onCharacteristicWrite:发送数据成功：" + binaryToHexString(characteristic.getValue()));
            } else LogUtils.eAiDex("onCharacteristicWrite:发送数据失败");
        }

        //设备的值有变化时会主动返回
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            LogUtils.error(TAG, "onCharacteristicChanged-->" + binaryToHexString(characteristic.getValue()));
            if (mBluetoothGatt == null) {
                Log.d(TAG, "收到数据包-->mBluetoothGatt为null 不处理数据" + binaryToHexString(characteristic.getValue()));
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
            LogUtils.debug("onDescriptorRead-->" + "status:" + status + descriptor.getUuid());
        }

        //设置Descriptor后回调
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.debug("onDescriptorWrite-->" + "描述符写入操作成功，蓝牙连接成功并可以通信成功！！！" + descriptor.getUuid());
                mWorkHandler.sendEmptyMessage(CONNECT_SUCCESS);
            } else {

                mWorkHandler.sendEmptyMessage(CLOSE_GATT);
                mWorkHandler.sendEmptyMessage(CONNECT_FAILURE);
                LogUtils.debug("onDescriptorWrite-->" + "描述符写入操作失败，蓝牙通信失败...");
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d(TAG, "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAG, "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAG, "onMtuChanged");
        }
    };


    int retryNum = 0;

    //Gatt操作失败status为133时
    private void gattError133(String method) {
        LogUtils.error("BluetoothGattCallback：" + method + "--> 因status=133，所以将关闭Gatt重新连接...");
        retryNum++;
        mWorkHandler.sendEmptyMessage(CONNECT_GATT);
    }

    /**
     * 断开连接
     *
     * @param isNeedClose 执行mBluetoothGatt.disconnect方法后是否需要执行mBluetoothGatt.close方法
     *                    执行
     */
    public void disConnect(boolean isNeedClose) {


    }


    /**
     * @param bytes
     * @return 将二进制转换为十六进制字符输出
     * new byte[]{0b01111111}-->"7F" ;  new byte[]{0x2F}-->"2F"
     */
    public static String binaryToHexString(byte[] bytes) {
        String result = "";
        if (bytes == null) {
            return result;
        }
        String hex = "";
        for (int i = 0; i < bytes.length; i++) {
            //字节高4位
            hex = String.valueOf("0123456789ABCDEF".charAt((bytes[i] & 0xF0) >> 4));
            //字节低4位
            hex += String.valueOf("0123456789ABCDEF".charAt(bytes[i] & 0x0F));
            result += hex + ",";
        }
        return result;
    }


    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    public boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    LogUtils.error("refreshDeviceCache-->" + "清理本地的BluetoothGatt 的缓存 " + bool);
                    return bool;
                }
            } catch (Exception localException) {
                Log.i(TAG, "An exception occured while refreshing device");
            }
        }
        return false;
    }


    public static boolean releaseAllScanClient() {
        try {
            Object mIBluetoothManager = getIBluetoothManager(BluetoothAdapter.getDefaultAdapter());
            if (mIBluetoothManager == null) return false;
            Object iGatt = getIBluetoothGatt(mIBluetoothManager);
            if (iGatt == null) return false;

            Method unregisterClient = getDeclaredMethod(iGatt, "unregisterClient", int.class);
            Method stopScan;
            int type;
            try {
                type = 0;
                stopScan = getDeclaredMethod(iGatt, "stopScan", int.class, boolean.class);
            } catch (Exception e) {
                type = 1;
                stopScan = getDeclaredMethod(iGatt, "stopScan", int.class);
            }

            for (int mClientIf = 0; mClientIf <= 40; mClientIf++) {
                if (type == 0) {
                    try {
                        stopScan.invoke(iGatt, mClientIf, false);
                    } catch (Exception ignored) {
                    }
                }
                if (type == 1) {
                    try {
                        stopScan.invoke(iGatt, mClientIf);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    unregisterClient.invoke(iGatt, mClientIf);
                } catch (Exception ignored) {
                }
            }
            stopScan.setAccessible(false);
            unregisterClient.setAccessible(false);
//            BLESupport.getDeclaredMethod(iGatt, "unregAll").invoke(iGatt);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("PrivateApi")
    public static Object getIBluetoothGatt(Object mIBluetoothManager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getBluetoothGatt = getDeclaredMethod(mIBluetoothManager, "getBluetoothGatt");
        Object object = new Object();
        try {
            object = getBluetoothGatt.invoke(mIBluetoothManager);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return object;
    }


    @SuppressLint("PrivateApi")
    public static Object getIBluetoothManager(BluetoothAdapter adapter) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getBluetoothManager = getDeclaredMethod(BluetoothAdapter.class, "getBluetoothManager");
        return getBluetoothManager.invoke(adapter);
    }


    public static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field declaredField = clazz.getDeclaredField(name);
        declaredField.setAccessible(true);
        return declaredField;
    }


    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method declaredMethod = clazz.getDeclaredMethod(name, parameterTypes);
        declaredMethod.setAccessible(true);
        return declaredMethod;
    }


    public static Field getDeclaredField(Object obj, String name) throws NoSuchFieldException {
        Field declaredField = obj.getClass().getDeclaredField(name);
        declaredField.setAccessible(true);
        return declaredField;
    }


    public static Method getDeclaredMethod(Object obj, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method declaredMethod = obj.getClass().getDeclaredMethod(name, parameterTypes);
        declaredMethod.setAccessible(true);
        return declaredMethod;
    }


    /**
     * 清理本地的BluetoothGatt 的缓存，以保证在蓝牙连接设备的时候，设备的服务、特征是最新的
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        Method refreshtMethod = null;
        if (null != gatt) {
            try {
                for (Method methodSub : gatt.getClass().getDeclaredMethods()) {
                    if ("connect".equalsIgnoreCase(methodSub.getName())) {
                        Class<?>[] types = methodSub.getParameterTypes();
                        if (types.length > 0) {
                            if ("int".equalsIgnoreCase(types[0].getName())) {
                                refreshtMethod = methodSub;
                            }
                        }
                    }
                }
                if (refreshtMethod != null) {
                    refreshtMethod.invoke(gatt);
                }
                LogUtils.eAiDex("refreshDeviceCache-->" + "清理本地的BluetoothGatt 的缓存成功");
                return true;
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }
        LogUtils.eAiDex("refreshDeviceCache-->" + "清理本地清理本地的BluetoothGatt缓存失败");
        return false;
    }

}



