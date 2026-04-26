package a.b;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.Gravity;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String s);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 50, 50, 50);
        root.setBackgroundColor(Style.BG);

        EditText in = new EditText(this);
        in.setHint("Input...");
        
        TextView out = new TextView(this);
        out.setText("Vuzt Ready.");
        out.setTextColor(Style.TEXT);
        out.setPadding(0, 40, 0, 40);

        Button btn = new Button(this);
        btn.setText("RUN");
        Style.apply(in, btn);
        
        btn.setOnClickListener(v -> out.setText(mesinPusatRust(in.getText().toString())));

        root.addView(in);
        root.addView(out);
        root.addView(btn);
        setContentView(root);
    }
}
