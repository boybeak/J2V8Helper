package com.github.boybeak.j2v8helper

import android.util.Log
import com.eclipsesource.v8.*
import com.github.boybeak.j2v8helper.annotation.V8Field
import com.github.boybeak.j2v8helper.annotation.V8Method
import com.github.boybeak.j2v8helper.ext.bindWith
import java.lang.reflect.Field

object J2V8Helper {
    private const val TAG = "J2V8Helper"
    private const val FUNC_NAME = "com_github_boybeak_j2v8helper_createProxy"
    private const val KEY_IS_UPDATABLE = "j2v8_helper_is_v8updatable"
    private const val KEY_UPDATABLE_STRATEGY = "j2v8_helper_updatable_strategy"
    private const val KEY_UPDATABLE_KEYS = "j2v8_helper_updatable_keys"
    private const val KEY_SETTERS = "j2v8_helper_setters"
    private const val CREATE_PROXY_FUNCTION = """
        function $FUNC_NAME(obj) {
            return new Proxy(obj, {
                set: function (target, key, value) {
                    var oldValue = target[key];
                    if (oldValue == undefined) {
                        oldValue = null;
                    }
                    var newValue = value;
                    if (newValue == undefined) {
                        newValue = null;
                    }
                    target[key] = value;
                    // call the native method update, notify value changed
                    const methodName = target["$KEY_SETTERS"][key];
                    if (methodName) {
                        target[methodName](value);
                    } else if (target["$KEY_IS_UPDATABLE"]) {
                        const updateStrategy = target["$KEY_UPDATABLE_STRATEGY"];
                        const keys = target["$KEY_UPDATABLE_KEYS"];
                        if (updateStrategy == "CARE") {
                            if (keys.includes(key)) {
                                target.onV8Update(target, key, newValue, oldValue);
                            }
                        } else {
                            if (!keys.includes(key)) {
                                target.onV8Update(target, key, newValue, oldValue);
                            }
                        }
                    } else {
                        throwError("Can not update key(" + key + "), make sure it has a V8Field or the object is a V8Updatable");
                    }
                    return true;
                }
            })
        }
    """

    fun help(v8: V8) {
        if (isHelping(v8)) {
            return
        }
        if (BuildConfig.DEBUG) {
            v8.add("Console", V8Object(v8).apply {
                this.bindWith(Console())
            })
        }
        v8.registerJavaMethod(this, "throwError", "throwError", arrayOf(String::class.java))
        v8.executeScript(CREATE_PROXY_FUNCTION)
    }

    fun throwError(message: String) {
        throw IllegalStateException(message)
    }

    fun isHelping(v8: V8): Boolean {
        return !v8.getObject(FUNC_NAME).isUndefined
    }

    fun createProxy(v8: V8, obj: Any): V8Object {
        if (!isHelping(v8)) {
            throw IllegalStateException("Execute J2V8Helper.help(v8) before executing createProxy")
        }
        val v8obj = V8Object(v8)
        val updatableInfoList = registerV8Fields(v8obj, obj)
        if (updatableInfoList.isNotEmpty()) {
            val settersObj = V8Object(v8)
            updatableInfoList.forEach {
                val guessSetterMethodName = "set${it.name.replaceFirstChar { c ->
                    if (c in 'a'..'z') {
                        c - 32
                    } else {
                        c
                    }
                }}"
                try {
                    val method = obj::class.java.getMethod(guessSetterMethodName, it.type)
                    settersObj.add(it.v8fieldName(), method.name)
                    v8obj.registerJavaMethod(obj, method.name, method.name, method.parameterTypes)
                } catch (e: Throwable) {
                    throw NoSuchMethodException("You must assign a setter method for updatable V8Field ${it.name}")
                }
            }
            v8obj.add(KEY_SETTERS, settersObj)
        }
        if (obj is V8Updatable) {
            v8obj.add(KEY_IS_UPDATABLE, true)
            v8obj.add(KEY_UPDATABLE_STRATEGY, obj.getUpdateStrategy().name)
            v8obj.add(KEY_UPDATABLE_KEYS, V8Array(v8).apply {
                obj.getKeys().forEach {
                    push(it)
                }
            })
            v8obj.registerJavaMethod(obj, "onV8Update", "onV8Update",
                arrayOf(V8Object::class.java, String::class.java, Any::class.java, Any::class.java))
        }
        registerV8Methods(v8obj, obj)
        return v8.executeObjectFunction(FUNC_NAME, V8Array(v8).push(v8obj))
    }

    fun registerV8Methods(v8obj: V8Object, otherObj: Any) {
        otherObj::class.java.declaredMethods.forEach {
            if (!it.isAnnotationPresent(V8Method::class.java)) {
                return@forEach
            }
            v8obj.registerJavaMethod(otherObj, it.name, it.name, it.parameterTypes)
        }
    }

    fun registerV8Fields(v8obj: V8Object, otherObj: Any): List<Field> {
        val updatableFields = ArrayList<Field>()
        otherObj::class.java.declaredFields.forEach {
            if (!it.isAnnotationPresent(V8Field::class.java)) {
                return@forEach
            }
            if (!isAcceptableV8Type(it.type)) {
                throw IllegalStateException("Only accept boolean, int, float, double, String and V8Value")
            }
            it.isAccessible = true
            val v8field = it.getAnnotation(V8Field::class.java)!!
            val key = it.v8fieldName()
            val updatable = v8field.updatable
            if (updatable) {
                updatableFields.add(it)
            }
            when(it.type) {
                Int::class.java -> {
                    v8obj.add(key, it.getInt(otherObj))
                }
                Boolean::class.java -> {
                    v8obj.add(key, it.getBoolean(otherObj))
                }
                /*Float::class.java -> {
                    v8obj.add(key, it.getFloat(otherObj).toDouble())
                }*/
                Double::class.java -> {
                    v8obj.add(key, it.getDouble(otherObj))
                }
                String::class.java -> {
                    v8obj.add(key, it.get(otherObj) as? String)
                }
                else -> {
                    v8obj.add(key, it.get(otherObj) as? V8Value)
                }
            }
            it.isAccessible = false
        }
        return updatableFields
    }

    fun isAcceptableV8Type(type: Class<*>): Boolean {
        return Int::class.java == type || Boolean::class.java == type || Double::class.java == type
                || String::class.java == type || V8Value::class.java.isAssignableFrom(type)
    }

    private fun Field.v8fieldName(): String {
        val v8field = getAnnotation(V8Field::class.java)!!
        return if (v8field.alias == "") name else v8field.alias
    }

    private class Console {
        companion object {
            private const val TAG = "Console"
        }
        @V8Method
        fun log(vararg args: Any) {
            Log.v(TAG, args.joinToString())
        }
        @V8Method
        fun error(vararg args: Any) {
            Log.e(TAG, args.joinToString())
        }
    }

}