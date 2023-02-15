package com.github.boybeak.j2v8helper.app.array

import android.util.Log
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8ArrayBuffer
import com.github.boybeak.j2v8helper.annotation.V8Method
import java.nio.ByteBuffer
import java.nio.IntBuffer

class V8ArrayBufferNative(val v8: V8) {

    companion object {
        private const val TAG = "V8ArrayBufferNative"
    }

    @V8Method
    fun getV8ArrayBuffer(): V8ArrayBuffer {
        val byteBuf = ByteBuffer.allocateDirect(4)
        byteBuf.putInt(42)
        return V8ArrayBuffer(v8, byteBuf)
    }

    @V8Method
    fun printV8ArrayBuffer(buffer: V8ArrayBuffer) {
        val bytes = ByteArray(4)
        buffer.get(bytes)
        val value = ByteBuffer.wrap(bytes).getInt(0)
        Log.d(TAG, "printV8ArrayBuffer value=$value")
    }

}