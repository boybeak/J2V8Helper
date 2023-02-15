package com.github.boybeak.j2v8helper.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.github.boybeak.j2v8helper.J2V8Helper
import com.github.boybeak.j2v8helper.app.array.V8ArrayBufferNative
import com.github.boybeak.j2v8helper.app.typedarray.TypedArrayNative
import com.github.boybeak.j2v8helper.ext.bindWith

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.hello).setOnClickListener {
            readJS("js/hello.js") {
                val v8 = V8.createV8Runtime()
                J2V8Helper.help(v8)

                val native = Native(v8)
                v8.add("Native", V8Object(v8).apply {
                    bindWith(native)
                })
                v8.executeScript(it)
            }
        }
        findViewById<Button>(R.id.v8ArrayBuffer).setOnClickListener {
            readJS("js/v8-array-buffer.js") {
                val v8 = V8.createV8Runtime()
                J2V8Helper.help(v8)

                val native = V8ArrayBufferNative(v8)
                v8.add("Native", V8Object(v8).apply {
                    bindWith(native)
                })
                v8.executeScript(it)
            }
        }
        findViewById<Button>(R.id.v8TypedArray).setOnClickListener {
            readJS("js/v8-typed-array.js") {
                val v8 = V8.createV8Runtime()
                J2V8Helper.help(v8)

                val native = TypedArrayNative(v8)
                v8.add("Native", V8Object(v8).apply {
                    bindWith(native)
                })
                v8.executeScript(it)
            }
        }
    }

    private fun readJS(jsPath: String, callback: (jsCode: String) -> Unit) {
        callback.invoke(assets.open(jsPath).reader().readText())
    }

}