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
import java.util.List;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    private PreviewView previewView;
    private TextView txtGpsOverlay, modeCam, modeVid;
    private Button btnWtmToggle;
    private View btnShutter;
    
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private VideoHelper videoHelper = new VideoHelper();
    private CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
    
    private String coords = "", address = "Mencari GPS...";
    private String currentMode = "CAMERA";
    private boolean isWtmOn = true;

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        // Binding UI secara eksplisit
        previewView = findViewById(R.id.previewView);
        txtGpsOverlay = findViewById(R.id.txtGpsOverlay);
        btnWtmToggle = findViewById(R.id.btnWtmToggle);
        btnShutter = findViewById(R.id.btnShutter);
        modeCam = findViewById(R.id.modeCam);
        modeVid = findViewById(R.id.modeVid);
        ImageButton btnSwitch = findViewById(R.id.btnSwitch);

        // Toggle Watermark
        btnWtmToggle.setOnClickListener(v -> {
            isWtmOn = !isWtmOn;
            btnWtmToggle.setText(isWtmOn ? "WTM: ON" : "WTM: OFF");
            txtGpsOverlay.setVisibility(isWtmOn ? View.VISIBLE : View.GONE);
        });

        // Logika Shutter (Foto / Video)
        btnShutter.setOnClickListener(v -> {
            if (currentMode.equals("CAMERA")) {
                PhotoHelper.takePhoto(imageCapture, this, isWtmOn, coords, address);
            } else {
                videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                    @Override public void onStarted() { btnShutter.setBackgroundColor(Color.RED); }
                    @Override public void onStopped() { btnShutter.setBackgroundColor(Color.WHITE); }
                });
            }
        });

        // Switch Kamera (Depan/Belakang)
        btnSwitch.setOnClickListener(v -> {
            selector = (selector == CameraSelector.DEFAULT_BACK_CAMERA) ? 
                       CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        // Ganti Mode
        modeCam.setOnClickListener(v -> setAppMode("CAMERA"));
        modeVid.setOnClickListener(v -> setAppMode("VIDEO"));

        startCamera();
        setupLocation();
    }

    private void setAppMode(String targetMode) {
        currentMode = targetMode;
        if (currentMode.equals("VIDEO")) {
            modeVid.setTextColor(Color.BLACK);
            modeVid.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
            modeCam.setTextColor(Color.WHITE);
            modeCam.setBackground(null);
        } else {
            modeCam.setTextColor(Color.BLACK);
            modeCam.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
            modeVid.setTextColor(Color.WHITE);
            modeVid.setBackground(null);
        }
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cp = ProcessCameraProvider.getInstance(this).get();
                Preview p = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
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
        try {
            List<Address> adr = new Geocoder(this, Locale.getDefault()).getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            if (adr != null && !adr.isEmpty()) address = adr.get(0).getAddressLine(0);
        } catch (Exception e) {}
        txtGpsOverlay.setText(coords + "\n" + address);
    }
}
