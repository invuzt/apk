package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Date;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView locText;
    private LifecycleRegistry lifecycleRegistry;

    @Override
    public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        // Main Layout (RelativeLayout agar bisa tumpang tindih)
        RelativeLayout root = new RelativeLayout(this);

        // 1. Camera Preview (Auto Aspect Ratio)
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, new RelativeLayout.LayoutParams(-1, -1));

        // 2. Info GPS (Overlay Atas)
        locText = new TextView(this);
        locText.setBackgroundColor(Color.parseColor("#80000000"));
        locText.setTextColor(Color.WHITE);
        locText.setPadding(30, 30, 30, 30);
        RelativeLayout.LayoutParams txtLp = new RelativeLayout.LayoutParams(-1, -2);
        txtLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        root.addView(locText, txtLp);

        // 3. Tombol Shutter (Overlay Bawah Tengah)
        Button shutterBtn = new Button(this);
        shutterBtn.setText("AMBIL FOTO");
        shutterBtn.setBackgroundColor(Color.RED);
        shutterBtn.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams btnLp = new RelativeLayout.LayoutParams(-2, -2);
        btnLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnLp.setMargins(0, 0, 0, 100);
        root.addView(shutterBtn, btnLp);

        shutterBtn.setOnClickListener(v -> Toast.makeText(this, "Foto diambil!", Toast.LENGTH_SHORT).show());

        setContentView(root);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
        setupLocation();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
            } catch (Exception e) {}
        }, ContextCompat.getMainExecutor(this));
    }

    private void setupLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
        } catch (Exception e) {}
    }

    @Override public void onLocationChanged(Location l) {
        locText.setText("Lat: " + l.getLatitude() + " | Long: " + l.getLongitude() + "\n" + new Date().toString());
    }

    @Override protected void onStart() { super.onStart(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.STARTED); }
    @Override protected void onResume() { super.onResume(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.RESUMED); }
    @Override protected void onDestroy() { super.onDestroy(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.DESTROYED); }
    
    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] g) {
        if (rc == 101 && g.length > 0 && g[0] == 0) { startCamera(); setupLocation(); }
    }
}
