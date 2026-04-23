package com.example.androidautobuildapk;

import android.graphics.*;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WatermarkService {
    public static Bitmap apply(Bitmap source, String coords, String address) {
        Bitmap mutable = source.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutable);
        
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(12, 0, 0, Color.BLACK);
        
        float margin = source.getWidth() * 0.05f;
        float maxWidth = source.getWidth() - (margin * 2);
        
        // 1. Siapkan data teks (Urutan: Alamat -> Coords -> Waktu dari bawah ke atas)
        String time = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
        
        // Gambarkan dari bawah ke atas agar tidak tumpang tindih
        float currentY = source.getHeight() - margin;

        // Gambar Alamat (Bisa multi-baris)
        paint.setTextSize(source.getWidth() / 32f);
        paint.setColor(Color.YELLOW);
        StaticLayout addressLayout = new StaticLayout(address, paint, (int)maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        currentY -= addressLayout.getHeight();
        canvas.save();
        canvas.translate(margin, currentY);
        addressLayout.draw(canvas);
        canvas.restore();

        // Gambar Koordinat
        paint.setColor(Color.WHITE);
        paint.setTextSize(source.getWidth() / 28f);
        currentY -= (paint.getTextSize() + 20);
        canvas.drawText(coords, margin, currentY, paint);

        // Gambar Waktu
        paint.setTextSize(source.getWidth() / 25f);
        currentY -= (paint.getTextSize() + 10);
        canvas.drawText(time, margin, currentY, paint);

        return mutable;
    }
}
