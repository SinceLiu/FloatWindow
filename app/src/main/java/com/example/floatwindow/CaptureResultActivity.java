package com.example.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureResultActivity extends AppCompatActivity {

    private ImageView captureImage;
    private Bitmap bitmap;

    private TextView save;

    private void initWindow() {
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(localDisplayMetrics);
        localLayoutParams.width = ((int) (localDisplayMetrics.widthPixels * 0.99D));
        localLayoutParams.gravity = 17;
        localLayoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(localLayoutParams);
        getWindow().setGravity(17);
        getWindow().getAttributes().windowAnimations = R.anim.anim_scale_in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    private void init() {
        CardView cardView = new CardView(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_capture_result,null,false);
        cardView.setRadius(ViewUtil.dp2px(10));
        cardView.addView(view);

        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this,R.color.transparent));
        setContentView(cardView);
        initWindow();

        Intent intent = getIntent();
        String fileName = intent.getStringExtra(ScreenCapture.FILE_NAME);
        if (fileName == null) {
            Toast.makeText(this,"截屏失败",Toast.LENGTH_SHORT).show();
            LogUtil.d("1","截屏失败");
            finish();
            return;
        }
        File capturedFile = new File(fileName);
        if (capturedFile.exists()) {
            bitmap = BitmapFactory.decodeFile(fileName);
        } else {
            Toast.makeText(this,"截屏失败",Toast.LENGTH_SHORT).show();
            LogUtil.d("2","截屏失败");
            finish();
            return;
        }
        captureImage = (ImageView) findViewById(R.id.capture_pic);
        save = (TextView) findViewById(R.id.save);

        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(localDisplayMetrics);
        if (bitmap.getHeight() > localDisplayMetrics.heightPixels * 2 /3
                || 1.0 * bitmap.getHeight() / bitmap.getWidth() >= 1.2) {
            LinearLayout container = (LinearLayout) findViewById(R.id.container);
            container.setOrientation(LinearLayout.HORIZONTAL);

            captureImage.setMaxWidth(localDisplayMetrics.widthPixels / 2);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) captureImage.getLayoutParams();
            if (bitmap.getWidth() > localDisplayMetrics.widthPixels / 2) {
                layoutParams.width = bitmap.getWidth() *2 / 5;
                layoutParams.height = bitmap.getHeight() *2 / 5;
            } else {
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            }
            captureImage.setLayoutParams(layoutParams);
        }

        captureImage.setImageBitmap(bitmap);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/Pictures/",format.format(new Date()) + ".jpg");
                    file.getParentFile().mkdirs();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(file));
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    sendBroadcast(intent);
                    Toast.makeText(CaptureResultActivity.this,"已保存sd卡 Pictures 目录下",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(CaptureResultActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                }
                finish();
                MyWindowManager.createSmallWindow(getApplication());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        MyWindowManager.createSmallWindow(getApplication());
    }
}
