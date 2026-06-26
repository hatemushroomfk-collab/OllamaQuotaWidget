package com.example.ollamaquotawidget

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class AlertRule(
    val id: String,
    var type: String, // "SESSION" or "WEEKLY"
    var threshold: Int,
    var enabled: Boolean
)

data class Account(
    val id: String,
    var name: String,
    var cookie: String,
    var quotaSummary: String,
    var quotaDetails: String,
    var showCollapsed: Boolean,
    var showExpanded: Boolean,
    var alertRulesJson: String, // JSON array of AlertRule
    var sessionResetTime: String,
    var alertOnReset: Boolean,
    var previousSessionVal: Int,
    var resetTimeDisplayMode: Int,
    var sessionResetTimestamp: Long
)

object SessionManager {
    private const val PREFS_NAME = "OllamaPrefs"
    private const val KEY_ACCOUNTS = "accounts_json"
    const val KEY_UPDATE_INTERVAL_MS = "update_interval_ms"
    const val KEY_AUTO_START = "auto_start_on_boot"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAutoStartEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_START, false)
    }

    fun saveAutoStartEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_START, enabled).apply()
    }

    fun saveUpdateInterval(context: Context, intervalMs: Long) {
        getPrefs(context).edit().putLong(KEY_UPDATE_INTERVAL_MS, intervalMs).apply()
    }

    fun getUpdateInterval(context: Context): Long {
        return getPrefs(context).getLong(KEY_UPDATE_INTERVAL_MS, 30L * 60L * 1000L)
    }

    // Dynamic Accounts List
    fun getAccounts(context: Context): List<Account> {
        val jsonStr = getPrefs(context).getString(KEY_ACCOUNTS, "[]") ?: "[]"
        val list = mutableListOf<Account>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                
                // 마이그레이션 호환성 처리 (기존 스위치 데이터를 AlertRule로 변환)
                var rulesJson = obj.optString("alertRulesJson", "")
                if (rulesJson.isEmpty() || rulesJson == "[]") {
                    val legacySession = obj.optBoolean("alertSession", false)
                    val legacyWeekly = obj.optBoolean("alertWeekly", false)
                    val legacySVal = obj.optInt("alertSessionVal", 90)
                    val legacyWVal = obj.optInt("alertWeeklyVal", 90)
                    
                    val migratedRules = JSONArray()
                    if (legacySession) {
                        val r1 = JSONObject()
                        r1.put("id", java.util.UUID.randomUUID().toString())
                        r1.put("type", "SESSION")
                        r1.put("threshold", legacySVal)
                        r1.put("enabled", true)
                        migratedRules.put(r1)
                    }
                    if (legacyWeekly) {
                        val r2 = JSONObject()
                        r2.put("id", java.util.UUID.randomUUID().toString())
                        r2.put("type", "WEEKLY")
                        r2.put("threshold", legacyWVal)
                        r2.put("enabled", true)
                        migratedRules.put(r2)
                    }
                    rulesJson = migratedRules.toString()
                }

                list.add(
                    Account(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        cookie = obj.optString("cookie", ""),
                        quotaSummary = obj.optString("quotaSummary", "데이터 없음"),
                        quotaDetails = obj.optString("quotaDetails", "[]"),
                        showCollapsed = obj.optBoolean("showCollapsed", true),
                        showExpanded = obj.optBoolean("showExpanded", true),
                        alertRulesJson = rulesJson,
                        sessionResetTime = obj.optString("sessionResetTime", ""),
                        alertOnReset = obj.optBoolean("alertOnReset", true),
                        previousSessionVal = obj.optInt("previousSessionVal", -1),
                        resetTimeDisplayMode = obj.optInt("resetTimeDisplayMode", 0),
                        sessionResetTimestamp = obj.optLong("sessionResetTimestamp", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveAccounts(context: Context, accounts: List<Account>) {
        val array = JSONArray()
        for (acc in accounts) {
            val obj = JSONObject()
            obj.put("id", acc.id)
            obj.put("name", acc.name)
            obj.put("cookie", acc.cookie)
            obj.put("quotaSummary", acc.quotaSummary)
            obj.put("quotaDetails", acc.quotaDetails)
            obj.put("showCollapsed", acc.showCollapsed)
            obj.put("showExpanded", acc.showExpanded)
            obj.put("alertRulesJson", acc.alertRulesJson)
            obj.put("sessionResetTime", acc.sessionResetTime)
            obj.put("alertOnReset", acc.alertOnReset)
            obj.put("previousSessionVal", acc.previousSessionVal)
            obj.put("resetTimeDisplayMode", acc.resetTimeDisplayMode)
            obj.put("sessionResetTimestamp", acc.sessionResetTimestamp)
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_ACCOUNTS, array.toString()).apply()
    }

    fun updateAccountCookie(context: Context, id: String, cookie: String) {
        val accounts = getAccounts(context).toMutableList()
        val index = accounts.indexOfFirst { it.id == id }
        if (index != -1) {
            accounts[index].cookie = cookie
            saveAccounts(context, accounts)
        }
    }
}
