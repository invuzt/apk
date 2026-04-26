package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    
    // Jembatan abadi
    private native String mesinPusatRust(String input);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(60, 60, 60, 60);

        EditText inputField = new EditText(this);
        inputField.setHint("Kirim perintah ke Rust...");
        inputField.setTextColor(Color.WHITE);
        root.addView(inputField);

        TextView resultView = new TextView(this);
        resultView.setText("Status: Standby");
        resultView.setTextColor(Color.CYAN);
        resultView.setPadding(0, 50, 0, 50);
        root.addView(resultView);

        Button actionBtn = new Button(this);
        actionBtn.setText("RUN ENGINE");
        actionBtn.setOnClickListener(v -> {
            String out = mesinPusatRust(inputField.getText().toString());
            resultView.setText(out);
        });
        root.addView(actionBtn);

        setContentView(root);
    }
}
