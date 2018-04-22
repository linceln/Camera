package cc.lince.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private int mCameraId = 0;

    public CustomSurfaceView(Context context) {
        this(context, null);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d("camera", "Constructor");
        getHolder().addCallback(this);
    }

    public void setCameraId(int cameraId) {
        this.mCameraId = cameraId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("camera", "surfaceCreated");
        CameraHelper.getInstance().openCamera(mCameraId);
        CameraHelper.getInstance().startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("camera", "surfaceChanged  width: " + width + " height: " + height);
//        Camera camera = CameraHelper.getInstance().getCamera();
//        Camera.Parameters parameters = camera.getParameters();
//        parameters.setPreviewSize(width, height);
//        camera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("camera", "surfaceDestroyed");
        CameraHelper.getInstance().releaseCamera();
    }
}