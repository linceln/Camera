package cc.lince.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import cc.lince.camera.util.FileUtils;
import cc.lince.camera.util.PhotoUtils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA_PERMISSION = 1;
    private ImageView mIvCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIvCapture = findViewById(R.id.ivCapture);
        mIvCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                capture();
                CameraHelper.getInstance().capture(MainActivity.this, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        camera.startPreview();
                        Log.e("camera", "onPictureTaken");
                        String s = FileUtils.savePicture(MainActivity.this, data);
                        Log.e("camera", "onPictureTaken: " + s);
                        Bitmap bgBitmap = BitmapFactory.decodeFile(s);
                        Bitmap fgBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.capture);
                        Bitmap bitmap = PhotoUtils.compose(bgBitmap, fgBitmap, 0, 0);
                        FileUtils.savePicture(bitmap);
                        mIvCapture.setImageBitmap(bitmap);
                    }
                });
            }
        });

        int numberOfCameras = Camera.getNumberOfCameras();
        Log.d("mCamera", "Camera numbers: " + numberOfCameras);
        // TODO 判断有没有相机以及能不能同时打开前后摄像头

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_CAMERA_PERMISSION);
        } else {
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initView();
            }
        }
    }

    private void initView() {
        CustomSurfaceView backSurfaceView = findViewById(R.id.backSurfaceView);
        backSurfaceView.setCameraId(0);
        CustomSurfaceView frontSurfaceView = findViewById(R.id.frontSurfaceView);
        frontSurfaceView.setCameraId(1);
    }
}
