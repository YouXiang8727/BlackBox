package top.niunaijun.blackboxa.hook

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

const val END_LINE = "================================================================"
interface BaseHookModule {
    val packageName: String
    var mApplicationContext: Context?
    var mClassLoader: ClassLoader?
    var isInit: Boolean

    fun init(applicationContext: Context, classLoader: ClassLoader) {
        if (isInit) return
        mApplicationContext = applicationContext
        mClassLoader = classLoader
        isInit = true

        CoroutineScope(Dispatchers.IO).launch {
            hookActivityAndFragment()
            initHook()
        }
    }

    fun initHook()
    fun hookActivityAndFragment() {
        // 要 hook 的方法的名稱
        val methodName = "onCreate"
        val tag = "hookActivityAndFragmentOnCreate"

        // 要 hook 的方法所在的類
        val activityClass = XposedHelpers.findClass("android.app.Activity", mClassLoader)
        val fragmentClass = XposedHelpers.findClass("androidx.fragment.app.Fragment", mClassLoader)

        // hook 所有 Activity 和 Fragment 的 onCreate 方法
        listOf(activityClass, fragmentClass).forEach { clazz ->
            try {
                XposedHelpers.findAndHookMethod(clazz, methodName, android.os.Bundle::class.java, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        val thisObject = param?.thisObject
                        val name = thisObject?.javaClass?.name
                        // 在 onCreate 方法之後執行的代碼
                        Log.d(tag, "$name onCreate")
                    }
                })
            }catch (e: Exception) {
                Log.e(tag, "$e",)
            }
        }
    }
    fun getLogXCHook(tag: String) = object: XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
            super.beforeHookedMethod(param)
            Log.d(tag, "beforeHookedMethod")
            param?.args?.forEachIndexed { index, any ->
                Log.d(tag, "args[$index]: $any")
            }
            Log.d(tag, END_LINE)
        }

        override fun afterHookedMethod(param: MethodHookParam?) {
            super.afterHookedMethod(param)
            Log.d(tag, "afterHookedMethod")
            Log.d(tag, "result: ${param?.result}")
            param?.thisObject?.javaClass?.declaredFields?.forEach { field ->
                field.isAccessible = true
                Log.d(tag, "${field.name}: ${field.get(param.thisObject)}")
            }
            Log.d(tag, END_LINE)
        }
    }

    fun setValueUsingReflection(mutableLiveData: Any, value: Any?) {
        try {
            val postValueMethod =
                mutableLiveData::class.declaredMemberFunctions.find { it.name == "postValue" }

            postValueMethod?.isAccessible = true

            postValueMethod?.call(mutableLiveData, value)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Reflection", "setValueUsingReflection e:$e")
        }
    }

    fun getValueUsingReflection(liveData: Any?): Any? {
        liveData?.let {
            try {
                val targetClass = liveData::class.java
                val method = targetClass.getMethod("getValue")

                return method.invoke(liveData)
            } catch (e: Exception) {
                Log.e("Reflection", "getValueUsingReflection e:$e")
            }
        }
        return null
    }
}