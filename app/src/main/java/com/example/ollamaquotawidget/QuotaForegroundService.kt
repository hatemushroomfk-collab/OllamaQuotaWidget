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

    // 1회성 알림 방지 상태 맵 — SharedPreferences에 영속화되어 서비스 재시작 시에도 유지
    private val hasAlertedRule = mutableMapOf<String, Boolean>()
    private val hasAlertedLogin = mutableMapOf<String, Boolean>()

    companion object {
        const val ACTION_FORCE_REFRESH = "ACTION_FORCE_REFRESH"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // 서비스 재시작 시 저장된 알림 상태 복원 (중복 알림 방지)
        hasAlertedRule.putAll(SessionManager.getAlertedRules(this))
        hasAlertedLogin.putAll(SessionManager.getAlertedLogin(this))
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
                acc.sessionResetTimestamp = if (qData.resetTimeIso.isNotEmpty()) {
                    QuotaParser.isoToEpoch(qData.resetTimeIso)
                } else {
                    calculateResetTimestamp(qData.resetTime)
                }
                acc.resetTimeIso = qData.resetTimeIso
                acc.sessionModelsJson = qData.sessionModelsJson
                acc.weeklyModelsJson = qData.weeklyModelsJson
                updated = true

                val sLong = QuotaParser.parsePercentToLong(qData.sessionVal)
                val wLong = QuotaParser.parsePercentToLong(qData.weeklyVal)
                checkAlerts(acc, sLong, wLong)

                // 세션 초기화 감지 — 초기화 시각(resetTimeIso)이 바뀌었으면 리셋된 것
                // 이전 값과 비교해서 시각이 달라지면 알림 발생
                if (acc.lastResetTimeIso.isNotEmpty() && 
                    qData.resetTimeIso.isNotEmpty() && 
                    qData.resetTimeIso != acc.lastResetTimeIso) {
                    if (acc.alertOnReset) {
                        sendResetAlert(acc.id, acc.name)
                    }
                }
                acc.lastResetTimeIso = qData.resetTimeIso
                acc.previousSessionVal = sLong
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
    
    private fun parsePercent(str: String): Int = QuotaParser.parsePercent(str)

    private fun parseVal(quota: String, prefix: String): String = QuotaParser.parseVal(quota, prefix)

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
        val collapsedViews = RemoteViews(packageName, R.layout.notification_custom_collapsed)
        collapsedViews.setTextViewText(R.id.tvUpdateTimeCollapsed, timeString)
        collapsedViews.setOnClickPendingIntent(R.id.llCollapsedRoot, refreshPendingIntent)

        // 계정별 행 동적 추가 — 계정명(가변) + usage(우측 고정) 정렬
        collapsedViews.removeAllViews(R.id.llCollapsedAccountsContainer)
        if (collapsedAccounts.isEmpty()) {
            val emptyView = RemoteViews(packageName, R.layout.notification_collapsed_account_item)
            emptyView.setTextViewText(R.id.tvCollapsedAccName, "설정된 계정 없음")
            emptyView.setTextViewText(R.id.tvCollapsedAccUsage, "")
            collapsedViews.addView(R.id.llCollapsedAccountsContainer, emptyView)
        } else {
            for (acc in collapsedAccounts) {
                val row = RemoteViews(packageName, R.layout.notification_collapsed_account_item)
                row.setTextViewText(R.id.tvCollapsedAccName, acc.name)
                row.setTextViewText(R.id.tvCollapsedAccUsage, acc.quotaSummary)
                collapsedViews.addView(R.id.llCollapsedAccountsContainer, row)
            }
        }

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

            // 모델별 사용량 바인딩 — 색 점 + 개별 행으로 동적 addView (expanded에서만)
            // 252dp 제한 때문에 usage 기준 상위 2개 모델만 표시
            itemView.removeAllViews(R.id.llSessionModelsContainer)
            itemView.removeAllViews(R.id.llWeeklyModelsContainer)

            val sessionModels = QuotaParser.takeTopModelsByUsage(
                QuotaParser.parseModelsToList(acc.sessionModelsJson, sVal), 2
            )
            val weeklyModels = QuotaParser.takeTopModelsByUsage(
                QuotaParser.parseModelsToList(acc.weeklyModelsJson, wVal), 2
            )

            for (m in sessionModels) {
                val infoStr = QuotaParser.formatModelInfo(
                    m, acc.showModelUsage, acc.showModelRequests, acc.showModelUsagePerReq
                )
                if (infoStr.isEmpty()) continue
                val modelView = RemoteViews(packageName, R.layout.notification_model_item)
                modelView.setTextViewText(R.id.tvColorDot, "●")
                modelView.setTextColor(R.id.tvColorDot, QuotaParser.parseColorInt(m.color))
                modelView.setTextViewText(R.id.tvModelInfo, "S: $infoStr")
                itemView.addView(R.id.llSessionModelsContainer, modelView)
            }

            for (m in weeklyModels) {
                val infoStr = QuotaParser.formatModelInfo(
                    m, acc.showModelUsage, acc.showModelRequests, acc.showModelUsagePerReq
                )
                if (infoStr.isEmpty()) continue
                val modelView = RemoteViews(packageName, R.layout.notification_model_item)
                modelView.setTextViewText(R.id.tvColorDot, "●")
                modelView.setTextColor(R.id.tvColorDot, QuotaParser.parseColorInt(m.color))
                modelView.setTextViewText(R.id.tvModelInfo, "W: $infoStr")
                itemView.addView(R.id.llWeeklyModelsContainer, modelView)
            }

            expandedViews.addView(R.id.llAccountsContainer, itemView)
        }

        builder.setContentTitle("Ollama Quota")
        // setContentText는 알림 기본 본문 — 줄바꿈 없이 첫 계정만 표시
        val contentTextDefault = if (collapsedAccounts.isNotEmpty()) {
            "${collapsedAccounts[0].name}: ${collapsedAccounts[0].quotaSummary}" +
            if (collapsedAccounts.size > 1) " 외 ${collapsedAccounts.size - 1}개" else ""
        } else "설정된 계정 없음"
        builder.setContentText(contentTextDefault)
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

            val result = QuotaParser.parse(document)

            // 로그인 필요 또는 401/403 감지
            if (result.summary == "로그인 필요") {
                sendLoginAlert(accId, accName)
            } else {
                if (hasAlertedLogin[accId] == true) {
                    hasAlertedLogin[accId] = false
                    SessionManager.saveAlertedLogin(this@QuotaForegroundService, hasAlertedLogin)
                }
            }

            result
        } catch (e: Exception) {
            if (e.message?.contains("401") == true || e.message?.contains("403") == true) {
                sendLoginAlert(accId, accName)
            }
            QuotaParser.networkErrorResult()
        }
    }

    private fun checkAlerts(acc: Account, sLong: Long, wLong: Long) {
        val rulesArray = try { JSONArray(acc.alertRulesJson) } catch (e: Exception) { JSONArray() }
        var changed = false
        for (i in 0 until rulesArray.length()) {
            val rule = rulesArray.getJSONObject(i)
            if (!rule.optBoolean("enabled", true)) continue

            val ruleId = rule.optString("id", "")
            if (ruleId.isEmpty()) continue

            val type = rule.optString("type", "SESSION")
            val threshold = rule.optInt("threshold", 90)
            // threshold(예: 90)를 ×10하여 Long과 단위 맞춤 (예: 17L = 1.7%)
            val thresholdLong = threshold.toLong() * 10L

            val currentValue = if (type == "WEEKLY") wLong else sLong
            val typeStr = if (type == "WEEKLY") "주간" else "세션"

            if (currentValue >= thresholdLong) {
                if (hasAlertedRule[ruleId] != true) {
                    // 표시용 % 문자열: Long → "1.7%"
                    val valueStr = String.format("%.1f%%", currentValue / 10.0)
                    sendQuotaAlert(acc.name, typeStr, valueStr, threshold, ruleId)
                    hasAlertedRule[ruleId] = true
                    changed = true
                }
            } else {
                if (hasAlertedRule[ruleId] == true) {
                    hasAlertedRule[ruleId] = false
                    changed = true
                }
            }
        }
        if (changed) {
            SessionManager.saveAlertedRules(this, hasAlertedRule)
        }
    }

    private fun sendQuotaAlert(accountName: String, type: String, valueStr: String, threshold: Int, ruleId: String) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("할당량 경고: $accountName")
            .setContentText("${type} 할당량을 $valueStr 사용했습니다! (경고 기준: $threshold%)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 알림 ID: 1000 대역 (quota alert). ruleId 해시로 각 규칙마다 고유 ID.
        val notifId = 1000 + (ruleId.hashCode() and 0x3FF) // 1000~1999
        manager.notify(notifId, builder.build())
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
        // 알림 ID: 3000 대역 (login alert). accId 해시로 각 계정마다 고유 ID.
        val notifId = 3000 + (accId.hashCode() and 0x3FF) // 3000~3999
        manager.notify(notifId, builder.build())

        hasAlertedLogin[accId] = true
        SessionManager.saveAlertedLogin(this, hasAlertedLogin)
    }

    private fun sendResetAlert(accId: String, accountName: String) {
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
        // 알림 ID: 2000 대역 (reset alert). accId 해시로 각 계정마다 고유 ID.
        val notifId = 2000 + (accId.hashCode() and 0x3FF) // 2000~2999
        manager.notify(notifId, builder.build())
    }
}
