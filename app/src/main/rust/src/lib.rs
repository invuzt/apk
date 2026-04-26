use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_androidautobuildapk_MainActivity_mesinPusatRust(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let input_str: String = env.get_string(&input).map(|s| s.into()).unwrap_or_default();
    
    // Proses balikkan string
    let hasil = format!("Rust OK: {}", input_str.chars().rev().collect::<String>());
    
    env.new_string(hasil).unwrap().into_raw()
}
