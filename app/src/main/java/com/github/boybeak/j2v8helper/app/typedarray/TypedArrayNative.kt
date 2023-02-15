package com.github.boybeak.j2v8helper.app.typedarray

import android.util.Log
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8TypedArray
import com.github.boybeak.j2v8helper.annotation.V8Method

class TypedArrayNative(val v8: V8) {

    companion object {
        private const val TAG = "TypedArrayNative"
    }

    @V8Method
    fun print(data: V8TypedArray) {
        Log.d(TAG, "print data.size=${data.length()} type=${data.type} limit=${data.buffer.limit()}")
    }

}