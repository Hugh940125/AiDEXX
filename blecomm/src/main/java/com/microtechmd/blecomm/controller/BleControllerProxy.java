package com.microtechmd.blecomm.controller;

import com.microtechmd.blecomm.entity.NewSensorEntity;

public abstract class BleControllerProxy extends BleController {
   public abstract void getTransInfo();
   public abstract void getDefaultParam();
   public abstract void newSensor(NewSensorEntity newSensorEntity);
   public abstract void startTime();
}
