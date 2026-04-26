pub fn engine(input: &str) -> String {
    match input.trim() {
        "1" => "Sistem: OK".to_string(),
        _ => format!("Rust: {}", input)
    }
}
