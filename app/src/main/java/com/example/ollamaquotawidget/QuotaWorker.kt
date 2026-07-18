package com.example.ollamaquotawidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuotaWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val accounts = SessionManager.getAccounts(context).toMutableList()
            var updated = false

            for (acc in accounts) {
                if (acc.cookie.isNotEmpty()) {
                    val qData = fetchQuota(acc.cookie)
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
                }
            }

            if (updated) {
                SessionManager.saveAccounts(context, accounts)
            }

            // Update UI
            withContext(Dispatchers.Main) {
                QuotaWidgetProvider.updateAllWidgets(context)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun fetchQuota(cookieString: String): ScrapeResult {
        return try {
            val document = Jsoup.connect("https://ollama.com/settings")
                .header("Cookie", cookieString)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get()

            QuotaParser.parse(document)
        } catch (e: Exception) {
            QuotaParser.networkErrorResult()
        }
    }

    private fun calculateResetTimestamp(resetString: String): Long {
        var ms = 0L
        val parts = resetString.trim().split(" ")
        if (parts.size >= 2) {
            val num = parts[0].toLongOrNull() ?: 0L
            val unit = parts[1].lowercase(java.util.Locale.getDefault())
            if (unit.startsWith("hour")) ms = num * 3600 * 1000
            else if (unit.startsWith("minute")) ms = num * 60 * 1000
            else if (unit.startsWith("day")) ms = num * 24 * 3600 * 1000
        }
        return if (ms > 0) System.currentTimeMillis() + ms else 0L
    }
}