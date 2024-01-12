package top.niunaijun.blackboxa.hook

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

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
}