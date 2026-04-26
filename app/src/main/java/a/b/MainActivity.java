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
        root.setPadding(40, 40, 40, 40);
        root.setBackgroundColor(Style.BG);

        EditText in = new EditText(this);
        in.setHint("Input...");
        Style.applyInput(in);

        TextView out = new TextView(this);
        out.setText("Ready");
        out.setTextColor(Style.TEXT);
        out.setPadding(0, 30, 0, 30);

        Button btn = new Button(this);
        btn.setText("EXECUTE");
        Style.applyBtn(btn);
        
        btn.setOnClickListener(v -> out.setText(mesinPusatRust(in.getText().toString())));

        root.addView(in);
        root.addView(out);
        root.addView(btn);
        setContentView(root);
    }
}
