package com.example.androidautobuildapk;

import android.app.Activity;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.widget.*;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.*;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView locText;
    private LifecycleRegistry lifecycleRegistry;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private VideoHelper videoHelper = new VideoHelper();
    
    private String currentCoords = "", currentAddress = "Mencari Lokasi...";
    private boolean isWtmOn = true;

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        initLayout();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == 0) {
            startCamera(); setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                "android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION", "android.permission.RECORD_AUDIO"
            }, 101);
        }
    }

    private void initLayout() {
        RelativeLayout root = new RelativeLayout(this);
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, -1, -1);

        locText = new TextView(this);
        locText.setBackgroundColor(0x80000000);
        locText.setTextColor(Color.WHITE);
        locText.setPadding(30, 30, 30, 30);
        root.addView(locText, -1, -2);

        LinearLayout btns = new LinearLayout(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.setMargins(0,0,0,100);
        root.addView(btns, lp);

        Button btnF = new Button(this); btnF.setText("FOTO"); btns.addView(btnF);
        Button btnV = new Button(this); btnV.setText("VIDEO"); btns.addView(btnV);
        
        btnF.setOnClickListener(v -> PhotoHelper.takePhoto(imageCapture, this, isWtmOn, currentCoords, currentAddress));
        btnV.setOnClickListener(v -> videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
            @Override public void onStarted() { btnV.setText("STOP"); }
            @Override public void onStopped() { btnV.setText("VIDEO"); }
        }));

        setContentView(root);
    }

    private void startCamera() {
        androidx.camera.lifecycle.ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cp = ProcessCameraProvider.getInstance(this).get();
                Preview p = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                
                Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.SD)).build();
                videoCapture = VideoCapture.withOutput(recorder);

                cp.unbindAll();
                cp.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p, imageCapture, videoCapture);
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
        currentCoords = String.format(Locale.US, "Lat: %.6f, Long: %.6f", l.getLatitude(), l.getLongitude());
        try {
            List<Address> adr = new Geocoder(this, Locale.getDefault()).getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            if (adr != null && !adr.isEmpty()) currentAddress = adr.get(0).getAddressLine(0);
        } catch (Exception e) {}
        locText.setText(currentCoords + "\n" + currentAddress);
    }
}
