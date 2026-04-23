package com.example.androidautobuildapk;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class PhotoHelper {
    public static void takePhoto(ImageCapture ic, Context ctx, boolean useWtm, String coords, String addr) {
        if (ic == null) return;
        ic.takePicture(androidx.core.content.ContextCompat.getMainExecutor(ctx), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Bitmap bmp = proxyToBitmap(image);
                if (useWtm) bmp = WatermarkService.apply(bmp, coords, addr);
                save(bmp, ctx);
                image.close();
            }
        });
    }

    private static Bitmap proxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix m = new Matrix();
        m.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private static void save(Bitmap bmp, Context ctx) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "Vuzt_" + System.currentTimeMillis() + ".jpg");
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VuztCam");
            Uri uri = ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            OutputStream os = ctx.getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
        } catch (Exception e) {}
    }
}
