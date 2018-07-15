package com.example.floatwindow;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * Created by 19160 on 2017/7/6.
 */

public class FloatWindowSmallView extends LinearLayout {

    public static int viewWidth;//小悬浮球的宽度

    public static int viewHeight;//小悬浮球的高度

    private static int statusBarHeight;//记录系统状态栏高度

    private WindowManager windowManager;//用于更新小悬浮球的位置

    private WindowManager.LayoutParams mParams;//小悬浮球的参数

    private float xEnd;//记录现在手指在屏幕中的横坐标值

    private float yEnd;//记录现在手指在屏幕中的纵坐标值

    private float xStart;//记录手指开始点击屏幕的横坐标值

    private float yStart;//记录手指开始点击屏幕的纵坐标值

    private float xView;//记录手指点击时相对于小悬浮球的view的横坐标值

    private float yView;//记录手指点击时相对于小悬浮球的view的纵坐标值

    public FloatWindowSmallView(Context context) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_small,this);
        View view = findViewById(R.id.small_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        ImageButton openBigWindow = (ImageButton) findViewById(R.id.openBigWindow);
        openBigWindow.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        xView = event.getX();
                        yView = event.getY();
                        xStart = event.getRawX();
                        yStart = event.getRawY() - getStatusBarHeight();
                        xEnd = event.getRawX();
                        yEnd = event.getRawY() - getStatusBarHeight();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xEnd = event.getRawX();
                        yEnd = event.getRawY() - getStatusBarHeight();
                        updateViewPosition();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (xStart == xEnd && yStart == yEnd) {
                            LogUtil.d("smallWindow","open bigWindow");
                            openBigWindow();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    private void updateViewPosition() {
        mParams.x = (int) (xEnd - xView);
        mParams.y = (int) (yEnd - yView);
        windowManager.updateViewLayout(this,mParams);
    }

    /*
    打卡大悬浮球
     */
    private void openBigWindow() {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    /*
    获取状态栏高度
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?>c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}
