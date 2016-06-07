package jp.techacademy.takashiwakamatsu.autoslideshowapp;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener   {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private int nowpage =0; //表示対象ページ
    private Timer mainTimer;					//タイマー用
    private MainTimerTask mainTimerTask;		//タイマータスククラス
    private Handler mHandler = new Handler();   //UI Threadへのpost用ハンドラ
    private Button buttonGo ;
    private Button buttonBack ;
    private Button buttonAuto ;
    private String autoLabel ; //自動再生ボタンのラベル

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo(nowpage);
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo(nowpage);
        }

        //ボタンインスタンスの取得と、リスナ定義
        buttonGo = (Button) findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(this);

        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        buttonAuto = (Button) findViewById(R.id.buttonAuto);
        buttonAuto.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(nowpage);
                }
                break;
            default:
                break;
        }
    }


    public void onClick(View v) {

        if (v.getId() == R.id.buttonGo) {
            //進むボタンの処理
            nowpage = nowpage + 1 ;
            getContentsInfo(nowpage);
        } else if (v.getId() == R.id.buttonBack) {
            //戻るボタンの処理
            nowpage = nowpage - 1 ;
            getContentsInfo(nowpage);
        } else if (v.getId() == R.id.buttonAuto) {
            //自動生成ボタンの処理
            nowpage = 0 ;

            //Timerは一度Cancelすると再利用できないようなので、ボタン押下毎に生成
            this.mainTimer = new Timer();
            this.mainTimerTask = new MainTimerTask();

            //現在の自動再生ボタンのラベルで処理を分岐
            autoLabel = buttonAuto.getText().toString();
            if ( autoLabel.equals("再生")){
                buttonAuto.setText("停止");
                buttonGo.setEnabled(false);
                buttonBack.setEnabled(false);
                this.mainTimer.schedule(mainTimerTask, 1000,2000);  //タイマースタート
            }else {
                buttonAuto.setText("再生");
                buttonGo.setEnabled(true);
                buttonBack.setEnabled(true);
                this.mainTimer.cancel();    //タイマーエンド
                this.mainTimer = null;
            }
        }
    }


    private void getContentsInfo(int page)  {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        int maxCount = cursor.getCount(); //カーソル内の画像数
        if ( page <= -1 ) {
            nowpage = maxCount -1 ; //0より前なら最後に移動する
        } else if  (page > maxCount-1 ) {
            nowpage = 0; //最大値の後なら最初に戻る
        }
        cursor.moveToPosition(nowpage);
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);

        /*
        Log.d("Ever",String.valueOf(nowpage));
        Log.d("Ever",String.valueOf(imageUri));
        */
        cursor.close();
    }


    public class MainTimerTask extends TimerTask {
        @Override
        public void run() {
            //ここに定周期で実行したい処理
            mHandler.post( new Runnable() {
                public void run() {
                    getContentsInfo(nowpage)  ;
                    nowpage = nowpage +  1;                      //実行間隔分を加算処理
                }
            });
        }
    }

}
