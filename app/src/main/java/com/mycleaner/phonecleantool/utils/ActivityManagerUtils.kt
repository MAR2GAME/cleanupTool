package com.mycleaner.phonecleantool.utils


import android.app.Activity
import android.content.Context
import com.mycleaner.phonecleantool.activity.MainActivity


import java.util.*
import kotlin.jvm.java
import kotlin.system.exitProcess

object ActivityManagerUtils {

    /**
     * 用于存储和统一销毁一套操作的Activities
     */
    public var activities: Stack<Activity>  = Stack()

    /**
     * 将该activity入栈
     *
     * @param activity
     */
    public fun addActivity(activity: Activity){
        activities.add(activity)
    }

    /*
      获取当前栈顶
   */
    fun currentActivity(): Activity {
        return activities.lastElement()
    }

    /**
     * 将该activity出栈
     *
     * @param activity
     */
    fun removeActivity(activity: Activity?) {
        if (activity != null) {
            activities.remove(activity)
            activity.finish()
        }
    }


    /**
     * 结束指定类名的Activity
     */
    @Synchronized
    fun finishActivityclass(cls: Class<*>) {
            for (activity in activities) {
                if (activity.javaClass == cls) {
                    removeActivity(activity)
                    break
                }

        }
    }

    /**
     * 结束所有activity
     */
    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
       // Process.killProcess(Process.myPid())
    }



    fun finishAllExceptMainActivity() {
        for (activity in activities) {
            if (!activity.isFinishing&&!activity.javaClass.equals(MainActivity::class.java)) {
                activity.finish()
            }
        }
        activities.clear()
        // Process.killProcess(Process.myPid())
    }





    /**
     * 退出程序并杀死任务栈
     * @param context
     */
    fun exitApp(context: Context?) {
        // 结束进程
        exitProcess(0)
    }



}