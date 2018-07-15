package com.example.floatwindow;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ToggleButton openFloatWindow;

    private ToggleButton openShake;

    private MediaProjectionManager mMediaProjectionManager;
    private int REQUEST_MEDIA_PROJECTION = 1;
    private int result = 0;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(MEDIA_PROJECTION_SERVICE);
        startIntent();

        openFloatWindow = (ToggleButton) findViewById(R.id.openFloatWindow);
        openFloatWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((SuperScreenShot)getApplication()).setOpenFloatWindow(true);
                    Intent openFloatWindow = new Intent(MainActivity.this,FloatWindowService.class);
                    startService(openFloatWindow);
                } else {
                    ((SuperScreenShot)getApplication()).setOpenFloatWindow(false);
                    Intent closeFloatWindow = new Intent(MainActivity.this,FloatWindowService.class);
                    stopService(closeFloatWindow);
                }
            }
        });

        openShake = (ToggleButton) findViewById(R.id.openShake);
        openShake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((SuperScreenShot)getApplication()).setOpenShake(true);
                    Intent intent = new Intent(MainActivity.this,ShakeService.class);
                    startService(intent);
                } else {
                    ((SuperScreenShot)getApplication()).setOpenShake(false);
                    Intent closeShake = new Intent(MainActivity.this,ShakeService.class);
                    stopService(closeShake);
                }
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startIntent() {
        if (intent != null && result != 0) {
            //用户同意截屏权限，设置请求返回参数
            ((SuperScreenShot) getApplication()).setResult(result);
            ((SuperScreenShot) getApplication()).setIntent(intent);

        } else {
            //请求开启截屏权限
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((SuperScreenShot) getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }
    //onActivityResult返回请求开启截屏权限结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            } else if (data != null && resultCode != 0) {
                result = resultCode;
                intent = data;
                ((SuperScreenShot) getApplication()).setResult(resultCode);
                ((SuperScreenShot) getApplication()).setIntent(data);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( ((SuperScreenShot)getApplication()).isOpenFloatWindow()){
            openFloatWindow.setChecked(true);
        }else {
            openFloatWindow.setChecked(false);
        }
        if(((SuperScreenShot)getApplication()).isOpenShake()){
            openShake.setChecked(true);
        }else{
            openShake.setChecked(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.instructions:
                Intent instructions = new Intent(MainActivity.this,InstructionsActivity.class);
                startActivity(instructions);
                break;
            case R.id.about_us:
                Intent aboutUs = new Intent(MainActivity.this,AboutUsActivity.class);
                startActivity(aboutUs);
                break;
            default:
        }
        return true;
    }
}
