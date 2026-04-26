pub fn proses_data(input: String) -> String {
    if input.is_empty() {
        return "System: Input Kosong".to_string();
    }

    // CONTOH LOGIKA: Anda bisa kembangkan ini sesuka hati
    match input.as_str() {
        "ping" => "pong!".to_string(),
        "info" => "Vuzt Engine v2.0 - Rust Powered".to_string(),
        _ => format!("Rust Memproses: {}", input.to_uppercase())
    }
}
