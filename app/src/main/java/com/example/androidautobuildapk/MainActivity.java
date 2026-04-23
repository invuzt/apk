package com.example.androidautobuildapk;

import android.app.Activity;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.*;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private VideoHelper videoHelper = new VideoHelper();
    private CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
    
    private String coords = "", address = "Mencari...";
    private String mode = "CAMERA";

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Panggil XML
        
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        previewView = findViewById(R.id.previewView);
        View btnShutter = findViewById(R.id.btnShutter);
        View btnSwitch = findViewById(R.id.btnSwitch);
        TextView modeVid = findViewById(R.id.modeVid);
        TextView modeCam = findViewById(R.id.modeCam);

        btnShutter.setOnClickListener(v -> {
            if (mode.equals("CAMERA")) PhotoHelper.takePhoto(imageCapture, this, true, coords, address);
            else videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                @Override public void onStarted() { btnShutter.setBackgroundColor(Color.RED); }
                @Override public void onStopped() { btnShutter.setBackgroundColor(Color.WHITE); }
            });
        });

        btnSwitch.setOnClickListener(v -> {
            selector = (selector == CameraSelector.DEFAULT_BACK_CAMERA) ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        modeVid.setOnClickListener(v -> { mode = "VIDEO"; updateUI(modeCam, modeVid); });
        modeCam.setOnClickListener(v -> { mode = "CAMERA"; updateUI(modeCam, modeVid); });

        startCamera();
        setupLocation();
    }

    private void updateUI(TextView cam, TextView vid) {
        if (mode.equals("VIDEO")) {
            vid.setTextColor(Color.BLACK); vid.setBackgroundColor(0xFFAEC6CF);
            cam.setTextColor(Color.WHITE); cam.setBackgroundColor(Color.TRANSPARENT);
        } else {
            cam.setTextColor(Color.BLACK); cam.setBackgroundColor(0xFFAEC6CF);
            vid.setTextColor(Color.WHITE); vid.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cp = ProcessCameraProvider.getInstance(this).get();
                Preview p = new Preview.Builder().build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                videoCapture = VideoCapture.withOutput(new Recorder.Builder().build());
                
                cp.unbindAll();
                cp.bindToLifecycle(this, selector, p, imageCapture, videoCapture);
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
        coords = String.format(Locale.US, "Lat: %.6f, Long: %.6f", l.getLatitude(), l.getLongitude());
    }
}
