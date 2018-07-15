package com.example.floatwindow;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import static android.content.ContentValues.TAG;

public class  AllShotService extends Service {
    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String imagePath = null;
    private String imageName = null;

    Handler handler = new Handler(Looper.getMainLooper());

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;


//    public AllShotService() {
//        super("AllShotService");
//    }

    @Override
        public void onCreate() {
            super.onCreate();
        LogUtil.d("AllShotService","onCreate");
    }

//    //每次点击（截取）全屏重启Service截图
//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//
//
//        LogUtil.d("IntentService","over");
//    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                createVirtualEnvironment();
                toCapture();
                stopSelf();
            }
        }).start();
        return super.onStartCommand(intent,flags,startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopVirtual();
        tearDownMediaProjection();
        LogUtil.d("AllShotService","onDestroy");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void toCapture() {
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG,"before startVirtual");
                    startVirtual();
                    LogUtil.d(TAG,"after startVirtual");
                }
            },10);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //capture the screen
                    try {
                        LogUtil.d(TAG,"before startCapture");
                        startCapture();
                        LogUtil.d(TAG,"After startCapture");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendBroadcastCaptureFail();
                    }
                }
            },100);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    //Prepare the virtual environment
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createVirtualEnvironment(){
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        imagePath= Environment.getExternalStorageDirectory().getPath()+"/Pictures/";  //图片保存路径为内存卡根目录Pictures文件夹
        imageName=imagePath+strDate+".png";
        mMediaProjectionManager1 = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startVirtual() {
        if (mMediaProjection != null) {
            Log.i(TAG, "want to display virtual");
            virtualDisplay();
        } else {
            Log.i(TAG, "start screen capture intent");
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpMediaProjection(){
        mResultData = ((SuperScreenShot)getApplication()).getIntent();
        mResultCode = ((SuperScreenShot)getApplication()).getResult();
        mMediaProjectionManager1 = ((SuperScreenShot)getApplication()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {
        try {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            Log.i(TAG, "virtual displayed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture() throws Exception{
        strDate = dateFormat.format(new java.util.Date());
        imageName = imagePath+strDate+".png";
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    toCapture();
                }
            });
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        saveCutBitmap(bitmap);//保存图片

        bitmap.recycle();//自由选择是否回收




//        if (bitmap != null) {
//            try {
//                File fileImage = new File(imageName);
//                if (!fileImage.exists()) {
//                    fileImage.createNewFile();
//                }
//                FileOutputStream out = new FileOutputStream(fileImage);
//                if (out != null) {
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                    out.flush();
//                    out.close();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


    }

    private void saveCutBitmap(Bitmap cutBitmap) {  //把裁剪获得的图片保存的bitmap中
        File localFile = new File(SuperScreenShot.getInstance().getFilesDir(),"temp.png");
        String fileName = localFile.getAbsolutePath();
        try {
            if (!localFile.exists()) {
                localFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(localFile);
            if (fileOutputStream != null) {
                cutBitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (IOException e) {
            sendBroadcastCaptureFail();
            return;
        }
        Intent newIntent = new Intent(this,CaptureResultActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.putExtra(ScreenCapture.Message,"保存成功");
        newIntent.putExtra(ScreenCapture.FILE_NAME,fileName);
        startActivity(newIntent);
        onDestroy();
    }

    private void sendBroadcastCaptureFail() {
        Toast.makeText(SuperScreenShot.getInstance(),"截屏失败",Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG,"virtual display stopped");
    }

}