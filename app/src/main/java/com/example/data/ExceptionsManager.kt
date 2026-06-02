package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.engine.BrowserDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class ExceptionsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("browserlimit_exceptions", Context.MODE_PRIVATE)
    private val EXCEPTIONS_KEY = "exceptions_list"
    
    private val _exceptionsFlow = MutableStateFlow<List<String>>(loadExceptions())
    val exceptionsFlow = _exceptionsFlow.asStateFlow()

    init {
        // Prepopulate with hardcoded permanent exception
        addException("com.aistudio.browserlimit.abxyz")
        addException("com.example")
    }

    private fun loadExceptions(): List<String> {
        val json = prefs.getString(EXCEPTIONS_KEY, "[]")
        val array = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    fun isExcepted(packageName: String): Boolean {
        if (packageName == "com.aistudio.browserlimit.abxyz" || packageName == "com.example") return true
        return loadExceptions().contains(packageName)
    }

    fun addException(packageName: String) {
        val current = loadExceptions().toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            saveExceptions(current)
        }
    }

    fun removeException(packageName: String) {
        if (packageName == "com.aistudio.browserlimit.abxyz" || packageName == "com.example") return
        val current = loadExceptions().toMutableList()
        current.remove(packageName)
        saveExceptions(current)
    }
    
    fun setCustomExceptions(packages: List<String>) {
        val current = loadExceptions().toMutableList()
        current.addAll(packages)
        saveExceptions(current.distinct())
    }

    private fun saveExceptions(list: List<String>) {
        val array = JSONArray()
        list.forEach { array.put(it) }
        prefs.edit().putString(EXCEPTIONS_KEY, array.toString()).apply()
        _exceptionsFlow.value = list
    }
}
