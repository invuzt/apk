#![no_main]
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_androidautobuildapk_MainActivity_mesinPusatRust(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let input_str: String = match env.get_string(&input) {
        Ok(s) => s.into(),
        Err(_) => return env.new_string("ERR").unwrap().into_raw(),
    };

    // Logika Pemisahan: CHAT vs AGENT
    let respon = if input_str.contains("CHAT:") {
        let msg = input_str.replace("CHAT:", "");
        format!("STATE:AI_CHAT|COLOR:#00D1FF|MSG:Membalas pesan: {}", msg)
    } else if input_str.contains("AGENT:") {
        let task = input_str.replace("AGENT:", "");
        // Simulasi Logika Agentic (Reasoning)
        let action = if task.contains("OPTIMASI") {
            "Membersihkan cache dan membatasi background proses."
        } else {
            "Menganalisis perintah sistem dan menunggu eksekusi."
        };
        format!("STATE:AI_AGENT|COLOR:#FF3D00|MSG:Tindakan: {}", action)
    } else {
        format!("STATE:IDLE|COLOR:#FFFFFF|MSG:Menunggu input...")
    };

    env.new_string(respon).unwrap().into_raw()
}
