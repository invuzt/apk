package a.b;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;

public class MainActivity extends Activity {
    static { System.loadLibrary("vuzt_native"); }
    private native String mesinPusatRust(String s);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // --- 1. Layout Akar (Relative) ---
        RelativeLayout root = new RelativeLayout(this);
        root.setPadding(60, 60, 60, 60);
        root.setBackgroundColor(Style.BG_COLOR);

        // --- 2. Kontainer Tengah (Linear) ---
        // Ini adalah kotak transparan di tengah yang menampung elemen
        LinearLayout centralContainer = new LinearLayout(this);
        centralContainer.setOrientation(LinearLayout.VERTICAL);
        // centralContainer.setGravity(Gravity.CENTER); // Mengetengahkan elemen di dalamnya
        
        // Parameter untuk menempatkan centralContainer di tengah RelativeLayout
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); // Kunci Posisi Tengah
        centralContainer.setLayoutParams(containerParams);


        // --- 3. Elemen-elemen UI (Di dalam centralContainer) ---

        // Judul (Plain Text Besar)
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Cak Ru");
        tvTitle.setTextSize(24);
        tvTitle.setTextColor(Style.TEXT_COLOR);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, 60); // Jarak bawah
        centralContainer.addView(tvTitle);

	// Response JNI (Pindah ke bawah kontaine>
        final TextView tvResponse = new TextView(>
        tvResponse.setText("Waiting...");
        tvResponse.setTextColor(Style.TEXT_COLOR);
        tvResponse.setPadding(0, 100, 0, 0); // J>
        tvResponse.setGravity(Gravity.CENTER);
        centralContainer.addView(tvResponse);


        // Sub-judul (Teks Kecil)
        TextView tvSubTitle = new TextView(this);
        tvSubTitle.setText("Enter Text");
        tvSubTitle.setTextSize(14);
        tvSubTitle.setTextColor(Color.parseColor("#DDDDDD"));
        tvSubTitle.setGravity(Gravity.CENTER);
        tvSubTitle.setPadding(0, 0, 0, 60); // Jarak bawah
        centralContainer.addView(tvSubTitle);

        // Input Field
        final EditText etInput = new EditText(this);
        etInput.setHint("input teks"); // Sesuai gambar
        Style.applyInputStyle(etInput);
        centralContainer.addView(etInput);

        // Spacer (Jarak antara input dan tombol)
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 60); // Tinggi spacer
        centralContainer.addView(spacer, spacerParams);

        // Tombol Kuning
        Button btnNext = new Button(this);
        btnNext.setText("Next");
        Style.applyButtonStyle(btnNext);
        centralContainer.addView(btnNext);

        // Response JNI (Pindah ke bawah kontainer tengah)
        // final TextView tvResponse = new TextView(this);
        // tvResponse.setText("Waiting...");
        // tvResponse.setTextColor(Style.TEXT_COLOR);
        // tvResponse.setPadding(0, 100, 0, 0); // Jarak atas yang besar
        // tvResponse.setGravity(Gravity.CENTER);
        // centralContainer.addView(tvResponse);
        
        // Logika Tombol (Tetap Modular)
        btnNext.setOnClickListener(v -> {
            String command = etInput.getText().toString();
            if(!command.isEmpty()){
                String result = mesinPusatRust(command);
                tvResponse.setText(result);
            }
        });


        // --- 4. Selesai & Tampilkan ---
        root.addView(centralContainer);
        setContentView(root);
    }
}
