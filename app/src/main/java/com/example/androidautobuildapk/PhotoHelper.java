package com.example.androidautobuildapk;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PhotoHelper {

    public interface OnPhotoSavedListener {
        void onSaved(Uri uri, Bitmap thumbnail);
    }

    public static void takePhoto(ImageCapture imageCapture, Context context, boolean isWtmOn, String gpsText, OnPhotoSavedListener listener) {
        if (imageCapture == null) return;

        String name = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        
        // Konfigurasi penyimpanan untuk MediaStore (Galeri Publik)
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-App");
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                context.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Uri uri = output.getSavedUri();
                if (uri != null) {
                    try {
                        // Scan file agar langsung muncul di Galeri
                        MediaScannerConnection.scanFile(context, new String[]{uri.getPath()}, null, null);
                        
                        // Buat thumbnail dari URI untuk mini viewer
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                        Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 150, 150, false);

                        if (listener != null) listener.onSaved(uri, thumb);
                    } catch (Exception e) {
                        Log.e("PhotoHelper", "Error processing saved photo", e);
                    }
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exc) {
                Log.e("PhotoHelper", "Capture failed: " + exc.getMessage());
            }
        });
    }
}
