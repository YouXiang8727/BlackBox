package top.niunaijun.blackboxa.hook

import android.app.Activity
import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WejoHookModule: BaseHookModule {
    override val packageName: String
        get() = "com.wejo.appv2"
    override var mApplicationContext: Context? = null
    override var mClassLoader: ClassLoader? = null
    override var isInit = false

    override fun initHook() {
        hookImagePrivate() //顯示相簿照片
        hookFeed() //顯示動態照片
        hookMember()
        hookJoDetailActivity()
    }

    private fun hookImagePrivate() {
        val clazz = XposedHelpers.findClass("com.wejo.ecapp.api.data.UploadImage", mClassLoader)
        XposedBridge.hookAllMethods(clazz, "getImage_private", object: XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                param?.result = 1
            }
        })
    }

    private fun hookFeed() {
        val clazz = XposedHelpers.findClass("com.wejo.ecapp.api.data.Feed", mClassLoader)
        val hookMethods = listOf<Pair<String, Any>>(
            Pair("getFeed_type", 1),
            Pair("getIs_secret", true)
        )
        hookMethods.forEach { method ->
            XposedHelpers.findAndHookMethod(clazz, method.first, object: XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    Log.d("hookFeed", "param method=$method, result=${param?.result}")
                    param?.result = method.second
                }
            })
        }
    }

    private fun hookMember() {
        val clazz = XposedHelpers.findClass("com.wejo.ecapp.api.data.Member", mClassLoader)
        XposedHelpers.findAndHookMethod(clazz, "getMember_type", object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                Log.d("WejoHookModule", "before hookMember getMember_type: ${param?.args?.joinToString(", ")}")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                Log.d("WejoHookModule", "after hookMember getMember_type: ${param?.args?.joinToString(", ")}")
                param?.result = 2
            }
        })

        XposedHelpers.findAndHookConstructor(clazz, object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                Log.d("WejoHookModule", "before hook constructor com.wejo.ecapp.api.data.Member")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                Log.d("WejoHookModule", "after hook constructor com.wejo.ecapp.api.data.Member")
            }
        })

        XposedHelpers.findAndHookMethod(clazz, "getMember_points", object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                Log.d("WejoHookModule", "before hookMember getMember_points: ${param?.args?.joinToString(", ")}")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                Log.d("WejoHookModule", "after hookMember getMember_points: ${param?.args?.joinToString(", ")}")
                param?.result = 99
            }
        })

        XposedHelpers.findAndHookMethod(clazz, "getWejo_points", object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                Log.d("WejoHookModule", "before hookMember getWejo_points: ${param?.args?.joinToString(", ")}")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                Log.d("WejoHookModule", "after hookMember getWejo_points: ${param?.args?.joinToString(", ")}")
                param?.result = 100
            }
        })
    }

    private fun hookJoDetailActivity() {
        val clazz = XposedHelpers.findClass("com.wejo.ecapp.activity.JoDetailActivity", mClassLoader)
        XposedHelpers.findAndHookMethod(clazz, "isRegistered", object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                Log.d("WejoHookModule", "before hook JoDetailActivity isRegistered: ${param?.args?.joinToString(", ")}")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                Log.d("WejoHookModule", "after hook JoDetailActivity isRegistered: ${param?.args?.joinToString(", ")}")
                param?.result = true
            }
        })
    }
}