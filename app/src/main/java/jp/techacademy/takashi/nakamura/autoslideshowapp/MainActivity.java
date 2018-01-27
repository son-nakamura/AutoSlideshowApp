package jp.techacademy.takashi.nakamura.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button mAutoButton;
    ImageView mImageView;

    private static final int PERMISSION_REQUEST_CODE = 100;
    boolean mPermitted = false;
    Cursor mCursor;
    Timer mTimer = null;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        mAutoButton = (Button) findViewById(R.id.autoButton);
        mImageView = (ImageView) findViewById(R.id.imageView);

        // 外部ストレージ読み込みのパーミッションを確認する
        // Android6.0以降の場合、外部ストレージの読み込みが許可されていないとき
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            // 許可ダイアログを表示する
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

        // Android5.x系以下の場合、
        } else {
            // パーミッションは許可されている
            mPermitted = true;
            // 画像の情報を取得
            ContentResolver resolver = getContentResolver();
            mCursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            // 最初の画像を表示する
            if (mCursor.moveToFirst()) {
                showPicture();
            }
        }

        // 進むボタンが押されたときの処理
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPermitted) {
                    if (mTimer == null) {
                        // 次の画像を表示する
                        if (mCursor.moveToNext()) {
                            showPicture();
                        } else {
                            // 次の画像がない場合、最初の画像を表示する
                            if (mCursor.moveToFirst()) {
                                showPicture();
                            }
                        }
                    }
                }
            }
        });

        // 戻るボタンが押されたときの処理
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPermitted) {
                    if (mTimer == null) {
                        // 前の画像を表示する
                        if (mCursor.moveToPrevious()) {
                            showPicture();
                        } else {
                            // 前の画像がない場合、最後の画像を表示する
                            if (mCursor.moveToLast()) {
                                showPicture();
                            }
                        }
                    }
                }
            }
        });

        // 再生/停止ボタンが押されたときの処理
        mAutoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPermitted) {
                    if (mTimer == null) {
                        // 再生開始の処理
                        mAutoButton.setText("停止");
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 次の画面を表示する
                                        if (mCursor.moveToNext()) {
                                            showPicture();
                                        } else {
                                            // 次の画像がない場合、最初の画像を表示する
                                            if (mCursor.moveToFirst()) {
                                                showPicture();
                                            }
                                        }
                                    }
                                });
                            }
                        }, 0, 2000);
                    } else {
                        // 停止する処理
                        mAutoButton.setText("再生");
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        });
    }   // onCreate()の終わり


    // アプリの終了時にmCursorをクローズする
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
    }


    // パーミッション許可の結果を受け取るメソッド
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission,
                                           int[] grantResult) {
        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが許可された場合
                    mPermitted = true;
                    // 画像の情報を取得
                    ContentResolver resolver = getContentResolver();
                    mCursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null, null, null, null);
                    // 最初の画像を表示する
                    if (mCursor.moveToFirst()) {
                        showPicture();
                    }
        }
    }


    // 画像を表示するメソッド
    private void showPicture() {
        Long id = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id);
        mImageView.setImageURI(imageUri);
    }

}
