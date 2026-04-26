package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {
    // Load library Rust
    static { System.loadLibrary("vuzt_native"); }
    
    // Deklarasi fungsi jembatan (Single Entry Point)
    private native String mesinPusatRust(String input);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout Utama (Plain Background)
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 50, 30, 30);
        // Menggunakan warna background gelap standar
        root.setBackgroundColor(Color.parseColor("#1A1A1A"));

        // Judul (Plain Text)
        TextView tvTitle = new TextView(this);
        tvTitle.setText("VUZT CORE - PLAIN");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, 50);
        root.addView(tvTitle);

        // Input Field (Standard Android Look)
        final EditText etInput = new EditText(this);
        etInput.setHint("Ketik perintah di sini...");
        etInput.setHintTextColor(Color.GRAY);
        etInput.setTextColor(Color.WHITE);
        // LayoutParams untuk melebarkan input field
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, 30);
        etInput.setLayoutParams(lp);
        root.addView(etInput);

        // Result Display (Plain Text)
        final TextView tvResult = new TextView(this);
        tvResult.setText("Menunggu Perintah...");
        tvResult.setTextColor(Color.parseColor("#AAAAAA")); // Abu-abu terang
        tvResult.setGravity(Gravity.CENTER);
        tvResult.setPadding(0, 30, 0, 50);
        root.addView(tvResult);

        // Tombol (Standard Android Button - NO GLOW)
        Button btnExecute = new Button(this);
        btnExecute.setText("EKSEKUSI");
        // Kita set background warna solid standar tanpa efek
        btnExecute.setBackgroundColor(Color.parseColor("#444444")); // Abu-abu gelap solid
        btnExecute.setTextColor(Color.WHITE);
        btnExecute.setOnClickListener(v -> {
            String command = etInput.getText().toString();
            // Panggil Jembatan Rust
            String result = mesinPusatRust(command);
            // Tampilkan Hasil
            tvResult.setText(result);
            tvResult.setTextColor(Color.GREEN); // Ubah warna jadi hijau saat sukses
        });
        root.addView(btnExecute);

        setContentView(root);
    }
}
