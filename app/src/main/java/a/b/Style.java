package a.b;
import android.graphics.Color;
import android.widget.*;

public class Style {
    public static int BG = Color.parseColor("#0A0A0A");
    public static int TEXT = Color.parseColor("#00FF00");

    public static void apply(EditText e, Button b) {
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.GRAY);
        b.setBackgroundColor(Color.parseColor("#333333"));
        b.setTextColor(TEXT);
    }
}
