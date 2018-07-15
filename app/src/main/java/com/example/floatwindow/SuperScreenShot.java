package com.example.floatwindow;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.provider.Settings;

/**
 * Created by 19160 on 2017/7/8.
 */

public class SuperScreenShot extends Application {

    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;
    private boolean isOpenFloatWindow ;
    private boolean isOpenShake;

    public boolean isOpenFloatWindow() {
        return isOpenFloatWindow;
    }

    public void setOpenFloatWindow(boolean openFloatWindow) {
        isOpenFloatWindow = openFloatWindow;
    }

    public boolean isOpenShake() {
        return isOpenShake;
    }

    public void setOpenShake(boolean openShake) {
        isOpenShake = openShake;
    }

    private static SuperScreenShot instance;

    public static SuperScreenShot getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public int getResult(){
        return result;
    }

    public Intent getIntent(){
        return intent;
    }

    public MediaProjectionManager getMediaProjectionManager(){
        return mMediaProjectionManager;
    }

    public void setResult(int result1){
        this.result = result1;
    }

    public void setIntent(Intent intent1){
        this.intent = intent1;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager){
        this.mMediaProjectionManager = mMediaProjectionManager;
    }
}
