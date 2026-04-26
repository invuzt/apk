use robusta_jni::bridge;

#[bridge]
mod jni {
    #[package(com.example.androidautobuildapk)]
    pub struct MainActivity;

    impl MainActivity {
        pub fn mesinPusatRust(input: String) -> String {
            // Proses di Rust: Kita balikkan text-nya atau tambahkan prefix
            let reversed: String = input.chars().rev().collect();
            format!("Rust Output: {}", reversed)
        }
    }
}
