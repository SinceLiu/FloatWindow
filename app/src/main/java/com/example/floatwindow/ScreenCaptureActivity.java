package com.example.floatwindow;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenCaptureActivity extends AppCompatActivity {

    private String TAG = "ScreenCaptureActivity";
    private int result = 0;
    private Intent intent = null;
    private MediaProjectionManager mMediaProjectionManager;//捕捉屏幕的管理器
    private MarkSizeView markSizeView;
    private Rect markedArea;
    private MarkSizeView.GraphicPath mGraphicPath;
    private TextView captureTips;
//    private Button captureAll;
    private Button markType;
    private boolean isMarkRect;
    private ScreenCapture screenCaptureService;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG,"11111111");
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(SuperScreenShot.getInstance(),"Screenshots 5.0以下系统暂不支持截屏", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LogUtil.d(TAG,"show Window");
        initWindow();

        setContentView(R.layout.activity_screen_capture);

        markSizeView = (MarkSizeView) findViewById(R.id.mark_size);
        captureTips = (TextView) findViewById(R.id.capture_tips);
//        captureAll = (Button) findViewById(R.id.capture_all);
        markType = (Button) findViewById(R.id.mark_type);

        Intent intent = getIntent();
        isMarkRect = intent.getBooleanExtra("isMarkRect",true);
        markSizeView.setIsMarkRect(isMarkRect);

        if (isMarkRect) {
            markType.setText("矩形选取");
        } else {
            markType.setText("曲线选取");
        }

//        markType.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {  //更换截屏类型
//                LogUtil.d(TAG,"change leixing");
//                isMarkRect =! isMarkRect;
//                markSizeView.setIsMarkRect(isMarkRect);
//                markType.setText(isMarkRect?"矩形选取":"任意图形");
//            }
//        });

        markSizeView.setmOnClickListener(new MarkSizeView.onClickListener() {  //截屏区域的触摸事件监听
            @Override
            public void onConfirm(Rect markedArea) {
                ScreenCaptureActivity.this.markedArea = new Rect(markedArea);
                markSizeView.reset();
                markSizeView.setUnmarkedColor(ContextCompat.getColor(ScreenCaptureActivity.this,R.color.transparent));
                markSizeView.setEnabled(false);
                startScreenCapture();
            }

            @Override
            public void onConfirm(MarkSizeView.GraphicPath path) {
                mGraphicPath = path;
                markSizeView.reset();
                markSizeView.setUnmarkedColor(ContextCompat.getColor(ScreenCaptureActivity.this,R.color.transparent));
                markSizeView.setEnabled(false);
                startScreenCapture();
            }

            @Override
            public void onCancel() {
                captureTips.setVisibility(View.VISIBLE);
//                captureAll.setVisibility(View.VISIBLE);
                markType.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTouch() {
                captureTips.setVisibility(View.GONE);
//                captureAll.setVisibility(View.GONE);
                markType.setVisibility(View.GONE);
            }
        });

//        captureAll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {  //开始截取全屏
//                Log.d(TAG,"start capture All");
//                markSizeView.setUnmarkedColor(ContextCompat.getColor(ScreenCaptureActivity.this,R.color.transparent));
//                captureTips.setVisibility(View.GONE);
//                captureAll.setVisibility(View.GONE);
//                markType.setVisibility(View.GONE);
//                startScreenCapture();
//
//            }
//        });
    }

    private void initWindow() { //初始化界面背景颜色透明
        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ScreenCaptureActivity.this,R.color.transparent));
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void startIntent() {
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    LogUtil.d(TAG,"qingqiu shouquan");
//                    startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),REQUEST_MEDIA_PROJECTION);//弹窗询问用户是否授权捕捉屏幕
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        if (screenCaptureService != null)
            screenCaptureService.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { //返回键重新显示小悬浮球
        super.onBackPressed();
        MyWindowManager.createSmallWindow(getApplication());
    }

    private void startScreenCapture() {

        screenCaptureService = new ScreenCapture(this,markedArea,mGraphicPath);
        try {
            screenCaptureService.toCapture();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //获取授权结果
//        LogUtil.d(TAG,"get shouquan");
//        if (requestCode == REQUEST_MEDIA_PROJECTION) {
//            if (resultCode != Activity.RESULT_OK) {
//                LogUtil.d(TAG,"shibai");
//                return;
//            } else if (data != null && resultCode != 0) {
//                LogUtil.i(TAG,"user agree the application to capture screen");
//                result = resultCode;
//                intent = data;
//                startScreenCapture(data,resultCode);
//                LogUtil.i(TAG,"start service ScreenCaptureService");
//            }
//        }
//    }
}
