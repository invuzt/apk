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
import android.os.Environment;
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
    private ImageView btnGallery;
    private String currentMode = "PHOTO";
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

        initViews();
        checkPermissions();
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        txtGps = findViewById(R.id.txtGpsOverlay);
        btnShutter = findViewById(R.id.btnShutter);
        btnGallery = findViewById(R.id.btnGallery);
        modeCam = findViewById(R.id.modeCam);
        modeVid = findViewById(R.id.modeVid);
        flashView = findViewById(R.id.flashView);

        // Galeri yang cerdas (Buka folder foto hasil capture)
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Galeri tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        });

        btnShutter.setOnClickListener(v -> {
            if (currentMode.equals("PHOTO")) {
                triggerFlash();
                PhotoHelper.takePhoto(imageCapture, this, isWtmOn, txtGps.getText().toString(), (uri, thumb) -> {
                    runOnUiThread(() -> btnGallery.setImageBitmap(thumb));
                });
            } else {
                videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                    @Override public void onStarted() { startRecordingAnim(); }
                    @Override public void onStopped() { stopRecordingAnim(); }
                });
            }
        });

        findViewById(R.id.btnSwitch).setOnClickListener(v -> {
            selector = (selector == CameraSelector.DEFAULT_BACK_CAMERA) ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        modeCam.setOnClickListener(v -> setMode("PHOTO"));
        modeVid.setOnClickListener(v -> setMode("VIDEO"));
        findViewById(R.id.btnWtmToggle).setOnClickListener(v -> {
            isWtmOn = !isWtmOn;
            txtGps.setVisibility(isWtmOn ? View.VISIBLE : View.GONE);
        });
    }

    private void setMode(String m) {
        currentMode = m;
        modeCam.setTextColor(m.equals("PHOTO") ? 0xFFFFD700 : 0x88FFFFFF);
        modeVid.setTextColor(m.equals("VIDEO") ? 0xFFFFD700 : 0x88FFFFFF);
        btnShutter.setBackgroundColor(m.equals("VIDEO") ? Color.RED : Color.WHITE);
    }

    private void triggerFlash() {
        flashView.setVisibility(View.VISIBLE);
        AlphaAnimation fade = new AlphaAnimation(1.0f, 0.0f);
        fade.setDuration(250);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationEnd(Animation a) { flashView.setVisibility(View.GONE); }
            @Override public void onAnimationStart(Animation a) {} @Override public void onAnimationRepeat(Animation a) {}
        });
        flashView.startAnimation(fade);
    }

    private void startRecordingAnim() {
        btnShutter.setBackgroundColor(Color.RED);
        ScaleAnimation pulse = new ScaleAnimation(1f, 1.15f, 1f, 1.15f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(600);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        btnShutter.startAnimation(pulse);
    }

    private void stopRecordingAnim() {
        btnShutter.clearAnimation();
        btnShutter.setBackgroundColor(Color.WHITE);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
            setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] p, int[] g) {
        if (rc == 101 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera(); setupLocation();
        }
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
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
        } catch (SecurityException e) {}
    }

    @Override public void onLocationChanged(Location l) {
        txtGps.setText(String.format(Locale.US, "Lat: %.5f Lon: %.5f", l.getLatitude(), l.getLongitude()));
    }
}
