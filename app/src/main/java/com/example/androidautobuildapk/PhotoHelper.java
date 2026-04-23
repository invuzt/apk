package com.example.androidautobuildapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PhotoHelper {

    // Interface untuk mengirim balik data ke MainActivity
    public interface OnPhotoSavedListener {
        void onSaved(Uri uri, Bitmap thumbnail);
    }

    public static void takePhoto(ImageCapture imageCapture, Context context, boolean isWtmOn, String gpsText, OnPhotoSavedListener listener) {
        if (imageCapture == null) return;

        File photoFile = new File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                
                // Buat Thumbnail sederhana untuk mini gallery
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
                
                // Jika watermark ON, kita bisa tambahkan logika proses gambar di sini 
                // (Untuk sekarang kita fokus perbaiki Error Compile dulu)

                if (listener != null) {
                    listener.onSaved(savedUri, thumb);
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("PhotoHelper", "Photo capture failed: " + exception.getMessage());
            }
        });
    }
}
