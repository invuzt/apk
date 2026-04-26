// Gunakan inline agar compiler langsung menempelkan kode ini ke jembatan (hemat size)
#[inline(always)]
pub fn engine(input: &str) -> String {
    match input {
        "1" => "Vuzt: Active".to_string(),
        "2" => "Battery: OK".to_string(),
        _ => format!("Res: {}", input.to_lowercase())
    }
}
