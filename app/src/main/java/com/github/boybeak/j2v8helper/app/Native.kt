package com.github.boybeak.j2v8helper.app

import android.util.Log
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.github.boybeak.j2v8helper.annotation.V8Method
import com.github.boybeak.j2v8helper.ext.createProxy

class Native(val v8: V8) {
    companion object {
        private const val TAG = "Native"
    }
    @V8Method
    fun getPerson(): V8Object {
        Log.d(TAG, "getNativeObj")
        val person = Person()
        return v8.createProxy(person)
    }

}