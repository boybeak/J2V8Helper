# J2V8Helper ![](https://jitpack.io/v/boybeak/J2V8Helper.svg)

As its name, this is a J2V8 helper library. It fixes 2 problems:

- Changing value in js layer, no notify event will send to java layer.

- Registering a lot of java fields and methods cost too much time and codes.

## Installing

**Step 1.** In your root build.gradle, add the JitPack repository.

```groovy
allprojects {
	repositories {
		...
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency

```groovy
dependencies {
    implementation 'com.github.boybeak:J2V8Helper:Tag'
}
```



## Usage

### Step 1. Create V8

Create an v8 object as usual, and then, help it.

```kotlin
val v8 = V8.createV8Runtime()
J2V8Helper.help(v8)
```

After this, the **J2V8Helper** is helping this v8 runtime.

### Step2. Register fields and methods

**Native.kt**

```kotlin
class Native(val v8: V8) {
    @V8Field
    val name = "Native for JS"
    @V8Method
    fun getPerson(): V8Object {
        val person = Person()
        return v8.createProxy(person)
    }
}
```

Mark the fields and methods with `@V8Field` and `@V8Method`. Then, register them.

```kotlin
val native = Native(v8)
val nativeV8 = V8Object(v8);
J2V8Helper.registerV8Fields(nativeV8, native)
J2V8Helper.registerV8Methods(nativeV8, native)
                
v8.add("Native", nativeV8)
```

Alternatively, if you are using Kotlin, you can use an extension method instead of `registerV8Fields` and `registerV8Methods`.

```kotlin
nativeV8.bindWith(native)
```

### Step 3. Make a proxy if you need.

Let's look back to the code snippet  **Native.kt**. It has a method named `getPerson`, in this method, return a V8Object that create by `createProxy`.

Now look deep into the **Persion.kt**.

```kotlin
class Person : V8Updatable {
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
```

The field `age` is marked by `@V8Field`, and has 2 options: **alias** and **updatable**.

- **alias:** The field's name in JS layer;

- **update:** Notify the field change when the value changed in JS layer.

Execute JS code with the v8 runtime.

```javascript
const person = Native.getPerson();
person.age = 15

person.name = "John"
person.phone = "123456"
```

When the person.age value changed, in java layer, the `setAge` method will be called.

The next line is `person.name= "john"`, and the Person has no field called *name*, so how to observe this value change?

The answer is using **V8Updatable**.

The Person class also implements `V8Updatable` interface.

`V8Updatable` has 3 key methods: **onV8Update**， **getKeys** and **getUpdateStrategy**.

- **onV8Update:** Will be called when the value changed;

- **getKeys:** The value change event will be cared or be ignored, it depends on the **getUpdateStrategy** return value, default value is empty array.

- **getUpdateStrategy:** Has 2 enum value—— **CARE** and **IGNORE**, CARE means care about getKeys value changed, IGNORE means ingore getKeys value changed, default value is **IGNORE**.

So, after run the js code. `age`'s set method will be called, and onV8Update method will be called after `person.name = "John"`, and `person.phone = "123456"` will not cause onV8Update be called, because the "phone" field is in the ignore list.
