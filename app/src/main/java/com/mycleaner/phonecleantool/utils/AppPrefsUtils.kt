package com.mycleaner.phonecleantool.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantooll.base.BaseConstant
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/*
    SP工具类
 */
object AppPrefsUtils {
    private var sp: SharedPreferences =
        BaseApplication.instance!!.getSharedPreferences(BaseConstant.TABLE_PRIVATE, Context.MODE_PRIVATE)
    private var ed: Editor = sp.edit()

    /*
        Boolean数据
     */
    fun putBoolean(key: String, value: Boolean) {
        ed.putBoolean(key, value)
        ed.commit()
    }

    /*
        默认 false
     */
    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    /*
        String数据
     */
    fun putString(key: String, value: String) {
        ed.putString(key, value)
        ed.commit()
    }
    /*
        默认 ""
     */
    fun getString(key: String): String {
        return sp.getString(key, "") + ""
    }

    /*
        Long数据
     */
    fun putLong(key: String, value: Long) {
        ed.putLong(key, value)
        ed.commit()
    }

    /*
        默认 0
     */
    fun getLong(key: String): Long {
        return sp.getLong(key, 0L)
    }


    /*
    Long数据
 */
    fun putDouble(key: String, value: Double) {
        ed.putString(key, value.toString())
        ed.commit()
    }

    /*
        默认 0
     */


    /*
        Set数据
     */
    fun putStringSet(key: String, set: Set<String>) {
        val localSet = getStringSet(key)!!.toMutableSet()
        localSet.addAll(set)
        ed.putStringSet(key, localSet)
        ed.commit()
    }

    /*
        默认空set
     */
    private fun getStringSet(key: String): Set<String>? {
        val set = setOf<String>()
        return sp.getStringSet(key, set)
    }

    /*
        删除key数据
     */
    fun remove(key: String) {
        ed.remove(key)
        ed.commit()
    }


    fun commitString(key: String?, value: String?) {
        ed.putString(key, value)
        ed.commit()
    }

    fun getString(key: String?, failValue: String?): String? {
        return sp.getString(key, failValue)
    }

    fun commitInt(key: String?, value: Int) {
        ed.putInt(key, value)
        ed.commit()
    }

    fun getInt(key: String?, failValue: Int): Int {
        return sp.getInt(key, failValue)
    }

    fun commitLong(key: String?, value: Long) {
        ed.putLong(key, value)
        ed.commit()
    }

    fun getLong(key: String?, failValue: Long): Long {
        return sp.getLong(key, failValue)
    }

    fun commitBoolean(key: String?, value: Boolean) {
        ed.putBoolean(key, value)
        ed.commit()
    }

    fun getBoolean(key: String?, failValue: Boolean): Boolean {
        return sp.getBoolean(key, failValue)
    }

    fun commitDouble(key: String?, value: Double) {
        ed.putString(key, value.toString() + "")
        ed.commit()
    }

    fun getDouble(key: String?, failValue: Double): Double {
        val strValue: String = sp.getString(key, "")!!
        if (strValue.isEmpty()) return failValue
        return strValue.toDouble()
    }

    fun commitFloat(key: String?, value: Float) {
        ed.putFloat(key, value)
        ed.commit()
    }

    fun getFloat(key: String?, failValue: Float): Float {
        return sp.getFloat(key, failValue)
    }


    class PreferenceDelegate<T>(
        private val key: String,
        private val defaultValue: T
    ) : ReadWriteProperty<Any?, T> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return when (defaultValue) {
                is Int -> AppPrefsUtils.getInt(key, defaultValue) as T
                is Long -> AppPrefsUtils.getLong(key, defaultValue) as T
                is Boolean -> AppPrefsUtils.getBoolean(key, defaultValue) as T
                is Float -> AppPrefsUtils.getFloat(key, defaultValue) as T
                is String -> AppPrefsUtils.getString(key, defaultValue) as T
                is Double -> AppPrefsUtils.getDouble(key, defaultValue) as T
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            when (value) {
                is Int -> AppPrefsUtils.commitInt(key, value)
                is Long -> AppPrefsUtils.commitLong(key, value)
                is Boolean -> AppPrefsUtils.commitBoolean(key, value)
                is Float -> AppPrefsUtils.commitFloat(key, value)
                is String -> AppPrefsUtils.commitString(key, value)
                is Double -> AppPrefsUtils.commitDouble(key, value)
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }
    }



}
