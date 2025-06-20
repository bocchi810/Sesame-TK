package fansirsqi.xposed.sesame.data

import com.fasterxml.jackson.annotation.JsonIgnore
import fansirsqi.xposed.sesame.util.Files
import fansirsqi.xposed.sesame.util.JsonUtil
import fansirsqi.xposed.sesame.util.Log
import fansirsqi.xposed.sesame.util.RandomUtil
import fansirsqi.xposed.sesame.util.StringUtil
import fansirsqi.xposed.sesame.util.TimeUtil
import java.io.File
import java.io.IOException

/**
 * @author Byseven
 * @date 2025/2/21
 * @apiNote
 */
object DataCache {
    private const val TAG: String = "DataCache"
    private const val FILENAME = "dataCache.json"
    private val FILE_PATH = Files.CONFIG_DIR

    @get:JsonIgnore
    private var init = false

    // 通用数据缓存
    val dataMap: MutableMap<String, Any> = mutableMapOf()

    init {
        // 在单例初始化时加载数据
        load()
    }

    fun <T> saveData(key: String, value: T): Boolean {
        if (value == null) {
            Log.error(TAG, "Value for key '$key' cannot be null.")
            return false
        }

        // 如果是 Set<String> 类型 → 转换为去重的 Set
        if (value is Set<*>) {
            val cleanedSet = value.mapNotNull { it as? String }
                .map { it }
                .filter { it.isNotEmpty() }
                .toSet()
            dataMap[key] = cleanedSet
        }
        // 如果是 List<String> 类型 → 去重 + trim
        else if (value is List<*>) {
            val cleanedList = value.mapNotNull { it as? String }
                .map { it }
                .distinct()
                .filter { it.isNotEmpty() }
            dataMap[key] = cleanedList
        }
        // 其他类型直接保存
        else {
            dataMap[key] = value
        }

        return save()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getData(key: String, defaultValue: T? = null): T? {
        Log.runtime(TAG, "get data for key '$key'")
        return dataMap[key] as? T ?: defaultValue
    }

    /**
     * 安全获取 Set<String> 类型的缓存值，自动处理 List
     * @/Set 类型兼容问题
     */
    fun getSet(key: String, defaultValue: Set<String> = emptySet()): MutableSet<String> {
        val value = dataMap[key]
        return when (value) {
            is Set<*> -> value.mapNotNull { it as? String }.toMutableSet()
            is List<*> -> value.mapNotNull { it as? String }.toMutableSet()
            else -> defaultValue.toMutableSet()
        }
    }

    fun getList(key: String, defaultValue: List<String> = emptyList()): List<String> {
        val value = dataMap[key]
        return when (value) {
            is List<*> -> value.mapNotNull { it as? String }
            is Set<*> -> value.mapNotNull { it as? String }
            else -> defaultValue
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        val value = dataMap[key]
        return when (value) {
            is String -> value
            else -> defaultValue
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val value = dataMap[key]
        return when (value) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        val value = dataMap[key]
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        val value = dataMap[key]
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        val value = dataMap[key]
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }
    }


    fun saveList(key: String, value: List<String>): Boolean {
        return saveData(key, value)
    }

    fun saveString(key: String, value: String): Boolean {
        return saveData(key, value)
    }

    fun saveBoolean(key: String, value: Boolean): Boolean {
        return saveData(key, value)
    }

    fun saveInt(key: String, value: Int): Boolean {
        return saveData(key, value)
    }

    fun saveLong(key: String, value: Long): Boolean {
        return saveData(key, value)
    }

    fun saveDouble(key: String, value: Double): Boolean {
        return saveData(key, value)
    }

    fun saveSet(key: String, value: Set<String>): Boolean {
        return saveData(key, value)
    }


    fun removeData(key: String): Boolean {
        if (dataMap.containsKey(key)) {
            dataMap.remove(key)
            return save()
        }
        return false
    }

    fun clearAllData(): Boolean {
        dataMap.clear()
        return save()
    }

    private fun save(): Boolean {
        val targetFile = File(FILE_PATH, FILENAME)
        val tempFile = File(targetFile.parent, "${targetFile.name}.tmp")
        return try {
            // 1. 序列化对象为格式化后的 JSON 字符串
            val json = JsonUtil.formatJson(this) ?: throw IllegalStateException("JSON 序列化失败")
            // 2. 写入临时文件
            tempFile.writeText(json)
            // 3. 原子性替换
            if (tempFile.exists()) {
                targetFile.delete()
                tempFile.renameTo(targetFile)
                true
            } else {
                Log.error(TAG, "临时文件写入失败")
                false
            }
        } catch (e: Exception) {
            Log.error(TAG, "保存缓存数据失败：${e.message}")
            false
        }
    }

    @Synchronized
    fun load(): Boolean {
        if (init) return true
        val oldFile = Files.getTargetFileofDir(Files.MAIN_DIR, FILENAME)
        val targetFile = Files.getTargetFileofDir(FILE_PATH, FILENAME)
        var success = false
        try {
            if (targetFile.exists()) {
                val json = Files.readFromFile(targetFile)
                val mapper = JsonUtil.copyMapper()
                mapper.readerForUpdating(this).readValue<Any>(json)
                val formatted = JsonUtil.formatJson(this)
                if (formatted != null && formatted != json) {
                    Log.runtime(TAG, "format $TAG config")
                    save() // 使用临时文件写入
                }
                if (oldFile.exists()) oldFile.delete()
            } else if (oldFile.exists()) {
                if (Files.copy(oldFile, targetFile)) {
                    val json = Files.readFromFile(targetFile)
                    JsonUtil.copyMapper().readerForUpdating(this).readValue<Any>(json)
                    val formatted = JsonUtil.formatJson(this)
                    if (formatted != null && formatted != json) {
                        Log.runtime(TAG, "format $TAG config")
                        save()
                    }
                    oldFile.delete()
                } else {
                    Log.error(TAG, "copy old config to new config failed")
                    return false
                }
            } else {
                Log.runtime(TAG, "init $TAG config")
                JsonUtil.copyMapper().updateValue(this, DataCache)
                val formatted = JsonUtil.formatJson(this)
                if (formatted != null) {
                    save()
                }
            }
            success = true
        } catch (e: Exception) {
            Log.error(TAG, "加载缓存数据失败：${e.message}")
            // 尝试恢复默认配置
            JsonUtil.copyMapper().updateValue(this, DataCache)
        } finally {
            init = success
        }
        return success
    }

    fun unload() {
        try {
            JsonUtil.copyMapper().updateValue(this, DataCache)
        } catch (e: Exception) {
            Log.error(TAG, "unload ：${e.message}")
        }
    }


}