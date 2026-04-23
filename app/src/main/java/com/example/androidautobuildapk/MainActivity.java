package com.example.androidautobuildapk;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.*;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, LifecycleOwner {
    private PreviewView previewView;
    private TextView locText;
    private LifecycleRegistry lifecycleRegistry;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private String currentAddress = "Mencari Alamat...";
    private String currentCoords = "";
    private boolean isWatermarkOn = true;

    @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycleRegistry; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);

        initUI();
        checkPermissionsAndStart();
    }

    private void initUI() {
        RelativeLayout root = new RelativeLayout(this);
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, -1, -1);

        locText = new TextView(this);
        locText.setBackgroundColor(0x99000000);
        locText.setTextColor(Color.WHITE);
        locText.setPadding(35, 35, 35, 35);
        root.addView(locText, -1, -2);

        // Toggle Watermark
        Button toggle = new Button(this);
        toggle.setText("WTM: ON");
        RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(-2, -2);
        tlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tlp.setMargins(0, 40, 40, 0);
        root.addView(toggle, tlp);
        toggle.setOnClickListener(v -> {
            isWatermarkOn = !isWatermarkOn;
            toggle.setText(isWatermarkOn ? "WTM: ON" : "WTM: OFF");
        });

        // Buttons Container
        LinearLayout row = new LinearLayout(this);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-2, -2);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.setMargins(0, 0, 0, 80);
        root.addView(row, rlp);

        Button btnPhoto = new Button(this); btnPhoto.setText("FOTO"); row.addView(btnPhoto);
        Button btnVideo = new Button(this); btnVideo.setText("VIDEO"); row.addView(btnVideo);

        btnPhoto.setOnClickListener(v -> takePhoto());
        btnVideo.setOnClickListener(v -> toggleVideo(btnVideo));

        setContentView(root);
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Bitmap bmp = imageToBitmap(image);
                if (isWatermarkOn) {
                    bmp = WatermarkService.apply(bmp, currentCoords, currentAddress);
                }
                save(bmp);
                image.close();
            }
        });
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix m = new Matrix();
        m.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private void save(Bitmap bmp) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "Vuzt_" + System.currentTimeMillis() + ".jpg");
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VuztCam");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            OutputStream os = getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
            runOnUiThread(() -> Toast.makeText(this, "Tersimpan!", 0).show());
        } catch (Exception e) {}
    }

    private void toggleVideo(Button btn) {
        if (recording != null) { recording.stop(); recording = null; btn.setText("VIDEO"); return; }
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "Vuzt_Vid_" + System.currentTimeMillis());
        cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VuztCam");
        recording = videoCapture.getOutput()
                .prepareRecording(this, new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setContentValues(cv).build())
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), event -> {
                    if (event instanceof VideoRecordEvent.Start) btn.setText("STOP");
                    else if (event instanceof VideoRecordEvent.Finalize) Toast.makeText(this, "Video OK", 0).show();
                });
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == 0) {
            startCamera(); setupLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, 101);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> f = ProcessCameraProvider.getInstance(this);
        f.addListener(() -> {
            try {
                ProcessCameraProvider cp = f.get();
                Preview p = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                p.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
                videoCapture = VideoCapture.withOutput(new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.LOWEST)).build());
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
