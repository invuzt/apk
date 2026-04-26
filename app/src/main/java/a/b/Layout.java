package a.b;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Layout {
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static LinearLayout.LayoutParams params(int w, int h, int t) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(0, t, 0, 0);
        return p;
    }
}
