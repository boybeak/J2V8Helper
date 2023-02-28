package com.github.boybeak.j2v8helper

import com.eclipsesource.v8.V8Object

interface V8Updatable {

    fun getUpdateStrategy(): Strategy = Strategy.IGNORE
    fun getKeys(): Array<String> = emptyArray()

    /**
     * @param v8obj The original js object.
     * @param key The key that updated
     * @param newValue new value, may be null
     * @param oldValue old value, may be null
     * Do not update v8obj's fields in this method.
     */
    fun onV8Update(v8obj: V8Object, key: String, newValue: Any?, oldValue: Any?)

    enum class Strategy {
        CARE, IGNORE
    }

}