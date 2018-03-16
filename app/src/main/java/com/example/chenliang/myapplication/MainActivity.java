package com.example.chenliang.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int PERMISSION_OF_OVERLAY=2333;
    public static boolean deleteScreenshotFile=false;
    ImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=(Button)findViewById(R.id.button);
       // Log.d(TAG, "onCreate: "+getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox=(CheckBox)findViewById(R.id.checkBox);
                deleteScreenshotFile=checkBox.isChecked();
                getPermission();
            }
        });

}
public void getPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(!Settings.canDrawOverlays(MainActivity.this)){
                Toast.makeText(MainActivity.this,"需要悬浮窗权限才能使用",Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())),PERMISSION_OF_OVERLAY);
            }
            else{
                startService();
            }
        }
        else {
            //Toast.makeText(MainActivity.this,"系统版本过低，无法使用",Toast.LENGTH_SHORT).show();
            startService();
        }
}
public void startService(){
    Intent intent=new Intent(MainActivity.this,MyService.class);
    startService(intent);
    finish();
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PERMISSION_OF_OVERLAY){
            Log.d(TAG, "onActivityResult: "+resultCode);
            if(!Settings.canDrawOverlays(MainActivity.this)){
                Toast.makeText(MainActivity.this,"授权失败",Toast.LENGTH_SHORT).show();
            }
            else {
                startService();
            }
        }
    }
}
