package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Typeface;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("vuzt_native");
    }

    private native String mesinPusatRust(String perintah);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 40, 40, 40);
        root.setBackgroundColor(Color.parseColor("#121212"));

        final TextView logView = new TextView(this);
        logView.setText("SYSTEM LOG: READY");
        logView.setTextColor(Color.GRAY);
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setPadding(0, 0, 0, 50);

        final TextView statusView = new TextView(this);
        statusView.setText("IDLE");
        statusView.setTextSize(22);
        statusView.setTextColor(Color.WHITE);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 0, 0, 80);

        // Helper untuk memanggil Rust
        final ViewUpdater updater = new ViewUpdater() {
            @Override
            public void update(String cmd) {
                try {
                    String rawRespon = mesinPusatRust(cmd);
                    String[] parts = rawRespon.split("\\|");
                    statusView.setText(parts[0].replace("STATE:", ""));
                    statusView.setTextColor(Color.parseColor(parts[1].replace("COLOR:", "")));
                    logView.setText("LOG: " + parts[2].replace("MSG:", ""));
                } catch (Exception e) {
                    logView.setText("ERROR: " + e.getMessage());
                }
            }
        };

        Button btnProses = createButton("EKSEKUSI DATA", "PROSES", updater);
        Button btnSetting = createButton("PENGATURAN", "SETTING", updater);
        Button btnHistory = createButton("RIWAYAT", "HISTORY", updater);

        root.addView(logView);
        root.addView(statusView);
        root.addView(btnProses);
        root.addView(btnSetting);
        root.addView(btnHistory);

        setContentView(root);
    }

    private Button createButton(String label, final String cmd, final ViewUpdater updater) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setOnClickListener(v -> updater.update(cmd));
        return btn;
    }

    interface ViewUpdater {
        void update(String cmd);
    }
}
