package com.example.androidautobuildapk;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.camera.video.*;
import androidx.core.content.ContextCompat;

public class VideoHelper {
    private Recording currentRecording;

    public void toggle(VideoCapture<Recorder> vc, Context ctx, VideoActionCallback callback) {
        if (currentRecording != null) {
            currentRecording.stop();
            currentRecording = null;
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, "Vuzt_Vid_" + System.currentTimeMillis());
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VuztCam");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(
                ctx.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(cv).build();

        currentRecording = vc.getOutput()
                .prepareRecording(ctx, options)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(ctx), event -> {
                    if (event instanceof VideoRecordEvent.Start) callback.onStarted();
                    else if (event instanceof VideoRecordEvent.Finalize) {
                        callback.onStopped();
                        if (((VideoRecordEvent.Finalize) event).hasError()) {
                            Toast.makeText(ctx, "Video Error!", 0).show();
                        } else {
                            Toast.makeText(ctx, "Video Tersimpan!", 0).show();
                        }
                    }
                });
    }

    public interface VideoActionCallback {
        void onStarted();
        void onStopped();
    }
}
