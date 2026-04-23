package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView locText;
    private LifecycleRegistry lifecycleRegistry;
    private ImageCapture imageCapture;

    @Override
    public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

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
        locText.setBackgroundColor(Color.parseColor("#80000000"));
        locText.setTextColor(Color.WHITE);
        locText.setPadding(30, 30, 30, 30);
        RelativeLayout.LayoutParams txtLp = new RelativeLayout.LayoutParams(-1, -2);
        root.addView(locText, txtLp);

        Button shutterBtn = new Button(this);
        shutterBtn.setText("AMBIL FOTO");
        shutterBtn.setBackgroundColor(Color.RED);
        shutterBtn.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams btnLp = new RelativeLayout.LayoutParams(-2, -2);
        btnLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnLp.setMargins(0, 0, 0, 100);
        root.addView(shutterBtn, btnLp);

        shutterBtn.setOnClickListener(v -> takePhoto());

        setContentView(root);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA, 
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 101);
        }
        setupLocation();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) {}
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VuztCam");
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                .build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(getBaseContext(), "Foto tersimpan di Galeri!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getBaseContext(), "Gagal: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
        } catch (Exception e) {}
    }

    @Override public void onLocationChanged(Location l) {
        locText.setText("Lat: " + l.getLatitude() + " | Long: " + l.getLongitude());
    }

    @Override protected void onStart() { super.onStart(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.STARTED); }
    @Override protected void onResume() { super.onResume(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.RESUMED); }
    @Override protected void onDestroy() { super.onDestroy(); lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.DESTROYED); }
}
