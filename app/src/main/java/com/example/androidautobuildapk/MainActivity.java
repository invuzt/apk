package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);

        final TextView status = new TextView(this);
        status.setText("VUZT READY");
        status.setTextColor(Color.WHITE);
        status.setGravity(Gravity.CENTER);

        layout.addView(status);

        String[] cmds = {"PROSES", "SETTING", "HISTORY"};
        for (final String c : cmds) {
            Button b = new Button(this);
            b.setText(c);
            // MENGGUNAKAN ANONYMOUS CLASS (Bukan Lambda)
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
