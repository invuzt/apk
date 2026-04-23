package com.example.androidautobuildapk;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.*;
import android.os.Bundle;
import android.view.Gravity;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView txtGps, txtQr, txtCam, txtVid;
    private View btnShutter;
    private LifecycleRegistry lifecycleRegistry;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private VideoHelper videoHelper = new VideoHelper();
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private ProcessCameraProvider cameraProvider;

    private String currentCoords = "", currentAddress = "Mencari Lokasi...";
    private String currentMode = "CAMERA";

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        initLayout();
        checkPermissionsAndStart();
    }

    private void initLayout() {
        RelativeLayout main = new RelativeLayout(this);
        main.setBackgroundColor(Color.BLACK);

        // 1. Top Bar (V Dropdown)
        ImageView topDrop = new ImageView(this);
        topDrop.setImageResource(android.R.drawable.arrow_down_float); // Temporary V icon
        topDrop.setPadding(40, 40, 40, 40);
        RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(-2, -2);
        tlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        main.addView(topDrop, tlp);

        // 2. Camera Preview (Tengah)
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(-1, -1);
        clp.setMargins(0, 150, 0, 600); // Sisakan ruang atas bawah
        main.addView(previewView, clp);

        // Overlay GPS (Sembunyi/Munculkan sesuai mode)
        txtGps = new TextView(this);
        txtGps.setBackgroundColor(0x80000000);
        txtGps.setTextColor(Color.WHITE);
        txtGps.setPadding(20,20,20,20);
        main.addView(txtGps, clp);

        // 3. Bottom Controls Area (Hitam Pekat)
        RelativeLayout bottomArea = new RelativeLayout(this);
        bottomArea.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams blp = new RelativeLayout.LayoutParams(-1, 600);
        blp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        main.addView(bottomArea, blp);

        // a. Main Buttons Row
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-1, -2);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        bottomArea.addView(row, rlp);

        // Kiri: Switch Kamera (Icon)
        ImageView btnSwitch = new ImageView(this);
        btnSwitch.setImageResource(android.R.drawable.ic_menu_rotate);
        btnSwitch.setPadding(50, 50, 50, 50);
        btnSwitch.setBackground(getCircleDrawable(Color.TRANSPARENT, 0x33FFFFFF)); // Ring putih tipis
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(180, 180);
        llp.setMargins(0,0,100,0);
        row.addView(btnSwitch, llp);

        // Tengah: Shutter Besar
        btnShutter = new View(this);
        btnShutter.setBackground(getShutterDrawable(Color.WHITE));
        row.addView(btnShutter, new LinearLayout.LayoutParams(250, 250));

        // Kanan: Galeri (Circle White)
        ImageView btnGal = new ImageView(this);
        btnGal.setBackground(getCircleDrawable(Color.WHITE, 0));
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(180, 180);
        glp.setMargins(100,0,0,0);
        row.addView(btnGal, glp);

        // b. Mode Selector (Bawah Buttons)
        LinearLayout modes = new LinearLayout(this);
        modes.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams mlp = new RelativeLayout.LayoutParams(-1, -2);
        mlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mlp.setMargins(0, 0, 0, 50);
        bottomArea.addView(modes, mlp);

        txtQr = getModeText("QR SCAN");
        txtCam = getModeText("CAMERA");
        txtVid = getModeText("VIDEO");
        modes.addView(txtQr);
        modes.addView(txtCam);
        modes.addView(txtVid);

        updateModeUI(); // Set Camera as default highlight

        // Listeners
        btnShutter.setOnClickListener(v -> handleShutter());
        btnSwitch.setOnClickListener(v -> switchCamera());
        txtCam.setOnClickListener(v -> setMode("CAMERA"));
        txtVid.setOnClickListener(v -> setMode("VIDEO"));

        setContentView(main);
    }

    private TextView getModeText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.GRAY);
        tv.setTextSize(14);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(30, 15, 30, 15);
        return tv;
    }

    private void updateModeUI() {
        txtQr.setTextColor(currentMode.equals("QR") ? Color.WHITE : Color.GRAY);
        txtVid.setTextColor(currentMode.equals("VIDEO") ? Color.WHITE : Color.GRAY);
        
        // Highlight Camera (Seperti gambar contoh: Teks putih, background biru muda oval)
        if (currentMode.equals("CAMERA")) {
            txtCam.setTextColor(Color.BLACK);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(0xFFAEC6CF); // Pastel Blue
            gd.setCornerRadius(50);
            txtCam.setBackground(gd);
        } else {
            txtCam.setTextColor(Color.GRAY);
            txtCam.setBackground(null);
        }
        
        // Update Shutter Color (Video = Merah, Kamera = Putih)
        btnShutter.setBackground(getShutterDrawable(currentMode.equals("VIDEO") ? Color.RED : Color.WHITE));
    }

    private void setMode(String mode) {
        if (currentMode.equals(mode)) return;
        
        // Jika sedang merekam video, paksa stop dulu
        if (currentMode.equals("VIDEO") && videoCapture != null && videoHelper != null) {
             // Stop recording logic here if needed
        }

        currentMode = mode;
        updateModeUI();
    }

    private void handleShutter() {
        if (currentMode.equals("CAMERA")) {
            PhotoHelper.takePhoto(imageCapture, this, true, currentCoords, currentAddress);
        } else if (currentMode.equals("VIDEO")) {
            videoHelper.toggle(videoCapture, this, new VideoHelper.VideoActionCallback() {
                @Override public void onStarted() { /* Custom animation if needed */ }
                @Override public void onStopped() { /* Custom stop if needed */ }
            });
        }
    }

    private void switchCamera() {
        cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) ?
                CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
        startCamera(); // Re-bind with new selector
    }

    private GradientDrawable getCircleDrawable(int color, int strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        if (strokeColor != 0) gd.setStroke(3, strokeColor);
        return gd;
    }

    private GradientDrawable getShutterDrawable(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        gd.setStroke(15, 0x99FFFFFF); // Ring luar transparant putih
        return gd;
    }

    private void startCamera() {
        androidx.camera.lifecycle.ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(this).get();
                Preview p = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.SD)).build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, p, imageCapture, videoCapture);
                lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.RESUMED);
            } catch (Exception e) {}
        }, ContextCompat.getMainExecutor(this));
    }

    // GPS Logic (Tetap seperti sebelumnya)
    private void setupLocation() { /* ... */ }
    @Override public void onLocationChanged(Location l) { /* ... */ locText.setText(currentCoords + "\n" + currentAddress); }
    private void checkPermissionsAndStart() { /* ... */ }
}
