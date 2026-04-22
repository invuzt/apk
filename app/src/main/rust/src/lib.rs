#![no_main]
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_androidautobuildapk_MainActivity_mesinPusatRust(
    env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let input_str: String = env.get_string(input).unwrap().into();
    
    let respon = match input_str.as_str() {
        "PROSES" => "STATE:ACTIVE|COLOR:#00FF00|MSG:Rust Engine Processing",
        "SETTING" => "STATE:CONFIG|COLOR:#FFFF00|MSG:Core Tuned",
        "HISTORY" => "STATE:DATA|COLOR:#00FFFF|MSG:Logs Cleared",
        _ => "STATE:UNKNOWN|COLOR:#FF0000|MSG:Unknown Command",
    };

    env.new_string(respon).unwrap().into_raw()
}
