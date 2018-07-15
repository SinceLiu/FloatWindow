package com.example.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by 19160 on 2017/7/6.
 */

public class FloatWindowBigView extends LinearLayout {

    private boolean isMarkRect;

    public static int viewWidth;//记录大悬浮球的宽度

    public static int viewHeight;//记录大悬浮球的高度

    public FloatWindowBigView(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_window_big,this);
        View view = findViewById(R.id.big_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;

        Button allShot = (Button) findViewById(R.id.allShot);
        allShot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("bigWindow","dianji qunaping");
                //TODO 添加截全屏逻辑
                MyWindowManager.removeBigWindow(context);   //关闭大悬浮窗
                //截全屏
                Intent intent = new Intent(context,AllShotActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        Button recShot = (Button) findViewById(R.id.recShot);
        recShot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 添加截区域屏逻辑
                isMarkRect = true;
                Intent intent = new Intent(context,ScreenCaptureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("isMarkRect",isMarkRect);
                context.startActivity(intent);
                MyWindowManager.removeBigWindow(context);
            }
        });

        Button fingerShot = (Button) findViewById(R.id.fingerShot);
        fingerShot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isMarkRect = false;
                Intent intent = new Intent(context,ScreenCaptureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("isMarkRect",isMarkRect);
                context.startActivity(intent);
                MyWindowManager.removeBigWindow(context);
            }
        });

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });

        Button close = (Button) findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                Intent close = new Intent(getContext(),FloatWindowService.class);
                context.stopService(close);
                ((SuperScreenShot)getContext()).setOpenFloatWindow(false);
            }
        });
    }
}
