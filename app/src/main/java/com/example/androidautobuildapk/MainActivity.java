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
import android.view.View;
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
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView locText;
    private LifecycleRegistry lifecycleRegistry;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private String currentGps = "No GPS";

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        RelativeLayout root = new RelativeLayout(this);
        previewView = new PreviewView(this);
        root.addView(previewView, new RelativeLayout.LayoutParams(-1, -1));

        locText = new TextView(this);
        locText.setBackgroundColor(Color.parseColor("#80000000"));
        locText.setTextColor(Color.WHITE);
        locText.setPadding(20, 20, 20, 20);
        root.addView(locText, new RelativeLayout.LayoutParams(-1, -2));

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

        shutterBtn.setOnClickListener(v -> takePhotoWithWatermark());
        videoBtn.setOnClickListener(v -> toggleVideo(videoBtn));

        setContentView(root);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO
            }, 101);
        }
        setupLocation();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cp = cameraProviderFuture.get();
                Preview p = new Preview.Builder().build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder().build();
                Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.LOWEST)).build();
                videoCapture = VideoCapture.withOutput(recorder);

                cp.unbindAll();
                cp.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p, imageCapture, videoCapture);
            } catch (Exception e) {}
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhotoWithWatermark() {
        if (imageCapture == null) return;
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                saveImageWithWatermark(image);
                image.close();
            }
        });
    }

    private void saveImageWithWatermark(ImageProxy image) {
        try {
            Bitmap bitmap = previewView.getBitmap(); // Ambil dari preview untuk mendapatkan watermark
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.YELLOW);
            paint.setTextSize(bitmap.getHeight() / 25f);
            paint.setShadowLayer(5, 0, 0, Color.BLACK);
            
            canvas.drawText(currentGps, 50, bitmap.getHeight() - 100, paint);
            canvas.drawText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(System.currentTimeMillis()), 50, bitmap.getHeight() - 50, paint);

            String name = "Vuzt_" + System.currentTimeMillis() + ".jpg";
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VuztCam");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            OutputStream os = getContentResolver().openOutputStream(uri);
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
            os.close();
            runOnUiThread(() -> Toast.makeText(this, "Foto + Watermark Tersimpan!", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {}
    }

    private void toggleVideo(Button btn) {
        if (recording != null) {
            recording.stop();
            recording = null;
            btn.setText("VIDEO");
            return;
        }
        
        String name = "Vuzt_Vid_" + System.currentTimeMillis();
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VuztCam");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setContentValues(cv).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return;

        recording = videoCapture.getOutput().prepareRecording(this, options).withAudioEnabled().start(ContextCompat.getMainExecutor(this), event -> {
            if (event instanceof VideoRecordEvent.Start) { btn.setText("STOP"); }
            else if (event instanceof VideoRecordEvent.Finalize) { Toast.makeText(this, "Video Tersimpan!", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void setupLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        } catch (SecurityException e) {}
    }

    @Override public void onLocationChanged(Location l) {
        currentGps = String.format(Locale.US, "Lat: %.6f | Long: %.6f", l.getLatitude(), l.getLongitude());
        locText.setText(currentGps);
    }

    @Override protected void onStart() { super.onStart(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.STARTED); }
    @Override protected void onResume() { super.onResume(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.RESUMED); }
    @Override protected void onDestroy() { super.onDestroy(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.DESTROYED); }
}
