package top.niunaijun.blackboxa.hook

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
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
//        hookChatShowMessage() //顯示聊天列表預覽訊息
//        hookChatAdapter() //顯示聊天室訊息
        hookImagePrivate() //顯示相簿照片
        hookFeed() //顯示動態照片
        hookMemberType() //取得MemberType 設定為VIP
    }

    private fun hookChatShowMessage() {
        val clazz = XposedHelpers.findClass("com.wejo.ecapp.activity.ChatActivity", mClassLoader)
        XposedBridge.hookAllMethods(clazz, "isShowMessage", object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                param?.result = true
            }
        })
    }

    private fun hookChatAdapter() {
        XposedHelpers.findAndHookConstructor("com.wejo.ecapp.adapter.ChatAdapter",
            mClassLoader,
            Context::class.java,
            String::class.java,
            Boolean::class.java,
            Boolean::class.java,
            object: XC_MethodHook(){
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                param?.args?.set(2, false)
                param?.args?.set(3, true)
            }
        })
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

    private fun hookMemberType() {
        val memberTypeEnumClass = XposedHelpers.findClass("com.wejo.ecapp.utils.AppInfoUtil.MemberType", mClassLoader)
        val enums = memberTypeEnumClass.enumConstants!!
        enums.forEachIndexed { index, any ->
            Log.d("hookMemberType", "enums$index value=${(any as Enum<*>).name}")
        }
        val clazz = XposedHelpers.findClass("com.wejo.ecapp.utils.AppInfoUtil", mClassLoader)
        XposedBridge.hookAllMethods(clazz, "getMemberType", object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                Log.d("hookMemberType", "beforeHookedMethod param=$param")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
                Log.d("hookMemberType", "afterHookedMethod param=$param")
                param?.result = enums[2] as Enum<*>
            }
        })
    }
}