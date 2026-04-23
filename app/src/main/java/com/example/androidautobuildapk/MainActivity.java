package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.*;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView locText;
    private LifecycleRegistry lifecycleRegistry;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private String currentAddress = "Mencari Lokasi...";
    private String currentCoords = "";
    private boolean isWatermarkOn = true;

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        RelativeLayout root = new RelativeLayout(this);
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER); 
        root.addView(previewView, new RelativeLayout.LayoutParams(-1, -1));

        locText = new TextView(this);
        locText.setBackgroundColor(Color.parseColor("#99000000"));
        locText.setTextColor(Color.WHITE);
        locText.setPadding(35, 35, 35, 35);
        root.addView(locText, new RelativeLayout.LayoutParams(-1, -2));

        // Tombol Toggle Watermark (Pojok Kanan Atas)
        Button toggleWtm = new Button(this);
        toggleWtm.setText("WTM: ON");
        toggleWtm.setAlpha(0.7f);
        RelativeLayout.LayoutParams wlp = new RelativeLayout.LayoutParams(-2, -2);
        wlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        wlp.setMargins(0, 50, 50, 0);
        root.addView(toggleWtm, wlp);
        
        toggleWtm.setOnClickListener(v -> {
            isWatermarkOn = !isWatermarkOn;
            toggleWtm.setText(isWatermarkOn ? "WTM: ON" : "WTM: OFF");
            locText.setVisibility(isWatermarkOn ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        LinearLayout btnArea = new LinearLayout(this);
        btnArea.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.setMargins(0, 0, 0, 100);
        root.addView(btnArea, lp);

        Button shutterBtn = new Button(this);
        shutterBtn.setText("FOTO");
        btnArea.addView(shutterBtn);

        Button videoBtn = new Button(this);
        videoBtn.setText("VIDEO");
        btnArea.addView(videoBtn);

        shutterBtn.setOnClickListener(v -> takePhoto());
        videoBtn.setOnClickListener(v -> toggleVideo(videoBtn));

        setContentView(root);
        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
            setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO
            }, 101);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cp = cameraProviderFuture.get();
                Preview p = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.LOWEST)).build();
                videoCapture = VideoCapture.withOutput(recorder);
                cp.unbindAll();
                cp.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p, imageCapture, videoCapture);
                lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.RESUMED);
            } catch (Exception e) {}
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                processAndSave(image);
                image.close();
            }
        });
    }

    private void processAndSave(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        
        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        Bitmap mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true);
        
        // HANYA LUKIS WATERMARK JIKA ON
        if (isWatermarkOn) {
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setTextSize(mutableBitmap.getWidth() / 25f);
            paint.setShadowLayer(10, 0, 0, Color.BLACK);

            String time = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
            float x = 50f;
            float yStart = mutableBitmap.getHeight() - 250f;
            
            canvas.drawText(time, x, yStart, paint);
            canvas.drawText(currentCoords, x, yStart + 70, paint);
            
            String addressLine = currentAddress;
            if (addressLine.length() > 50) addressLine = addressLine.substring(0, 47) + "...";
            canvas.drawText(addressLine, x, yStart + 130, paint);
        }

        saveToGallery(mutableBitmap);
    }

    private void saveToGallery(Bitmap bmp) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "Vuzt_" + System.currentTimeMillis() + ".jpg");
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VuztCam");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            OutputStream os = getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
            runOnUiThread(() -> Toast.makeText(this, "Foto Tersimpan!", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {}
    }

    private void toggleVideo(Button btn) {
        if (recording != null) { recording.stop(); recording = null; btn.setText("VIDEO"); return; }
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "Vuzt_Vid_" + System.currentTimeMillis());
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VuztCam");
        recording = videoCapture.getOutput()
                .prepareRecording(this, new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setContentValues(cv).build())
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), event -> {
                    if (event instanceof VideoRecordEvent.Start) btn.setText("STOP");
                    else if (event instanceof VideoRecordEvent.Finalize) Toast.makeText(this, "Video OK!", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        } catch (SecurityException e) {}
    }

    @Override public void onLocationChanged(Location l) {
        currentCoords = String.format(Locale.US, "Lat: %.6f, Long: %.6f", l.getLatitude(), l.getLongitude());
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                currentAddress = addresses.get(0).getAddressLine(0);
            }
        } catch (Exception e) { currentAddress = "Alamat tidak ditemukan"; }
        if (isWatermarkOn) locText.setText(currentCoords + "\n" + currentAddress);
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] g) {
        if (rc == 101 && g.length > 0 && g[0] == 0) { startCamera(); setupLocation(); }
    }
}
