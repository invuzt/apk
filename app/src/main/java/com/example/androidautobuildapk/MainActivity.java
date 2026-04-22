package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#1C1B1F"));
        root.setPadding(50, 50, 50, 50);

        final TextView display = new TextView(this);
        display.setText("VUZT AI MULTI-MODE");
        display.setTextColor(Color.WHITE);
        display.setGravity(Gravity.CENTER);
        display.setPadding(0, 0, 0, 80);
        root.addView(display);

        // Tombol Chat
        root.addView(buatTombol("KIRIM PESAN (CHAT)", "#D0BCFF", new View.OnClickListener() {
            public void onClick(View v) { update("CHAT:Halo AI!", display); }
        }));

        // Tombol Agent
        root.addView(buatTombol("EKSEKUSI AGENT", "#FFB4AB", new View.OnClickListener() {
            public void onClick(View v) { update("AGENT:OPTIMASI", display); }
        }));

        setContentView(root);
    }

    private Button buatTombol(String label, String color, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.BLACK);
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(100);
        gd.setColor(Color.parseColor(color));
        b.setBackground(gd);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 140);
        lp.setMargins(0, 20, 0, 20);
        b.setLayoutParams(lp);
        b.setOnClickListener(listener);
        return b;
    }

    private void update(String req, TextView tv) {
        String res = mesinPusatRust(req);
        String[] p = res.split("\\|");
        tv.setText(p[2].substring(4));
        tv.setTextColor(Color.parseColor(p[1].substring(6)));
    }
}
