package com.example.ollamaquotawidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

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
    
    data class ScrapeResult(val summary: String, val sessionVal: String, val weeklyVal: String, val detailsJson: String)

    private fun fetchQuota(cookieString: String): ScrapeResult {
        return try {
            val document = Jsoup.connect("https://ollama.com/settings")
                .header("Cookie", cookieString)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get()

            val bodyText = document.body().text()
            
            if (bodyText.contains("Sign In", ignoreCase = true) || document.title().contains("Sign In")) {
                return ScrapeResult("로그인 필요", "0%", "0%", "[]")
            }
            
            val sessionRegex = Regex("Session usage\\s*([0-9.]+%)\\s*used", RegexOption.IGNORE_CASE)
            val sessionVal = sessionRegex.find(bodyText)?.groups?.get(1)?.value ?: "0%"

            val weeklyRegex = Regex("Weekly usage\\s*([0-9.]+%)\\s*used", RegexOption.IGNORE_CASE)
            val weeklyVal = weeklyRegex.find(bodyText)?.groups?.get(1)?.value ?: "0%"
            
            val summary = if (sessionVal == "0%" && weeklyVal == "0%") "데이터 없음" else "S: $sessionVal | W: $weeklyVal"
            val jsonArray = JSONArray()

            ScrapeResult(summary, sessionVal, weeklyVal, jsonArray.toString())
        } catch (e: Exception) {
            ScrapeResult("네트워크 에러", "0%", "0%", "[]")
        }
    }
}
