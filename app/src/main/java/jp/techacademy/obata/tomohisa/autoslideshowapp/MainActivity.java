package jp.techacademy.obata.tomohisa.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private Handler handler = new Handler();
    private Timer mytimer = new Timer();

    boolean startEndFlg = false;

    Button button1;
    Button button2;
    Button button3;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //戻る
        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(this);

        //再生/停止
        button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(this);
        mytimer.schedule(new MyTimer(), 0, 2000);

        //進む
        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(this);

        // Android 6.0以降の場合
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // パーミッションの許可状態を確認する
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();

        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
        if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageVIew = (ImageView) findViewById(R.id.imageView1);
            imageVIew.setImageURI(imageUri);
        }
        this.cursor = cursor;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button2){
            next();
        } else if (view.getId() == R.id.button1) {
            previous();
        } else {
            if(button1.isEnabled()){
                button1.setEnabled(false);
                button2.setEnabled(false);
                this.startEndFlg = true;
            }else{
                button1.setEnabled(true);
                button2.setEnabled(true);
                this.startEndFlg = false;
            }

        }
    }

    private void next(){
        if (this.cursor.isLast()){
            this.cursor.moveToFirst();
        } else {
            this.cursor.moveToNext();
        }
        int fieldIndex = this.cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = this.cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView1);
        imageVIew.setImageURI(imageUri);
    }

    private void previous(){
        if (this.cursor.isFirst()){
            this.cursor.moveToLast();
        } else {
            this.cursor.moveToPrevious();
        }
        int fieldIndex = this.cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = this.cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView1);
        imageVIew.setImageURI(imageUri);
    }

    private class MyThread extends Thread {

        public void run() {
            if(startEndFlg) {
                handler.post(new Runnable() {
                    public void run() {
                        next();
                    }
                });
            }
        }

    }

    private class MyTimer extends TimerTask {
        public void run() {
            MyThread mythread = new MyThread();
            mythread.start();
        }
    }
}
