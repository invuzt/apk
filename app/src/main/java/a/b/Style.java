package a.b;
import android.graphics.Color;
import android.widget.*;
import android.view.View;

public class Style {
    public static int BG = Color.parseColor("#0A0A0A");
    public static int ACCENT = Color.parseColor("#00FF00");
    public static int TEXT = Color.WHITE;

    public static void applyInput(EditText e) {
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.GRAY);
        e.setBackgroundColor(Color.parseColor("#1A1A1A"));
    }

    public static void applyBtn(Button b) {
        b.setBackgroundColor(Color.parseColor("#333333"));
        b.setTextColor(ACCENT);
    }
}
