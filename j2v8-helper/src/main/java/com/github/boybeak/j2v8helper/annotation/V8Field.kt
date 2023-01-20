package com.github.boybeak.j2v8helper.annotation

/**
 *
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class V8Field(val alias: String = "",
                         val updatable: Boolean = false)
