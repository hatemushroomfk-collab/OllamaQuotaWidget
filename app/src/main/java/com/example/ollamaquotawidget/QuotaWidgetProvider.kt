package com.example.ollamaquotawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class QuotaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.ollamaquotawidget.ACTION_REFRESH_QUOTA") {
            // Trigger QuotaForegroundService instead of Worker
            val serviceIntent = Intent(context, QuotaForegroundService::class.java).apply {
                action = QuotaForegroundService.ACTION_FORCE_REFRESH
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
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

        private fun parsePercent(str: String): Int {
            return try { str.replace("%", "").toFloat().toInt() } catch (e: Exception) { 0 }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quota)

            views.removeAllViews(R.id.llAccountsContainer)

            val accounts = SessionManager.getAccounts(context).filter { it.showExpanded }
            for (acc in accounts) {
                val sVal = parseVal(acc.quotaSummary, "S")
                val wVal = parseVal(acc.quotaSummary, "W")
                val sInt = parsePercent(sVal)
                val wInt = parsePercent(wVal)
                
                val itemView = RemoteViews(context.packageName, R.layout.widget_account_item)
                
                val displayStr = if (acc.resetTimeDisplayMode == 1 && acc.sessionResetTimestamp > 0) {
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(acc.sessionResetTimestamp))
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
                
                views.addView(R.id.llAccountsContainer, itemView)
            }
            
            val timeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            views.setTextViewText(R.id.tvUpdateTime, "Updated: $timeString")

            val intent = Intent(context, QuotaWidgetProvider::class.java).apply {
                action = "com.example.ollamaquotawidget.ACTION_REFRESH_QUOTA"
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btnRefresh, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, QuotaWidgetProvider::class.java))
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
