package com.github.boybeak.j2v8helper.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.github.boybeak.j2v8helper.J2V8Helper
import com.github.boybeak.j2v8helper.ext.bindWith

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.go).setOnClickListener {
            val v8 = V8.createV8Runtime()
            J2V8Helper.help(v8)

            val native = Native(v8)
            v8.add("Native", V8Object(v8).apply {
                bindWith(native)
            })
            v8.executeScript(readJS())
        }
    }

    private fun readJS(): String {
        return assets.open("js/hello.js").reader().readText()
    }

}