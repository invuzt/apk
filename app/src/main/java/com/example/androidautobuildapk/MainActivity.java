package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Root Layout (M3 Dark Surface)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#1C1B1F")); // M3 Dark Background
        layout.setPadding(60, 60, 60, 60);

        final TextView status = new TextView(this);
        status.setText("VUZT SYSTEM");
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        status.setTextColor(Color.parseColor("#E6E1E5")); // M3 On Surface
        status.setPadding(0, 0, 0, 100);
        layout.addView(status);

        String[] cmds = {"PROSES", "SETTING", "HISTORY"};
        for (final String c : cmds) {
            Button b = new Button(this);
            b.setText(c);
            b.setTextColor(Color.parseColor("#1C1B1F")); // Text color on primary
            b.setAllCaps(false); // M3 tidak pakai ALL CAPS
            
            // Membuat Shape Material 3 (Rounded Rectangle) secara programatik
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(100f); // Fully Rounded
            shape.setColor(Color.parseColor("#D0BCFF")); // M3 Primary Color (Purple)
            b.setBackground(shape);

            // Margin antar tombol
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140 // Tinggi standar M3 (sekitar 48-56dp)
            );
            params.setMargins(0, 20, 0, 20);
            b.setLayoutParams(params);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String res = mesinPusatRust(c);
                    String[] p = res.split("\\|");
                    if (p.length >= 2) {
                        status.setText(p[0].substring(6));
                        status.setTextColor(Color.parseColor(p[1].substring(6)));
                    }
                }
            });
            layout.addView(b);
        }
        setContentView(layout);
    }
}
