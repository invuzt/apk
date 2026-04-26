package a.b;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import android.view.View;

public class Style {
    // Warna presisi sesuai gambar referensi
    public static int BG_COLOR = Color.parseColor("#1542A1"); // Biru Tua
    public static int INPUT_BG = Color.parseColor("#102B6D"); // Biru Input Gelap
    public static int BTN_COLOR = Color.parseColor("#FFB72B"); // Kuning Tombol
    public static int TEXT_COLOR = Color.WHITE;

    // Membuat background input field dengan sudut tumpul
    public static void applyInputStyle(EditText et) {
        et.setTextColor(TEXT_COLOR);
        et.setHintTextColor(Color.parseColor("#AAAAAA"));
        
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(15); // Sudut tumpul
        shape.setColor(INPUT_BG); // Warna input
        // shape.setStroke(2, Color.parseColor("#204090")); // Opsional: border tipis
        et.setBackground(shape);
        et.setPadding(30, 30, 30, 30); // Padding dalam input
    }

    // Membuat background tombol kuning solid
    public static void applyButtonStyle(Button btn) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(15); // Sudut tumpul tombol
        shape.setColor(BTN_COLOR); // Warna kuning tombol
        btn.setBackground(shape);
        
        btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false); // Jangan huruf besar semua
        btn.setTextSize(16);
    }
}
