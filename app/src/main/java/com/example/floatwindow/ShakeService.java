package com.example.floatwindow;


import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;


public class ShakeService extends Service  {
    SensorManager sensorManager = null;
    @Override
    public void onCreate(){
        super.onCreate();
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        if(sensorManager != null){  //注册监听器
            sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(sensorManager != null){  //取消监听器
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override  //方法1 onSensorChanged 当数据变化的时候被触发调用
        public void onSensorChanged(SensorEvent event) {
            int sensorType = event.sensor.getType();
            float[] values = event.values;  //values[0]:X轴，values[1]：Y轴，values[2]：Z轴
            if(sensorType == Sensor.TYPE_ACCELEROMETER){
                if((Math.abs(values[0])>18||Math.abs(values[1])>18||Math.abs(values[2])>18)){
                    Intent intent = new Intent(ShakeService.this,AllShotActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    MyWindowManager.removeSmallWindow(getApplicationContext());
                }
            }
        }

        @Override //方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
