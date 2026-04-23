package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.view.View;
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
    private View btnShutter;
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
        modeCam = findViewById(R.id.modeCam);
        modeVid = findViewById(R.id.modeVid);

        if (allPermissionsGranted()) {
            startCamera();
            setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO
            }, 101);
        }

        // Action Handlers
        findViewById(R.id.btnWtmToggle).setOnClickListener(v -> {
            isWtmOn = !isWtmOn;
            ((Button)v).setText(isWtmOn ? "WTM: ON" : "WTM: OFF");
            txtGps.setVisibility(isWtmOn ? View.VISIBLE : View.GONE);
        });

        btnShutter.setOnClickListener(v -> {
            if (currentMode.equals("CAMERA")) {
                PhotoHelper.takePhoto(imageCapture, this, isWtmOn, txtGps.getText().toString(), "");
            } else {
                videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                    @Override public void onStarted() { btnShutter.setBackgroundColor(Color.RED); }
                    @Override public void onStopped() { btnShutter.setBackgroundColor(Color.WHITE); }
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

    private void switchMode(String m) {
        currentMode = m;
        modeCam.setTextColor(m.equals("CAMERA") ? 0xFFFFCC00 : Color.WHITE);
        modeVid.setTextColor(m.equals("VIDEO") ? 0xFFFFCC00 : Color.WHITE);
        btnShutter.setBackgroundColor(m.equals("VIDEO") ? Color.RED : Color.WHITE);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
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

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.SD))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

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
