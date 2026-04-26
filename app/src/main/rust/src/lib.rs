mod core; // Menghubungkan ke file core.rs

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_androidautobuildapk_MainActivity_mesinPusatRust(
    mut env: JNIEnv,
    _: JClass,
    input: JString,
) -> jstring {
    // Ambil string dari Java secara efisien
    let input_str: String = env.get_string(&input)
        .map(|s| s.into())
        .unwrap_or_else(|_| String::new());

    // Panggil logika dari file core.rs
    let hasil = core::engine(&input_str);

    // Kirim balik ke Java
    env.new_string(hasil)
        .map(|js| js.into_raw())
        .unwrap_or(std::ptr::null_mut())
}
