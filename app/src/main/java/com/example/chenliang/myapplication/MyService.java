package com.example.chenliang.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MyService extends Service {
    public static final int FILE_CHANGED=111;
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    public static final long startServiceTime= new Date().getTime();
    View view1;
    Message message;
    Handler handler;
    private static final String TAG = "MyService";
    //public static int a=0;
   // public static boolean changed=false;
    public static File[] firstDetected;
    public static String DCIM_path=Environment.getExternalStorageDirectory()+"/DCIM/Screenshots";
    public static String Picture_path=Environment.getExternalStorageDirectory()+"/Pictures/Screenshots";
    public static String tureScreenshotPath;
    @Override
    public void onCreate()  {
        super.onCreate();
        if(new File(Picture_path).listFiles()==null){
            tureScreenshotPath=DCIM_path;
        }
        else{
            tureScreenshotPath=Picture_path;
        }
        firstDetected=new File(tureScreenshotPath).listFiles();
        Log.d(TAG, "onCreate: "+tureScreenshotPath);
        Log.d(TAG, "onCreate: "+new File(tureScreenshotPath).listFiles());
       //File file2;
        File[] files=new File(tureScreenshotPath).listFiles();
        for(File file2:files){
            Log.d(TAG, "onCreate: "+file2.getName());
        }
        //Log.d(TAG, "onCreate: "+new File(tureScreenshotPath).listFiles().length);
        File file=new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath()+"/tessdata/eng.traineddata");

        if(!file.exists()){
           try{
               handleDataFile();
           }catch (IOException e){
               e.printStackTrace();
           }

        }
        createWindow();
        DetectOnBackground detectOnBackground=new DetectOnBackground();
        detectOnBackground.start();
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(message.what){
                    case FILE_CHANGED:
                        popUp();break;
                    default:break;
                }
            }
        };
    }
    class DetectOnBackground extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                while (!interrupted()){
                    Thread.sleep(1000);
                    if(detectScreenshotFileChanged()){
                        message=new Message();
                        message.what=FILE_CHANGED;
                        handler.sendMessage(message);
                    }
                }
            }catch (InterruptedException e){e.printStackTrace();}
        }
    }
    public void createWindow(){
        windowManager=(WindowManager)getApplicationContext().getSystemService(getApplicationContext().WINDOW_SERVICE);
        layoutParams=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,300,300,WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);
        layoutParams.gravity= Gravity.LEFT|Gravity.TOP;
        view1= LayoutInflater.from(getApplicationContext()).inflate(R.layout.float_layout,null);
        windowManager.addView(view1,layoutParams);
        final float statusBarHeight=getStatusBarHeight();
        view1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                layoutParams.x=(int)(motionEvent.getRawX()-view1.getWidth()/2);
                layoutParams.y=(int)(motionEvent.getRawY()-view1.getHeight()/2-statusBarHeight);
                windowManager.updateViewLayout(view1,layoutParams);

                return false;
            }
        });
       /* view1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final View view2=LayoutInflater.from(getApplicationContext()).inflate(R.layout.second_page,null);
                view1.setVisibility(View.GONE);
                windowManager.addView(view2,layoutParams);
                Bitmap bitmap= BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/Pictures/Screenshots/"+getScreenShotFileName());
                List<Integer> jingjiData=get_jingji_data(bitmap);
                List<Integer> our=jingjiData.subList(0,5);
                List<Integer> enemy=jingjiData.subList(5,10);
                showList(our);showList(enemy);
                int ourSum=addListElement(our);
                int enemySum=addListElement(enemy);
                Log.d(TAG, "onCreate: "+ourSum);
                Log.d(TAG, "onCreate: sum"+enemySum);
                TextView textView=(TextView)view2.findViewById(R.id.textView);
                Button close=(Button)view2.findViewById(R.id.close);
                if(ourSum-enemySum>=0){
                    textView.setText("我方领先"+(ourSum-enemySum)+"经济");
                }
                if(ourSum-enemySum<0){
                    textView.setText("我方落后"+(enemySum-ourSum)+"经济");
                }
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        windowManager.removeView(view2);
                        view1.setVisibility(View.VISIBLE);
                    }
                });
                return false;
            }
        });*/
        }


    public List<Integer> get_jingji_data(Bitmap bitmap){
        TessBaseAPI tessBaseAPI=new TessBaseAPI();
        tessBaseAPI.init(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(),"eng");
        tessBaseAPI.setDebug(true);
        List<Integer> jingjiData=new ArrayList<>();
        for(int i=0;i<5;i++){
            Bitmap jingji=Bitmap.createBitmap(bitmap,760,294+(i*133),110,36);
            //imageView.setImageBitmap(jingji);
            tessBaseAPI.setImage(jingji);
            jingjiData.add(parseTessResult(tessBaseAPI.getUTF8Text()));
        }
        for(int j=0;j<5;j++){
            Bitmap jingji=Bitmap.createBitmap(bitmap,1700,294+(j*133),110,36);
            //imageView.setImageBitmap(jingji);
            tessBaseAPI.setImage(jingji);
            jingjiData.add(parseTessResult(tessBaseAPI.getUTF8Text()));
        }
        tessBaseAPI.end();
        return jingjiData;
    }
    public void showList(List<Integer> a){
        for(int i:a){
            Log.d(TAG, "showList: "+i);
        }
    }
    public int addListElement(List<Integer> list){
        int sum=0;
        for(int i:list){
            sum+=i;
        }
        return sum;
    }
    public int parseTessResult(String result){
        String a=result.replaceAll("\\s","");
        return Integer.parseInt(a);
    }
    public String getScreenShotFileName(){
        File file=new File(tureScreenshotPath);
        List<Long> time_long=new ArrayList<>();
        List<String> fileName=new ArrayList<>();
        for(File file1:file.listFiles()){
           // Log.d(TAG, "onCreate: "+file1.lastModified()+file1.getName());
            time_long.add(file1.lastModified());
            fileName.add(file1.getName());
        }
       // Log.d(TAG, "onCreate: "+ Collections.max(time_long));
        //Log.d(TAG, "onCreate: "+fileName.get(time_long.indexOf(Collections.max(time_long))));
        return fileName.get(time_long.indexOf(Collections.max(time_long)));
    }
    public float getStatusBarHeight(){
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    public void handleDataFile()throws FileNotFoundException,IOException{
        InputStream inputStream=getResources().openRawResource(R.raw.eng);
        File filedir=getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file1=new File(filedir.getPath()+"/tessdata");
        file1.mkdirs();
        byte[] bytes=new byte[1024];

        FileOutputStream outputStream=new FileOutputStream(filedir.getPath()+"/tessdata/eng.traineddata");
        while (inputStream.read(bytes)!=-1){
        outputStream.write(bytes);
        }
        inputStream.close();
        outputStream.close();
        Toast.makeText(getApplicationContext(),"初始化完成",Toast.LENGTH_SHORT).show();
    }
    public boolean detectScreenshotFileChanged(){
        File screenshot=new File(tureScreenshotPath);
        File[] detected=screenshot.listFiles();
        Log.d(TAG, "detectScreenshotFileChanged: "+detected.length);
        //Log.d(TAG, "detectScreenshotFileChanged: ");
        if (detected.length>firstDetected.length){
            firstDetected=new File(tureScreenshotPath).listFiles();
            return true;
        }
        return false;
    }
    public void popUp(){
        final View view2=LayoutInflater.from(getApplicationContext()).inflate(R.layout.second_page,null);
        view1.setVisibility(View.GONE);
        windowManager.addView(view2,layoutParams);
        Bitmap bitmap= BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/Pictures/Screenshots/"+getScreenShotFileName());
        List<Integer> jingjiData=get_jingji_data(bitmap);
        List<Integer> our=jingjiData.subList(0,5);
        List<Integer> enemy=jingjiData.subList(5,10);
        showList(our);showList(enemy);
        int ourSum=addListElement(our);
        int enemySum=addListElement(enemy);
        Log.d(TAG, "onCreate: "+ourSum);
        Log.d(TAG, "onCreate: sum"+enemySum);
        TextView textView=(TextView)view2.findViewById(R.id.textView);
        Button close=(Button)view2.findViewById(R.id.close);
        if(ourSum-enemySum>=0){
            textView.setText("左边领先右边"+(ourSum-enemySum)+"经济");
        }
        if(ourSum-enemySum<0){
            textView.setText("左边落后右边"+(enemySum-ourSum)+"经济");
        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(view2);
                view1.setVisibility(View.VISIBLE);
            }
        });
    }
    public void deleteScreenshotFiles(){
        long end=new Date().getTime();
        for(File file:firstDetected){
            if(file.lastModified()<end&&file.lastModified()>startServiceTime){
                file.delete();
            }
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: "+"服务关闭执行函数");
        Log.d(TAG, "onDestroy: "+MainActivity.deleteScreenshotFile);
        if(MainActivity.deleteScreenshotFile==true) {
            deleteScreenshotFiles();
        }
    }

    @Override
    public boolean stopService(Intent name) {

        Log.d(TAG, "onDestroy: "+"服务关闭执行函数");
        Log.d(TAG, "onDestroy: "+MainActivity.deleteScreenshotFile);
        if(MainActivity.deleteScreenshotFile==true) {
            deleteScreenshotFiles();
        }
        return super.stopService(name);
    }
}
