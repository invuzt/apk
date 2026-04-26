// Fungsi logika murni, terpisah dari urusan JNI
#[inline(always)]
pub fn engine(input: &str) -> String {
    if input.is_empty() {
        return "Sistem: Input Kosong".to_string();
    }

    // CONTOH LOGIKA: Sederhana & Cepat
    match input.trim() {
        "ping" => "pong!".to_string(),
        "info" => "Vuzt Engine v3.0 [Plain UI Edition]".to_string(),
        _ => format!("Rust Memproses: {}", input)
    }
}
