use robusta_jni::bridge;

#[bridge]
mod jni {
    #[package(com.example.androidautobuildapk)]
    pub struct MainActivity;

    impl MainActivity {
        pub fn mesinPusatRust(input: String) -> String {
            // Proses sederhana di Rust
            let hasil_proses = format!("Rust menerima: '{}'. (Proses Berhasil)", input);
            
            // Kembalikan ke Java
            hasil_proses
        }
    }
}
