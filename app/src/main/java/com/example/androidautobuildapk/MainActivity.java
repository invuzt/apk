package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Color;
import java.util.Date;

public class MainActivity extends Activity implements SurfaceHolder.Callback, LocationListener {
    private Camera mCamera;
    private SurfaceView mPreview;
    private TextView locText;
    private LocationManager locManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout Setup
        FrameLayout root = new FrameLayout(this);
        mPreview = new SurfaceView(this);
        mPreview.getHolder().addCallback(this);
        root.addView(mPreview);

        locText = new TextView(this);
        locText.setBackgroundColor(Color.parseColor("#99000000"));
        locText.setTextColor(Color.WHITE);
        locText.setPadding(40, 40, 40, 40);
        
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2);
        lp.gravity = android.view.Gravity.BOTTOM;
        root.addView(locText, lp);

        setContentView(root);

        // Cek Izin Kamera & GPS
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            requestPermissions(new String[]{
                Manifest.permission.CAMERA, 
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 101);
            locText.setText("Menunggu izin kamera...");
        } else {
            setupLocation();
        }
    }

    private void startCamera(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            locText.setText("Kamera Aktif. Mencari GPS...");
        } catch (Exception e) {
            locText.setText("Gagal: " + e.getMessage());
        }
    }

    private void setupLocation() {
        try {
            locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        } catch (SecurityException e) {}
    }

    // INI KUNCINYA: Langsung start setelah izin diberikan
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Izin diberikan, pancing ulang Surface kamera
            startCamera(mPreview.getHolder());
            setupLocation();
        } else {
            locText.setText("Izin ditolak. Aplikasi tidak bisa berjalan.");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(holder);
        }
    }

    @Override public void onLocationChanged(Location l) {
        locText.setText("Vuzt Cam\nLat: " + l.getLatitude() + "\nLong: " + l.getLongitude() + "\n" + new Date().toString());
    }

    @Override public void surfaceDestroyed(SurfaceHolder h) {
        if (mCamera != null) { mCamera.stopPreview(); mCamera.release(); mCamera = null; }
    }
    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int h1) {}
    @Override public void onStatusChanged(String p, int s, Bundle e) {}
    @Override public void onProviderEnabled(String p) {}
    @Override public void onProviderDisabled(String p) {}
}
