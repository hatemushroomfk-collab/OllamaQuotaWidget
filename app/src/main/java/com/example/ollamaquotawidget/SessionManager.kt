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
    var previousSessionVal: Long,  // 소수 1자리 보존 (예: "1.7%" → 17L)
    var resetTimeDisplayMode: Int,
    var sessionResetTimestamp: Long,
    var sessionModelsJson: String = "[]",  // [{model, requests, share, color}]
    var weeklyModelsJson: String = "[]",   // [{model, requests, share, color}]
    var resetTimeIso: String = "",          // ISO 8601 timestamp (e.g. "2026-07-18T07:00:00Z")
    // 모델별 표시 토글 (expanded 알림창 / 위젯 / 앱에서 적용)
    var showModelUsage: Boolean = true,           // 모델별 usage %
    var showModelRequests: Boolean = true,         // 모델별 requests 수
    var showModelUsagePerReq: Boolean = true       // 모델별 usage/requests 비율
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
                        previousSessionVal = run {
                            // 마이그레이션: 기존 Int 값(예: 1 = 1%)을 ×10 보정 (→ 10L = 1.0%)
                            // 새 형식은 소수 1자리 보존 (예: 1.7% → 17L)
                            val raw = obj.opt("previousSessionVal")
                            when (raw) {
                                is Number -> {
                                    val v = raw.toLong()
                                    // 기존 값이 ×10 안 된 구버전 감지: 0~100 범위면 ×10 보정
                                    if (v in 0..100) v * 10 else v
                                }
                                else -> -10L  // -1.0% (미설정)
                            }
                        },
                        resetTimeDisplayMode = obj.optInt("resetTimeDisplayMode", 0),
                        sessionResetTimestamp = obj.optLong("sessionResetTimestamp", 0L),
                        sessionModelsJson = obj.optString("sessionModelsJson", "[]"),
                        weeklyModelsJson = obj.optString("weeklyModelsJson", "[]"),
                        resetTimeIso = obj.optString("resetTimeIso", ""),
                        showModelUsage = obj.optBoolean("showModelUsage", true),
                        showModelRequests = obj.optBoolean("showModelRequests", true),
                        showModelUsagePerReq = obj.optBoolean("showModelUsagePerReq", true)
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
            obj.put("sessionModelsJson", acc.sessionModelsJson)
            obj.put("weeklyModelsJson", acc.weeklyModelsJson)
            obj.put("resetTimeIso", acc.resetTimeIso)
            obj.put("showModelUsage", acc.showModelUsage)
            obj.put("showModelRequests", acc.showModelRequests)
            obj.put("showModelUsagePerReq", acc.showModelUsagePerReq)
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

    // ===== 알림 상태 영속화 (서비스 재시작 시 중복 알림 방지) =====
    // hasAlertedRule: { ruleId -> Boolean }
    // hasAlertedLogin: { accId  -> Boolean }
    private const val KEY_ALERTED_RULES = "alerted_rules_json"
    private const val KEY_ALERTED_LOGIN = "alerted_login_json"

    fun getAlertedRules(context: Context): Map<String, Boolean> {
        val jsonStr = getPrefs(context).getString(KEY_ALERTED_RULES, "{}") ?: "{}"
        return try {
            val obj = JSONObject(jsonStr)
            val map = mutableMapOf<String, Boolean>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getBoolean(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveAlertedRules(context: Context, map: Map<String, Boolean>) {
        val obj = JSONObject()
        for ((k, v) in map) obj.put(k, v)
        getPrefs(context).edit().putString(KEY_ALERTED_RULES, obj.toString()).apply()
    }

    fun getAlertedLogin(context: Context): Map<String, Boolean> {
        val jsonStr = getPrefs(context).getString(KEY_ALERTED_LOGIN, "{}") ?: "{}"
        return try {
            val obj = JSONObject(jsonStr)
            val map = mutableMapOf<String, Boolean>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getBoolean(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveAlertedLogin(context: Context, map: Map<String, Boolean>) {
        val obj = JSONObject()
        for ((k, v) in map) obj.put(k, v)
        getPrefs(context).edit().putString(KEY_ALERTED_LOGIN, obj.toString()).apply()
    }
}
