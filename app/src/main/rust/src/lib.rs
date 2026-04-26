use robusta_jni::bridge;

#[bridge]
mod jni {
    use robusta_jni::convert::IntoJava;

    #[package(com.example.androidautobuildapk)]
    pub struct MainActivity;

    impl MainActivity {
        pub fn mesinPusatRust(input: String) -> String {
            // Logika Pemisahan: CHAT vs AGENT (Safe & Automated)
            if input.contains("CHAT:") {
                let msg = input.replace("CHAT:", "");
                format!("STATE:AI_CHAT|COLOR:#00D1FF|MSG:Membalas pesan: {}", msg)
            } else if input.contains("AGENT:") {
                let task = input.replace("AGENT:", "");
                let action = if task.contains("OPTIMASI") {
                    "Membersihkan cache dan membatasi background proses."
                } else {
                    "Menganalisis perintah sistem dan menunggu eksekusi."
                };
                format!("STATE:AI_AGENT|COLOR:#FF3D00|MSG:Tindakan: {}", action)
            } else {
                format!("STATE:IDLE|COLOR:#FFFFFF|MSG:Menunggu input...")
            }
        }
    }
}
