package com.microtechmd.blecomm.controller;

import com.microtechmd.blecomm.entity.NewSensorEntity;

public abstract class BleControllerProxy extends BleController {
   public abstract void getTransInfo();
   public abstract void getDefaultParam();
   public abstract void newSensor(NewSensorEntity newSensorEntity);
   public abstract void startTime();
   public abstract void clearPair();
   public abstract int setDynamicMode(int mode);
   public abstract int setAutoUpdate();
}
