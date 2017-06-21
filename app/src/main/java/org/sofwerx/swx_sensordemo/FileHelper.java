package org.sofwerx.swx_sensordemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by thromadka on 6/21/2017.
 *
 * attempt to deal with latest Android security changes to public app file access
 *
 * add the following permission to manifest:
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 */

public class FileHelper {
    Context mContext;

    public FileHelper(Context context) {
        mContext = context;
    }


    public File openPublicFile(String filename) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            Toast.makeText(mContext, "Not allowed to write to SD card!", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!isExternalStorageWritable()) {
            Toast.makeText(mContext, "External storage not writable!", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Create the folder.
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + File.separator  + "swx-sensordemo";
        File folder = new File(path);
        folder.mkdirs();

        // Create the file.
        File file = new File(folder, filename);

        Toast.makeText(mContext, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        return file;
    }

    public void writeToFile(File f, String s) {
        try {
            boolean append = true;
            FileWriter out = new FileWriter(f, append);
            out.write(s);
            out.close();
        } catch (IOException e) {
            //
        }
    }


    public void streamToFile(File f, String s) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(s.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            //
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


}
