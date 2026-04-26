package a.b;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String s);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Root Layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(60, 100, 60, 0);
        root.setBackgroundColor(Style.BG);

        // Judul
        TextView title = new TextView(this);
        title.setText("Forgot Password");
        Style.text(title, "#FFFFFF", 24);

        // Subtitle
        TextView sub = new TextView(this);
        sub.setText("Enter your registered phone number.");
        Style.text(sub, "#DDDDDD", 14);
        sub.setGravity(Gravity.CENTER);

        // Input
        EditText input = new EditText(this);
        input.setHint("62853xxx");
        Style.box(input, "#102B6D", 15);
        input.setPadding(40, 40, 40, 40);

        // Tombol
        Button btn = new Button(this);
        btn.setText("Next");
        Style.box(btn, "#FFB72B", 15);
        btn.setTextColor(Color.WHITE);

        // Response/Status
        TextView res = new TextView(this);
        res.setText("Ready");
        Style.text(res, "#00FF00", 12);
        res.setGravity(Gravity.CENTER);

        // Susun Ke Layar (Panggil Mesin Layout)
        root.addView(title, Layout.params(Layout.WRAP, Layout.WRAP, 0));
        root.addView(sub, Layout.params(Layout.WRAP, Layout.WRAP, 20));
        root.addView(input, Layout.params(Layout.MATCH, Layout.WRAP, 80));
        root.addView(btn, Layout.params(Layout.MATCH, 130, 40));
        root.addView(res, Layout.params(Layout.WRAP, Layout.WRAP, 50));

        // Logika
        btn.setOnClickListener(v -> {
            String out = mesinPusatRust(input.getText().toString());
            res.setText(out);
        });

        setContentView(root);
    }
}
