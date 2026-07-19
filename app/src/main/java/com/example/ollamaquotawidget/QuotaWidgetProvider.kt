package com.example.ollamaquotawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

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
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quota)

            views.removeAllViews(R.id.llAccountsContainer)

            val accounts = SessionManager.getAccounts(context).filter { it.showExpanded }
            for (acc in accounts) {
                val sVal = QuotaParser.parseVal(acc.quotaSummary, "S")
                val wVal = QuotaParser.parseVal(acc.quotaSummary, "W")
                val sInt = QuotaParser.parsePercent(sVal)
                val wInt = QuotaParser.parsePercent(wVal)

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

                // 모델별 사용량 바인딩 — 색 점 + 개별 행으로 동적 addView
                // 위젯 공간 제한 때문에 usage 기준 상위 2개 모델만 표시
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
                    val modelView = RemoteViews(context.packageName, R.layout.widget_model_item)
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
                    val modelView = RemoteViews(context.packageName, R.layout.widget_model_item)
                    modelView.setTextViewText(R.id.tvColorDot, "●")
                    modelView.setTextColor(R.id.tvColorDot, QuotaParser.parseColorInt(m.color))
                    modelView.setTextViewText(R.id.tvModelInfo, "W: $infoStr")
                    itemView.addView(R.id.llWeeklyModelsContainer, modelView)
                }

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
