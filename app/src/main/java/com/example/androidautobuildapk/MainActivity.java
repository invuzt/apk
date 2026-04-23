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
import java.util.List;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    private PreviewView previewView;
    private TextView txtGps, modeCam, modeVid;
    private View btnShutter, wtmContainer;
    private String currentMode = "CAMERA";
    private boolean isWtmOn = true;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private VideoHelper videoHelper = new VideoHelper();
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        previewView = findViewById(R.id.previewView);
        txtGps = findViewById(R.id.txtGpsOverlay);
        wtmContainer = findViewById(R.id.wtmContainer);
        btnShutter = findViewById(R.id.btnShutter);
        modeCam = findViewById(R.id.modeCam);
        modeVid = findViewById(R.id.modeVid);

        // CEK IZIN SAAT START
        if (checkAllPermissions()) {
            startCamera();
            setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, 
                Manifest.permission.ACCESS_FINE_LOCATION, 
                Manifest.permission.RECORD_AUDIO
            }, 1001);
        }

        findViewById(R.id.btnWtmToggle).setOnClickListener(v -> {
            isWtmOn = !isWtmOn;
            ((Button)v).setText(isWtmOn ? "WTM: ON" : "WTM: OFF");
            wtmContainer.setVisibility(isWtmOn ? View.VISIBLE : View.GONE);
        });

        btnShutter.setOnClickListener(v -> {
            if (currentMode.equals("CAMERA")) PhotoHelper.takePhoto(imageCapture, this, isWtmOn, txtGps.getText().toString(), "");
            else videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                @Override public void onStarted() { btnShutter.setBackgroundColor(Color.RED); }
                @Override public void onStopped() { btnShutter.setBackgroundColor(Color.WHITE); }
            });
        });

        findViewById(R.id.btnSwitch).setOnClickListener(v -> {
            cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        modeCam.setOnClickListener(v -> setMode("CAMERA"));
        modeVid.setOnClickListener(v -> setMode("VIDEO"));
    }

    private void setMode(String m) {
        currentMode = m;
        modeCam.setTextColor(m.equals("CAMERA") ? 0xFFFFCC00 : Color.WHITE);
        modeVid.setTextColor(m.equals("VIDEO") ? 0xFFFFCC00 : Color.WHITE);
        btnShutter.setBackgroundColor(m.equals("VIDEO") ? Color.RED : Color.WHITE);
    }

    private boolean checkAllPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] p, int[] g) {
        if (rc == 1001 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera(); setupLocation();
        } else {
            Toast.makeText(this, "Izin Kamera & Lokasi Wajib!", Toast.LENGTH_LONG).show();
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
                cp.bindToLifecycle(this, cameraSelector, p, imageCapture, videoCapture);
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
        try {
            List<Address> adr = new Geocoder(this, Locale.getDefault()).getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            String addr = (adr != null && !adr.isEmpty()) ? adr.get(0).getAddressLine(0) : "Lokasi tidak dikenal";
            txtGps.setText(String.format("Lat: %.5f, Lon: %.5f\n%s", l.getLatitude(), l.getLongitude(), addr));
        } catch (Exception e) {}
    }
}
