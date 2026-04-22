use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_androidautobuildapk_MainActivity_mesinPusatRust<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    perintah: JString<'local>,
) -> jstring {
    // Memperbaiki error trait Default dengan menggunakan match manual
    let input: String = match env.get_string(&perintah) {
        Ok(java_str) => java_str.into(),
        Err(_) => "IDLE".to_string(),
    };

    // Logika Instruksi Pusat (Sesuai skenario 3 tombol)
    let respon = match input.as_str() {
        "PROSES" => "STATE:SUCCESS|COLOR:#00FF00|MSG:Data Berhasil Diolah!",
        "SETTING" => "STATE:CONFIG|COLOR:#FFA500|MSG:Mode Performa Tinggi Aktif",
        "HISTORY" => "STATE:LOG|COLOR:#808080|MSG:Terakhir diakses: 23 April 2026",
        _ => "STATE:IDLE|COLOR:#FFFFFF|MSG:Siap Menerima Perintah",
    };

    // Buat jstring baru untuk dikirim balik ke Kotlin
    let output = env.new_string(respon).expect("Gagal buat instruksi");
    output.into_raw()
}
