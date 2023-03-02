package com.microtech.aidexx.ble.device.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.microtech.aidexx.utils.LogUtil;
import com.microtechmd.blecomm.controller.BleController;

public class StartScanWorker extends Worker {

    public StartScanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LogUtil.eAiDEX("WorkManager start scan");
        BleController.startScan();
        return Result.success();
    }
}
