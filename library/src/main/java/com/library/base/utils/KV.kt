package com.library.base.utils

import com.tencent.mmkv.MMKV


/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/2/25 下午4:23
 */
object KV {
    var mmkv1 = MMKV.defaultMMKV()

    fun removeValue(key: String) {
        mmkv1?.removeValueForKey(key)
    }
    fun saveStr(key: String, value: String) {
        mmkv1?.encode(key, value)
    }

    fun getStr(key: String): String {
        return getStr(key, "")
    }

    fun getStr(key: String, default: String): String {
        val decodeString = mmkv1?.decodeString(key)
        decodeString?.let {
            return it
        }
        return default
    }

    fun saveBoolean(key: String, value: Boolean) {
        mmkv1?.encode(key, value)
    }

    fun getBoolean(key: String): Boolean {
        return getBoolean(key)
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        val decodeString = mmkv1?.decodeBool(key)
        decodeString?.let {
            return it
        }
        return default
    }

    fun saveInt(key: String, value: Int) {
        mmkv1?.encode(key, value)
    }

    fun getInt(key: String): Int {
        return getInt(key)
    }

    fun getInt(key: String, default: Int): Int {
        val decodeString = mmkv1?.decodeInt(key)
        decodeString?.let {
            return it
        }
        return default
    }

}