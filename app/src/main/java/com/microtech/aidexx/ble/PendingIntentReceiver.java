package com.microtech.aidexx.ble;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.jeremyliao.liveeventbus.LiveEventBus;
import java.util.List;

public class PendingIntentReceiver extends BroadcastReceiver {
    public static final String ACTION = "no.nordicsemi.android.support.v18.ACTION_FOUND";
    public static final int REQUEST_CODE = 2004;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        final int errorCode = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, 0);
        final List<ScanResult> nativeScanResults =
                intent.getParcelableArrayListExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);
        if (nativeScanResults != null && nativeScanResults.size() > 0) {
            ScanResult scanResult = nativeScanResults.get(0);
        }
    }
}
