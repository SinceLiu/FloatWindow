package com.example.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by 19160 on 2017/7/6.
 */

public class MyWindowManager {

    private static FloatWindowSmallView smallWindow;//小悬浮球view的实例

    private static FloatWindowBigView bigWindow;//大悬浮球view的实例

    private static WindowManager.LayoutParams smallWindowParams;//小悬浮球view的参数

    private static WindowManager.LayoutParams bigWindowParams;//大悬浮球view的参数

    private static WindowManager mWindowManager;//用于控制在屏幕上添加或者移除悬浮窗

    /*
    创建小悬浮球
     */
    public static void createSmallWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        int screenHeight = point.y;
        if (smallWindow == null) {
            smallWindow = new FloatWindowSmallView(context);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.START|Gravity.TOP;
                smallWindowParams.width = FloatWindowSmallView.viewWidth;
                smallWindowParams.height = FloatWindowSmallView.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight/2;
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow,smallWindowParams);
        }
    }

    /*
    移除小悬浮球
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /*
    创建大悬浮球
     */
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        int screenHeight = point.y;
        if (bigWindow == null) {
            bigWindow = new FloatWindowBigView(context);
            if (bigWindowParams == null) {
                bigWindowParams = new WindowManager.LayoutParams();
                bigWindowParams.x = 0;
                bigWindowParams.y = 0;
                bigWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.gravity = Gravity.START|Gravity.TOP;
                bigWindowParams.width = FloatWindowBigView.viewWidth;
                bigWindowParams.height = FloatWindowBigView.viewHeight;
            }
            windowManager.addView(bigWindow,bigWindowParams);
        }
    }

    /*
    移除大悬浮球
     */
    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }

    /*
    如果没有创建WindowManager，则创建一个新的WindowManager返回，否则返回当前已创建的WindowManager
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }
}
