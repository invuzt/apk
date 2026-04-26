package a.b;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

public class Style {
    public static final int BG = Color.parseColor("#1542A1");
    
    public static void box(View v, String color, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(color));
        gd.setCornerRadius(radius);
        v.setBackground(gd);
    }

    public static void text(TextView t, String color, float size) {
        t.setTextColor(Color.parseColor(color));
        t.setTextSize(size);
    }
}
