package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.*;
import android.widget.*;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.*;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    private PreviewView previewView;
    private TextView txtGps, modeCam, modeVid;
    private View btnShutter, flashView;
    private ImageView btnGallery; // Ubah ke ImageView untuk thumbnail
    private String currentMode = "CAMERA";
    private boolean isWtmOn = true;
    
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private VideoHelper videoHelper = new VideoHelper();
    private CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        previewView = findViewById(R.id.previewView);
        txtGps = findViewById(R.id.txtGpsOverlay);
        btnShutter = findViewById(R.id.btnShutter);
        btnGallery = findViewById(R.id.btnGallery);
        modeCam = findViewById(R.id.modeCam);
        modeVid = findViewById(R.id.modeVid);
        flashView = findViewById(R.id.flashView);

        if (allPermissionsGranted()) {
            startCamera();
            setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO
            }, 101);
        }

        // Buka Galeri saat diklik
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        btnShutter.setOnClickListener(v -> {
            if (currentMode.equals("CAMERA")) {
                showCaptureFeedback();
                // Kirim listener ke PhotoHelper agar kita dapat filenya
                PhotoHelper.takePhoto(imageCapture, this, isWtmOn, txtGps.getText().toString(), new PhotoHelper.OnPhotoSavedListener() {
                    @Override public void onSaved(Uri uri, Bitmap thumbnail) {
                        runOnUiThread(() -> btnGallery.setImageBitmap(thumbnail));
                    }
                });
            } else {
                videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                    @Override public void onStarted() { startVideoAnimation(); }
                    @Override public void onStopped() { stopVideoAnimation(); }
                });
            }
        });

        findViewById(R.id.btnSwitch).setOnClickListener(v -> {
            selector = (selector == CameraSelector.DEFAULT_BACK_CAMERA) ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        modeCam.setOnClickListener(v -> switchMode("CAMERA"));
        modeVid.setOnClickListener(v -> switchMode("VIDEO"));
    }

    private void showCaptureFeedback() {
        flashView.setVisibility(View.VISIBLE);
        AlphaAnimation fade = new AlphaAnimation(1.0f, 0.0f);
        fade.setDuration(300);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationEnd(Animation a) { flashView.setVisibility(View.GONE); }
            @Override public void onAnimationStart(Animation a) {} @Override public void onAnimationRepeat(Animation a) {}
        });
        flashView.startAnimation(fade);
    }

    private void startVideoAnimation() {
        btnShutter.setBackgroundColor(Color.RED);
        ScaleAnimation pulse = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(500);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        btnShutter.startAnimation(pulse);
    }

    private void stopVideoAnimation() {
        btnShutter.clearAnimation();
        btnShutter.setBackgroundColor(Color.WHITE);
        btnShutter.setScaleX(1f); btnShutter.setScaleY(1f);
    }

    private void switchMode(String m) {
        currentMode = m;
        modeCam.setTextColor(m.equals("CAMERA") ? 0xFFFFCC00 : Color.WHITE);
        modeVid.setTextColor(m.equals("VIDEO") ? 0xFFFFCC00 : Color.WHITE);
        btnShutter.setBackgroundColor(m.equals("VIDEO") ? Color.RED : Color.WHITE);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cp = ProcessCameraProvider.getInstance(this).get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                videoCapture = VideoCapture.withOutput(new Recorder.Builder().build());
                cp.unbindAll();
                cp.bindToLifecycle(this, selector, preview, imageCapture, videoCapture);
                lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.RESUMED);
            } catch (Exception e) {}
        }, ContextCompat.getMainExecutor(this));
    }

    private void setupLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        } catch (SecurityException e) {}
    }

    @Override public void onLocationChanged(Location l) {
        txtGps.setText(String.format(Locale.US, "Lat: %.5f\nLon: %.5f", l.getLatitude(), l.getLongitude()));
    }
}
