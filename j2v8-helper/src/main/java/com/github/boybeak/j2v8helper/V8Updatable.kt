package com.github.boybeak.j2v8helper

interface V8Updatable {

    fun getUpdateStrategy(): Strategy = Strategy.IGNORE
    fun getKeys(): Array<String> = emptyArray()
    fun onV8Update(key: String, value: Any)

    enum class Strategy {
        CARE, IGNORE
    }

}