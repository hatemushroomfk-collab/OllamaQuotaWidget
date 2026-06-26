package com.example.ollamaquotawidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONArray
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuotaForegroundService : Service() {

    private val CHANNEL_ID = "QuotaServiceChannel"
    private val ALERT_CHANNEL_ID = "OllamaAlertChannel"
    private val NOTIFICATION_ID = 1
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var updateJob: Job? = null

    // 1회성 알림 방지 상태 맵 (Key: Rule ID)
    private val hasAlertedRule = mutableMapOf<String, Boolean>()
    private val hasAlertedLogin = mutableMapOf<String, Boolean>()

    companion object {
        const val ACTION_FORCE_REFRESH = "ACTION_FORCE_REFRESH"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_FORCE_REFRESH) {
            scope.launch {
                updateQuota()
            }
        } else {
            val emptyList = emptyList<Account>()
            val loadingNotif = buildNotification(emptyList)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, loadingNotif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, loadingNotif)
            }
            startPeriodicUpdates()
        }
        return START_STICKY
    }

    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = scope.launch {
            while (isActive) {
                updateQuota()
                val intervalMs = SessionManager.getUpdateInterval(this@QuotaForegroundService)
                delay(if (intervalMs < 1000) 1000 else intervalMs)
            }
        }
    }

    private suspend fun updateQuota() {
        val accounts = SessionManager.getAccounts(this).toMutableList()
        var updated = false

        for (acc in accounts) {
            if (acc.cookie.isNotEmpty()) {
                val qData = fetchQuota(acc.cookie, acc.id, acc.name)
                acc.quotaSummary = qData.summary
                acc.quotaDetails = qData.detailsJson
                acc.sessionResetTime = qData.resetTime
                acc.sessionResetTimestamp = calculateResetTimestamp(qData.resetTime)
                updated = true
                
                val sInt = parsePercent(qData.sessionVal)
                val wInt = parsePercent(qData.weeklyVal)
                checkAlerts(acc, sInt, wInt)

                if (acc.previousSessionVal >= 0 && sInt < acc.previousSessionVal) {
                    if (acc.alertOnReset) {
                        sendResetAlert(acc.name)
                    }
                }
                acc.previousSessionVal = sInt
            }
        }

        if (updated) {
            SessionManager.saveAccounts(this, accounts)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(accounts))

        withContext(Dispatchers.Main) {
            QuotaWidgetProvider.updateAllWidgets(this@QuotaForegroundService)
        }
    }
    
    private fun parsePercent(str: String): Int {
        return try { str.replace("%", "").toFloat().toInt() } catch (e: Exception) { 0 }
    }

    private fun parseVal(quota: String, prefix: String): String {
        try {
            val split = quota.split("|")
            for (part in split) {
                if (part.trim().startsWith(prefix)) {
                    return part.substringAfter(":").trim()
                }
            }
        } catch (e: Exception) {}
        return "0%"
    }

    private fun buildNotification(accounts: List<Account>): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val refreshIntent = Intent(this, QuotaForegroundService::class.java).apply { action = ACTION_FORCE_REFRESH }
        val refreshPendingIntent = PendingIntent.getService(
            this, 1, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        // --- 1. Collapsed View ---
        val collapsedAccounts = accounts.filter { it.showCollapsed }.take(2)
        val collapsedText = if (collapsedAccounts.isEmpty()) {
            "설정된 계정 없음"
        } else {
            collapsedAccounts.joinToString(" | ") { "${it.name}: ${it.quotaSummary}" }
        }
        
        val collapsedViews = RemoteViews(packageName, R.layout.notification_custom_collapsed)
        collapsedViews.setTextViewText(R.id.tvCollapsedText, collapsedText)
        collapsedViews.setTextViewText(R.id.tvUpdateTimeCollapsed, timeString)
        collapsedViews.setOnClickPendingIntent(R.id.llCollapsedRoot, refreshPendingIntent)

        // --- 2. Expanded (Graph) View ---
        val expandedViews = RemoteViews(packageName, R.layout.notification_custom_graph)
        expandedViews.setTextViewText(R.id.tvUpdateTime, timeString)
        expandedViews.setOnClickPendingIntent(R.id.llGraphRoot, refreshPendingIntent)
        expandedViews.setOnClickPendingIntent(R.id.btnOpenApp, pendingIntent)
        
        expandedViews.removeAllViews(R.id.llAccountsContainer)
        
        val expandedAccounts = accounts.filter { it.showExpanded }
        for (acc in expandedAccounts) {
            val sVal = parseVal(acc.quotaSummary, "S")
            val wVal = parseVal(acc.quotaSummary, "W")
            val sInt = parsePercent(sVal)
            val wInt = parsePercent(wVal)
            
            val itemView = RemoteViews(packageName, R.layout.notification_account_item)
            
            val displayStr = if (acc.resetTimeDisplayMode == 1 && acc.sessionResetTimestamp > 0) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(acc.sessionResetTimestamp))
            } else {
                acc.sessionResetTime
            }
            
            val nameLabel = if (displayStr.isNotEmpty()) "${acc.name} (↻ $displayStr)" else acc.name
            itemView.setTextViewText(R.id.tvAccName, nameLabel)
            itemView.setTextViewText(R.id.tvAccSessionLabel, "S")
            
            itemView.setTextViewText(R.id.tvAccSessionVal, sVal)
            itemView.setProgressBar(R.id.pbAccSession, 100, sInt, false)
            itemView.setTextViewText(R.id.tvAccWeeklyVal, wVal)
            itemView.setProgressBar(R.id.pbAccWeekly, 100, wInt, false)
            
            expandedViews.addView(R.id.llAccountsContainer, itemView)
        }

        builder.setContentTitle("Ollama Quota")
        builder.setContentText(collapsedText)
        builder.setCustomContentView(collapsedViews)
        builder.setCustomBigContentView(expandedViews)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Ollama Quota", NotificationManager.IMPORTANCE_LOW)
            val alertChannel = NotificationChannel(ALERT_CHANNEL_ID, "Ollama Alerts", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            manager?.createNotificationChannel(alertChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    
    data class ScrapeResult(val summary: String, val sessionVal: String, val weeklyVal: String, val detailsJson: String, val resetTime: String)

    private fun calculateResetTimestamp(resetString: String): Long {
        var ms = 0L
        val parts = resetString.trim().split(" ")
        if (parts.size >= 2) {
            val num = parts[0].toLongOrNull() ?: 0L
            val unit = parts[1].lowercase(Locale.getDefault())
            if (unit.startsWith("hour")) ms = num * 3600 * 1000
            else if (unit.startsWith("minute")) ms = num * 60 * 1000
            else if (unit.startsWith("day")) ms = num * 24 * 3600 * 1000
        }
        return if (ms > 0) System.currentTimeMillis() + ms else 0L
    }

    private suspend fun fetchQuota(cookieString: String, accId: String, accName: String): ScrapeResult = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect("https://ollama.com/settings")
                .header("Cookie", cookieString)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get()

            val bodyText = document.body().text()
            
            if (bodyText.contains("Sign In", ignoreCase = true) || document.title().contains("Sign In")) {
                sendLoginAlert(accId, accName)
                return@withContext ScrapeResult("로그인 필요", "0%", "0%", "[]", "")
            } else {
                hasAlertedLogin[accId] = false
            }
            
            val sessionRegex = Regex("Session usage\\s*([0-9.]+%)\\s*used", RegexOption.IGNORE_CASE)
            val sessionVal = sessionRegex.find(bodyText)?.groups?.get(1)?.value ?: "0%"

            val weeklyRegex = Regex("Weekly usage\\s*([0-9.]+%)\\s*used", RegexOption.IGNORE_CASE)
            val weeklyVal = weeklyRegex.find(bodyText)?.groups?.get(1)?.value ?: "0%"
            
            val sessionResetRegex = Regex("Session usage.*?Resets in\\s*([0-9]+\\s*[a-zA-Z]+)", RegexOption.IGNORE_CASE)
            val sessionResetVal = sessionResetRegex.find(bodyText)?.groups?.get(1)?.value ?: ""
            
            val summary = if (sessionVal == "0%" && weeklyVal == "0%") "데이터 없음" else "S: $sessionVal | W: $weeklyVal"
            val jsonArray = JSONArray()

            ScrapeResult(summary, sessionVal, weeklyVal, jsonArray.toString(), sessionResetVal)
        } catch (e: Exception) {
            if (e.message?.contains("401") == true || e.message?.contains("403") == true) {
                sendLoginAlert(accId, accName)
            }
            ScrapeResult("네트워크 에러", "0%", "0%", "[]", "")
        }
    }

    private fun checkAlerts(acc: Account, sInt: Int, wInt: Int) {
        val rulesArray = try { JSONArray(acc.alertRulesJson) } catch (e: Exception) { JSONArray() }
        for (i in 0 until rulesArray.length()) {
            val rule = rulesArray.getJSONObject(i)
            if (!rule.optBoolean("enabled", true)) continue
            
            val ruleId = rule.optString("id", "")
            if (ruleId.isEmpty()) continue
            
            val type = rule.optString("type", "SESSION")
            val threshold = rule.optInt("threshold", 90)
            
            val currentValue = if (type == "WEEKLY") wInt else sInt
            val typeStr = if (type == "WEEKLY") "주간" else "세션"
            
            if (currentValue >= threshold) {
                if (hasAlertedRule[ruleId] != true) {
                    sendQuotaAlert(acc.name, typeStr, currentValue, threshold)
                    hasAlertedRule[ruleId] = true
                }
            } else {
                hasAlertedRule[ruleId] = false
            }
        }
    }

    private fun sendQuotaAlert(accountName: String, type: String, value: Int, threshold: Int) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("할당량 경고: $accountName")
            .setContentText("${type} 할당량을 $value% 사용했습니다! (경고 기준: $threshold%)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
    }

    private fun sendLoginAlert(accId: String, accName: String) {
        if (hasAlertedLogin[accId] == true) return
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("로그인 만료: $accName")
            .setContentText("권한이 만료되었습니다. 탭하여 다시 로그인하세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((accId.hashCode() % 10000) + 200, builder.build())
        
        hasAlertedLogin[accId] = true
    }

    private fun sendResetAlert(accountName: String) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("세션 할당량 초기화: $accountName")
            .setContentText("해당 계정의 세션 사용량이 초기화되었습니다!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((System.currentTimeMillis() % 10000).toInt() + 100, builder.build())
    }
}
