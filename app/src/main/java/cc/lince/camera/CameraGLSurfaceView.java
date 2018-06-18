package cc.lince.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    private static final String TAG = "CameraGLSurfaceView";

    private SurfaceTexture mSurfaceTexture;
    private DirectDrawer mDirectDrawer;
    private OnSurfaceTextureCreated mListener;
    private Camera mCamera;

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setEGLContextClientVersion(2);
        this.setRenderer(this);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setOnSurfaceTextureCreatedListener(OnSurfaceTextureCreated listener) {
        mListener = listener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "Renderer onSurfaceCreated START. Thread" + Thread.currentThread().getName());
        int oesTextureId = getOESTextureId();
        // 创建 Shader
        mDirectDrawer = new DirectDrawer(oesTextureId);
        // 创建一个 SurfaceTexture 绑定到 openGL 外部 OES 纹理
        mSurfaceTexture = new SurfaceTexture(oesTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                // 有可用的帧数据，调用 requestRender 通知 Render 回调 onDrawFrame 开始绘制
                requestRender();
                Log.e(TAG, "SurfaceTexture onFrameAvailable and requestRender. Thread" + Thread.currentThread().getName());
            }
        });
        if (mListener != null) {
            // 通知外部 SurfaceTexture 已经创建完成，可以开启摄像头或者其他操作了
            mListener.onSurfaceTextureCreated(mSurfaceTexture);
        }
        Log.e(TAG, "Renderer onSurfaceCreated END");
    }

    private int getOESTextureId() {
        int[] textureId = new int[1];
        // 生成一个纹理
        GLES20.glGenTextures(textureId.length, textureId, 0);
        // 将此纹理绑定到 OES 外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES, textureId[0]);
        // 设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        // 解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES, 0);
        return textureId[0];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "Render onSurfaceChanged width: " + width + " height: " + height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e(TAG, "Renderer onDrawFrame START. Thread:" + Thread.currentThread().getName());
        GLES20.glClearColor(1f, 1f, 1f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 更新纹理图像
        mSurfaceTexture.updateTexImage();
        Log.e(TAG, "Renderer onDrawFrame: Texture timestamp: " + mSurfaceTexture.getTimestamp());
        float[] matrix = new float[16];
        // 获取外部纹理的变换矩阵
        mSurfaceTexture.getTransformMatrix(matrix);
        // TODO Shader
        mDirectDrawer.draw(matrix);
        Log.e(TAG, "Renderer onDrawFrame END");
    }

    public void startPreview() {
        if (mSurfaceTexture != null) {
            mCamera = Camera.open();
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.addCallbackBuffer(allocBuffer());
                mCamera.addCallbackBuffer(allocBuffer());
                mCamera.addCallbackBuffer(allocBuffer());
                mCamera.setPreviewCallbackWithBuffer(this);
                mCamera.startPreview();
                Log.e(TAG, "Camera start previewing");
            } catch (IOException e) {
                Log.e(TAG, "setPreviewTexture exception");
            }
        } else {
            Log.e(TAG, "Open camera failed: SurfaceTexture not ready");
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 在 SurfaceTexture 的 OnFrameAvailable 之后回调 主线程
        Log.e(TAG, "Camera onPreviewFrame callback: data length:" + data.length
                + ". Thread: " + Thread.currentThread().getName());
        // TODO 开新线程处理数据
        camera.addCallbackBuffer(data);
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.release();
        }
    }

    /**
     * 根据预览格式和预览尺寸获得缓冲
     *
     * @return 缓冲
     */
    private byte[] allocBuffer() {
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size previewSize = parameters.getPreviewSize();
        Log.e(TAG, "Camera previewSize: " + previewSize.width + "x" + previewSize.height);
        int previewFormat = parameters.getPreviewFormat();
        Log.e(TAG, "Camera previewFormat: " + previewFormat);
        int bufferSize = previewSize.width * previewSize.height
                * ImageFormat.getBitsPerPixel(previewFormat) / 8;
        Log.e(TAG, "Camera allocBuffer: " + bufferSize);
        return new byte[bufferSize];
    }

    public interface OnSurfaceTextureCreated {
        void onSurfaceTextureCreated(SurfaceTexture surfaceTexture);
    }
}
