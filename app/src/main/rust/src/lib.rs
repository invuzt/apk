mod core;
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_androidautobuildapk_MainActivity_mesinPusatRust(
    mut env: JNIEnv,
    _: JClass,
    input: JString,
) -> jstring {
    // Ambil string tanpa alokasi berlebih
    let s: String = env.get_string(&input).map(Into::into).unwrap_or_default();
    
    // Panggil engine modular
    let res = core::engine(&s);

    // Kirim balik
    env.new_string(res).map_or(std::ptr::null_mut(), |js| js.into_raw())
}
