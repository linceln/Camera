package cc.lince.camera;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 2018/5/10
 */
public class GLRenderer implements GLSurfaceView.Renderer {

    private GLSurfaceView glSurfaceView;
    private OnSurfaceTextureListener onSurfaceTextureListener;
    private SurfaceTexture surfaceTexture;

    public GLRenderer(GLSurfaceView glSurfaceView, OnSurfaceTextureListener onSurfaceTextureListener) {
        this.glSurfaceView = glSurfaceView;
        this.onSurfaceTextureListener = onSurfaceTextureListener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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

        // 创建一个 SurfaceTexture 绑定到 openGL 外部 OES 纹理
        surfaceTexture = new SurfaceTexture(textureId[0]);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                glSurfaceView.requestRender();
            }
        });
        if (onSurfaceTextureListener != null) {
            onSurfaceTextureListener.onSurfaceTextureAvailable(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 更新纹理图像
        surfaceTexture.updateTexImage();
        float[] matrix = new float[16];
        // 获取外部纹理的变换矩阵
        surfaceTexture.getTransformMatrix(matrix);

        // TODO
    }

    public interface OnSurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture);
    }
}
