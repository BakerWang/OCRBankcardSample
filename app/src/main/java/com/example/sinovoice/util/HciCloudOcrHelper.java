package com.example.sinovoice.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Environment;
import android.util.Log;

import com.sinovoice.hcicloudsdk.android.ocr.capture.CameraPreview;
import com.sinovoice.hcicloudsdk.android.ocr.capture.CaptureEvent;
import com.sinovoice.hcicloudsdk.android.ocr.capture.OCRCapture;
import com.sinovoice.hcicloudsdk.android.ocr.capture.OCRCaptureListener;
import com.sinovoice.hcicloudsdk.common.ocr.OcrConfig;
import com.sinovoice.hcicloudsdk.common.ocr.OcrCornersResult;
import com.sinovoice.hcicloudsdk.common.ocr.OcrInitParam;
import com.sinovoice.hcicloudsdk.common.ocr.OcrRecogRegion;
import com.sinovoice.hcicloudsdk.common.ocr.OcrRecogResult;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by miaochangchun on 2016/12/5.
 */
public class HciCloudOcrHelper {
    private static final String TAG = HciCloudOcrHelper.class.getSimpleName();
    private static HciCloudOcrHelper mHciCloudOcrHelper = null;
    private OCRCapture ocrCapture;

    private HciCloudOcrHelper(){
    }

    public static HciCloudOcrHelper getInstance() {
        if (mHciCloudOcrHelper == null) {
            return new HciCloudOcrHelper();
        }
        return mHciCloudOcrHelper;
    }

    /**
     * 拍照器初始化功能
     * @param context
     * @param initCapkeys
     * @return
     */
    public int init(Context context, String initCapkeys) {
        ocrCapture = new OCRCapture();
        String strConfig = getOcrInitParam(context, initCapkeys);
        int errorCode = ocrCapture.hciOcrCaptureInit(context, strConfig, new ocrCaptureListener());
        return errorCode;
    }

    /**
     *
     * @param context
     * @param initCapkeys
     * @return
     */
    private String getOcrInitParam(Context context, String initCapkeys) {
        OcrInitParam ocrInitParam = new OcrInitParam();
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String packageName = context.getPackageName();
        String dataPath = sdPath + File.separator + "sinovoice"
                + File.separator + packageName + File.separator + "data"
                + File.separator;
        copyData(context, dataPath, "idcard");
        ocrInitParam.addParam(OcrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        ocrInitParam.addParam(OcrInitParam.PARAM_KEY_FILE_FLAG, "none");
        ocrInitParam.addParam(OcrInitParam.PARAM_KEY_INIT_CAP_KEYS, initCapkeys);
        return ocrInitParam.getStringConfig();
    }

    /**
     * 拷贝资源文件
     * @param dataPath  sd卡下的路径
     * @param dataAssetPath    assets目录下的文件夹名称
     */
    private void copyData(Context context, String dataPath, String dataAssetPath) {
        File file = new File(dataPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        AssetManager assetMgr = context.getResources().getAssets();
        try {
            String[] filesList = assetMgr.list(dataAssetPath);
            for (String string : filesList) {
                Log.v(TAG, string);
                copyAssetFile(assetMgr, dataAssetPath + File.separator + string, dataPath + File.separator + string);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把assets目录下的所有文件都拷贝到sd卡
     * @param assetMgr
     * @param src
     * @param dst
     */
    private void copyAssetFile(AssetManager assetMgr, String src, String dst) {
        if (assetMgr == null) {
            throw new NullPointerException("Method param assetMgr is null.");
        }
        if (src == null) {
            throw new NullPointerException("Method param src is null.");
        }
        if (dst == null) {
            throw new NullPointerException("Method param dst is null.");
        }

        InputStream is = null;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            is = assetMgr.open(src, AssetManager.ACCESS_RANDOM);
            dis = new DataInputStream(is);

            File file = new File(dst);
            if (file.exists()) {
                // file.delete();
                return;
            }
            file.createNewFile();

            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);
            byte[] buffer = new byte[1024];

            int len = 0;
            while ((len = dis.read(buffer, 0, buffer.length)) != -1) {
                dos.write(buffer, 0, len);
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                    dis = null;
                }

                if (is != null) {
                    is.close();
                    is = null;
                }

                if (dos != null) {
                    dos.close();
                    dos = null;
                }

                if (fos != null) {
                    fos.close();
                    fos = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拍照器反初始化功能
     * @return
     */
    public int release(){
        int errorCode = 0;
        if (ocrCapture != null) {
            errorCode = ocrCapture.hciOcrCaptureRelease();
        }
        return errorCode;
    }

    /**
     *
     * @param capkey
     */
    public void startCapture(String capkey){
        String strConfig = getOcrRecogParam(capkey);
        ocrCapture.hciOcrCaptureStart(strConfig);
    }

    /**
     * 获取摄像机的预览图片
     * @return
     */
    public CameraPreview previewCapture() {
        return ocrCapture.getCameraPreview();
    }

    /**
     * 关闭拍照器并开始识别
     */
    public void stopCapture(){
        ocrCapture.hciOcrCaptureStopAndRecog();
    }

    /**
     * 文本图片识别，需要获取到图像之后手动调用此函数识别。
     * @param data  拍照器获取的图像数据
     * @param recogRegionList   要识别的区域列表
     */
    public void recogCapture(byte[] data, ArrayList<OcrRecogRegion> recogRegionList){
        String recogConfig = "capkey=ocr.local";
        ocrCapture.hciOcrCaptureRecog(data, recogConfig, recogRegionList);
    }

    private String getOcrRecogParam(String capkey) {
        OcrConfig ocrConfig = new OcrConfig();
        ocrConfig.addParam(OcrConfig.SessionConfig.PARAM_KEY_CAP_KEY, capkey);
//        ocrConfig.addParam(OcrConfig.ResultConfig.);
        return ocrConfig.getStringConfig();
    }


    /**
     * 拍照器回调类
     */
    private class ocrCaptureListener implements OCRCaptureListener{

        @Override
        public void onCaptureEventError(CaptureEvent captureEvent, int i) {

        }

        @Override
        public void onCaptureEventStateChange(CaptureEvent captureEvent) {

        }

        @Override
        public void onCaptureEventCapturing(CaptureEvent captureEvent, byte[] bytes, OcrCornersResult ocrCornersResult) {

        }

        @Override
        public void onCaptureEventRecogFinish(CaptureEvent captureEvent, OcrRecogResult ocrRecogResult) {
            Log.d(TAG, "ocrRecogResult = " + ocrRecogResult.getResultText());
        }
    }
}
