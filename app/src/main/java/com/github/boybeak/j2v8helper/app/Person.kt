package com.github.boybeak.j2v8helper.app

import android.util.Log
import com.eclipsesource.v8.V8Object
import com.github.boybeak.j2v8helper.V8Updatable
import com.github.boybeak.j2v8helper.annotation.V8Field

class Person : V8Updatable {
    companion object {
        private const val TAG = "Person"
    }
    @V8Field(alias = "age", updatable = true)
    var age: Int = 0
        set(value) {
            field = value
            Log.d(TAG, "setAge to $value thread=${Thread.currentThread().name}")
        }

    override fun getKeys(): Array<String> {
        return arrayOf("phone")
    }

    override fun onV8Update(v8obj: V8Object, key: String, newValue: Any?, oldValue: Any?) {
        Log.d(TAG, "onV8Update(key=$key, value=$newValue)")
    }
}