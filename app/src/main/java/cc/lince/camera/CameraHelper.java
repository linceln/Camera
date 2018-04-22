package cc.lince.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraHelper {

    private static CameraHelper mInstance = new CameraHelper();

    private Camera mCamera;

    private CameraHelper() {
    }

    public static CameraHelper getInstance() {
        return mInstance;
    }

    public Camera getCamera() {
        checkCamera();
        return mCamera;
    }

    public void openCamera(int cameraId) {
        mCamera = Camera.open(cameraId);
        setCameraDisplayOrientation(cameraId, mCamera);
        setPreviewSize(cameraId);
    }

    public void startPreview(SurfaceHolder holder) {
        checkCamera();
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.e("camera", "设置预览界面失败: " + e.getMessage());
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    public void capture(final Context context, Camera.PictureCallback pictureCallback) {
        checkCamera();
        mCamera.takePicture(null, null, pictureCallback);
    }

    public void releaseCamera() {
        checkCamera();
//        mCamera.stopPreview();
        mCamera.release();
    }

    /**
     * 设置画面的旋转角度
     *
     * @param cameraId 摄像头
     * @param camera   {@link Camera}
     */
    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        checkCamera();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void setPreviewSize(int cameraId) {
        checkCamera();

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = parameters.getPreviewSize();
//            parameters.setPreviewSize(320, 240);
//            mCamera.setParameters(parameters);
        }
    }

    /**
     * 检查 Camera 是否初始化
     */
    private void checkCamera() {
        if (mCamera == null) {
            throw new NullPointerException("Open camera first");
        }
    }
}
