mod core;
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_a_b_MainActivity_mesinPusatRust(
    mut env: JNIEnv,
    _: JClass,
    input: JString,
) -> jstring {
    let s: String = env.get_string(&input).map(Into::into).unwrap_or_default();
    let res = core::engine(&s);
    env.new_string(res).map_or(std::ptr::null_mut(), |js| js.into_raw())
}
