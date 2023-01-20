package com.github.boybeak.j2v8helper.ext

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.github.boybeak.j2v8helper.J2V8Helper

fun V8.helpMe() {
    J2V8Helper.help(this)
}
fun V8.createProxy(obj: Any): V8Object {
    return J2V8Helper.createProxy(this, obj)
}
fun V8Object.bindWith(obj: Any) {
    J2V8Helper.registerV8Fields(this, obj)
    J2V8Helper.registerV8Methods(this, obj)
}