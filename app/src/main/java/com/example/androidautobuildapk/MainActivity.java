package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    
    // Nama fungsi ini harus sama persis dengan yang di lib.rs (Robusta)
    private native String mesinPusatRust(String input);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);
        root.setBackgroundColor(Color.parseColor("#121212"));

        // Judul
        TextView title = new TextView(this);
        title.setText("VUZT RUST BRIDGE");
        title.setTextColor(Color.CYAN);
        title.setPadding(0, 0, 0, 40);
        root.addView(title);

        // Input Text
        final EditText inputField = new EditText(this);
        inputField.setHint("Ketik sesuatu untuk Rust...");
        inputField.setHintTextColor(Color.GRAY);
        inputField.setTextColor(Color.WHITE);
        root.addView(inputField);

        // Hasil dari Rust
        final TextView display = new TextView(this);
        display.setText("Menunggu hasil...");
        display.setTextColor(Color.LTGRAY);
        display.setPadding(0, 40, 0, 40);
        display.setGravity(Gravity.CENTER);
        root.addView(display);

        // Tombol Proses
        Button btn = new Button(this);
        btn.setText("KIRIM KE RUST");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputField.getText().toString();
                if(!text.isEmpty()) {
                    // Panggil fungsi Rust
                    String hasil = mesinPusatRust(text);
                    display.setText(hasil);
                    display.setTextColor(Color.GREEN);
                }
            }
        });
        root.addView(btn);

        setContentView(root);
    }
}
