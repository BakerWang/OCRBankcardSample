package com.example.sinovoice.ocrbankcardsample;

import android.content.pm.ActivityInfo;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sinovoice.util.ConfigUtil;
import com.example.sinovoice.util.HciCloudOcrHelper;
import com.example.sinovoice.util.HciCloudSysHelper;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView tvResult;
    private HciCloudSysHelper mHciCloudSysHelper;
    private HciCloudOcrHelper mHciCloudOcrHelper;
    private Button btnPlay;
    private FrameLayout cameraPreviewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置窗体全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_result);
        tvResult = (TextView) findViewById(R.id.tv_result);

        initSinovoice();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        btnPlay = (Button) findViewById(R.id.btn_take_picture);
        cameraPreviewLayout = (FrameLayout) findViewById(R.id.layout_camera_preview);
        cameraPreviewLayout.addView(mHciCloudOcrHelper.previewCapture());
        mHciCloudOcrHelper.startCapture(ConfigUtil.CAP_KEY_OCR_LOCAL_BANKCARD);

        btnPlay.setOnClickListener(this);
    }

    private void initSinovoice() {
        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        mHciCloudOcrHelper = HciCloudOcrHelper.getInstance();
        int errorCode = mHciCloudSysHelper.init(this);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "系统初始化失败，错误码=" + errorCode);
            Toast.makeText(this, "系统初始化失败，错误码=" + errorCode, Toast.LENGTH_SHORT).show();
            return;
        }
        errorCode = mHciCloudOcrHelper.init(this, ConfigUtil.CAP_KEY_OCR_LOCAL_BANKCARD);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "拍照器初始化失败，错误码=" + errorCode);
            Toast.makeText(this, "拍照器初始化失败，错误码=" + errorCode, Toast.LENGTH_SHORT).show();
            return;
        }
        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_picture:
                mHciCloudOcrHelper.stopCapture();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mHciCloudOcrHelper != null) {
            mHciCloudOcrHelper.release();
        }
        if (mHciCloudSysHelper != null) {
            mHciCloudSysHelper.release();
        }
        super.onDestroy();
    }
}
