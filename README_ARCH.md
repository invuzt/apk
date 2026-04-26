# Dokumentasi Arsitektur Vuzt Ultra-Slim (Rust-Android NDK)

Arsitektur ini dirancang untuk memisahkan antara **Jembatan (Bridge)**, **Logika (Core)**, dan **Tampilan (UI)**. Tujuannya adalah agar file jembatan tidak perlu diubah-ubah lagi meskipun fitur terus bertambah.

## 🏗️ Struktur Folder & File
- `app/src/main/rust/src/lib.rs` -> **Jembatan (Bridge)**: Statis, tidak untuk diubah.
- `app/src/main/rust/src/core.rs` -> **Logika (Core)**: Tempat modifikasi fitur Rust.
- `app/src/main/java/.../MainActivity.java` -> **Tampilan (UI)**: Tempat modifikasi tombol dan input.

---

## 🛠️ Cara Menambah Fitur Tanpa Merubah Jembatan

Jembatan kita hanya menerima satu String: `mesinPusatRust(String input)`. 
Untuk menambah fitur, kita gunakan sistem **Command/Instruksi**.

### 1. Edit di Sisi Java (MainActivity.java)
Jika ingin menambah tombol baru atau input file, Anda cukup mengirim string dengan format tertentu.

**Contoh Tambah Fitur Cek File:**
```java
// Di dalam onClick tombol baru
String pathFile = "/sdcard/download/data.txt";
String hasil = mesinPusatRust("FILE_CHECK:" + pathFile);

