package com.example.floatwindow;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Created by 19160 on 2017/7/11.
 */

public class LogUtil {

    public static final String _TAG = "SuperScreenShot";
    public static boolean isEshow=true;
    public static boolean isWshow=true;
    public static boolean isIshow=true;
    public static boolean isDshow=true;
    public static boolean isVshow=true;


    static{
        if (isApkDebugable(SuperScreenShot.getInstance())){
            isEshow=true;
            isWshow=true;
            isIshow=true;
            isDshow=true;
            isVshow=true;
        }else {
            isEshow=true;
            isWshow=false;
            isIshow=false;
            isDshow=false;
            isVshow=false;
        }
    }

    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info= context.getApplicationInfo();
            return (info.flags& ApplicationInfo.FLAG_DEBUGGABLE)== ApplicationInfo.FLAG_DEBUGGABLE;
        } catch (Exception e) {

        }
        return false;
    }

    public static void d(String msg){
        if(isDshow)
            Log.d(_TAG, msg);
    }
    public static void d(String tag, String msg){
        d(tag+","+msg);
    }
    public static void e(String msg){
        if(isEshow) Log.e(_TAG, msg);
    }
    public static void e(String tag, String msg){
        e(tag+","+msg);
    }
    public static void i(String msg){
        if(isIshow) Log.i(_TAG, msg);
    }
    public static void i(String tag, String msg){
        i(tag+","+msg);
    }
    public static void w(String msg){
        if(isWshow) Log.w(_TAG, msg);
    }
    public static void w(String tag, String msg){
        w(tag+","+msg);
    }
    public static void v(String msg){
        if(isVshow) Log.v(_TAG, msg);
    }
    public static void v(String tag, String msg){
        v(tag+","+msg);
    }
}
