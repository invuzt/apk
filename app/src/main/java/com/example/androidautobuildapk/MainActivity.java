package com.example.androidautobuildapk;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String s);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        final EditText input = new EditText(this);
        input.setHint("Command...");
        
        final TextView label = new TextView(this);
        label.setText("Result will appear here");

        Button run = new Button(this);
        run.setText("Execute");
        run.setOnClickListener(v -> label.setText(mesinPusatRust(input.getText().toString())));

        layout.addView(input);
        layout.addView(run);
        layout.addView(label);
        setContentView(layout);
    }
}
