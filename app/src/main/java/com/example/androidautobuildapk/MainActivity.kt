package com.example.androidautobuildapk

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {

    companion object {
        init { System.loadLibrary("vuzt_native") }
    }

    private external fun mesinPusatRust(perintah: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        // --- UI Komponen ---
        val logView = TextView(this).apply {
            text = "SYSTEM LOG: READY"
            textSize = 12f
            setTextColor(Color.GRAY)
            typeface = Typeface.MONOSPACE
            setPadding(0, 0, 0, 50)
        }

        val statusView = TextView(this).apply {
            text = "IDLE"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }

        // --- Fungsi Helper untuk Eksekusi Pipa ---
        fun kirimInstruksi(cmd: String) {
            try {
                val rawRespon = mesinPusatRust(cmd)
                // Parsing sederhana hasil dari Rust (STATE:XX|COLOR:YY|MSG:ZZ)
                val parts = rawRespon.split("|")
                val state = parts[0].replace("STATE:", "")
                val color = parts[1].replace("COLOR:", "")
                val msg = parts[2].replace("MSG:", "")

                statusView.text = state
                statusView.setTextColor(Color.parseColor(color))
                logView.text = "LOG: $msg"
                
            } catch (e: Exception) {
                logView.text = "LOG ERROR: ${e.message}"
            }
        }

        // --- Tombol-Tombol ---
        val btnProses = Button(this).apply { text = "EKSEKUSI DATA" }
        val btnSetting = Button(this).apply { text = "PENGATURAN" }
        val btnHistory = Button(this).apply { text = "RIWAYAT" }

        btnProses.setOnClickListener { kirimInstruksi("PROSES") }
        btnSetting.setOnClickListener { kirimInstruksi("SETTING") }
        btnHistory.setOnClickListener { kirimInstruksi("HISTORY") }

        // Tambah ke Layout
        root.addView(logView)
        root.addView(statusView)
        root.addView(btnProses)
        root.addView(btnSetting)
        root.addView(btnHistory)

        setContentView(root)
    }
}
