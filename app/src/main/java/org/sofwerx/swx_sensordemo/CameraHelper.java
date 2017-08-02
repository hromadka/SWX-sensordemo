package org.sofwerx.swx_sensordemo;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by thromadka on 6/23/2017.
 * Based on https://github.com/googlesamples/android-Camera2Basic/
 * and the deprecated Camera version found at
 * http://www.vogella.com/tutorials/AndroidCamera/article.html
 */

public class CameraHelper implements PictureCallback {

    // want photo taken without user interaction...
    private final Context context;

    public CameraHelper(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

//            Log.d(MainActivity.DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
//            Log.d(MainActivity.DEBUG_TAG, "File" + filename + "not saved: " + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }
}


/** more code from Vogella tutorial to show usage.  Deprecated, not going to work.
 *
 * package de.vogella.camera.api;

 import android.app.Activity;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 import de.vogella.cameara.api.R;

 public class MakePhotoActivity extends Activity {
 private final static String DEBUG_TAG = "MakePhotoActivity";
 private Camera camera;
 private int cameraId = 0;

 @Override
 public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.main);

 // do we have a camera?
 if (!getPackageManager()
 .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
 Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
 .show();
 } else {
 cameraId = findFrontFacingCamera();
 if (cameraId < 0) {
 Toast.makeText(this, "No front facing camera found.",
 Toast.LENGTH_LONG).show();
 } else {
 camera = Camera.open(cameraId);
 }
 }
 }

 public void onClick(View view) {
 camera.startPreview();
 camera.takePicture(null, null,
 new PhotoHandler(getApplicationContext()));
 }

 private int findFrontFacingCamera() {
 int cameraId = -1;
 // Search for the front facing camera
 int numberOfCameras = Camera.getNumberOfCameras();
 for (int i = 0; i < numberOfCameras; i++) {
 CameraInfo info = new CameraInfo();
 Camera.getCameraInfo(i, info);
 if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
 Log.d(DEBUG_TAG, "Camera found");
 cameraId = i;
 break;
 }
 }
 return cameraId;
 }

 @Override
 protected void onPause() {
 if (camera != null) {
 camera.release();
 camera = null;
 }
 super.onPause();
 }
 *
 *
 */
