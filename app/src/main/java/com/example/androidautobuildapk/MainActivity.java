package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);
        root.setBackgroundColor(Color.parseColor("#121212"));

        final TextView display = new TextView(this);
        display.setText("VUZT CORE");
        display.setTextColor(Color.WHITE);
        display.setTextSize(20);
        display.setPadding(0, 0, 0, 64);
        root.addView(display);

        root.addView(tombol("CHAT MODE", "CHAT:Halo", display));
        root.addView(tombol("AGENT MODE", "AGENT:OPTIMASI", display));

        setContentView(root);
    }

    private Button tombol(String teks, final String cmd, final TextView tv) {
        Button b = new Button(this);
        b.setText(teks);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] res = mesinPusatRust(cmd).split("\\|");
                tv.setText(res[2].substring(4));
                tv.setTextColor(Color.parseColor(res[1].substring(6)));
            }
        });
        return b;
    }
}
