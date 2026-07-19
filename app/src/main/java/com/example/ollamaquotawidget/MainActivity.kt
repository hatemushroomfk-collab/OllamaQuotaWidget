package com.example.ollamaquotawidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var llAccountsContainer: android.widget.LinearLayout
    private lateinit var btnAddAccount: Button
    private lateinit var btnToggleService: Button
    private lateinit var switchAutoStart: Switch

    // 계정 카드별 뷰 참조를 보관 — "전체 설정 저장" 버튼에서 일괄 저장할 때 사용
    private data class AccountCardViews(
        val accountId: String,
        val etName: EditText,
        val cbCollapsed: android.widget.CheckBox,
        val cbExpanded: android.widget.CheckBox,
        val cbAlertOnReset: android.widget.CheckBox,
        val spinnerResetTimeMode: Spinner,
        val cbShowModelUsage: android.widget.CheckBox,
        val cbShowModelRequests: android.widget.CheckBox,
        val cbShowModelUsagePerReq: android.widget.CheckBox,
        val llAlertsContainer: android.widget.LinearLayout
    )
    private val cardViewsMap = mutableMapOf<String, AccountCardViews>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        llAccountsContainer = findViewById(R.id.llAccountsContainer)
        btnAddAccount = findViewById(R.id.btnAddAccount)
        btnToggleService = findViewById(R.id.btnToggleService)

        btnAddAccount.setOnClickListener {
            addNewAccount()
        }

        btnToggleService.setOnClickListener {
            checkPermissionsAndStartService()
        }

        switchAutoStart = findViewById(R.id.switchAutoStart)
        switchAutoStart.isChecked = SessionManager.isAutoStartEnabled(this)
        switchAutoStart.setOnCheckedChangeListener { _, isChecked ->
            SessionManager.saveAutoStartEnabled(this, isChecked)
        }

        setupIntervalUI()

        // "전체 설정 저장" 버튼 — 모든 계정 카드의 설정을 한 번에 저장
        val btnSaveAll = findViewById<Button>(R.id.btnSaveAllSettings)
        btnSaveAll.setOnClickListener {
            saveAllAccountSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        renderAccounts()
    }

    private fun startLogin(accountId: String) {
        val intent = Intent(this, LoginWebViewActivity::class.java)
        intent.putExtra("ACCOUNT_KEY", accountId)
        startActivity(intent)
    }

    private fun formatDetailJson(jsonString: String): String {
        if (jsonString == "{}") return "상세 내역 없음"
        return try {
            val sb = java.lang.StringBuilder()
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                sb.append("- ").append(obj.getString("model")).append(": ")
                  .append(obj.getString("requests")).append(" requests\n")
            }
            if (sb.isEmpty()) "상세 내역 없음" else sb.toString().trim()
        } catch (e: Exception) {
            "상세 내역 파싱 오류"
        }
    }

    private fun addNewAccount() {
        val accounts = SessionManager.getAccounts(this).toMutableList()
        val newId = UUID.randomUUID().toString()
        val newCount = accounts.size + 1
        
        val currentCollapsedCount = accounts.count { it.showCollapsed }
        val shouldShowCollapsed = currentCollapsedCount < 2

        val initialRules = JSONArray()
        val rule1 = JSONObject().apply {
            put("id", UUID.randomUUID().toString())
            put("type", "SESSION")
            put("threshold", 90)
            put("enabled", true)
        }
        initialRules.put(rule1)

        val newAcc = Account(
            id = newId,
            name = "계정 $newCount",
            cookie = "",
            quotaSummary = "데이터 없음",
            quotaDetails = "[]",
            showCollapsed = shouldShowCollapsed,
            showExpanded = true,
            alertRulesJson = initialRules.toString(),
            sessionResetTime = "",
            alertOnReset = true,
            previousSessionVal = -1,
            resetTimeDisplayMode = 0,
            sessionResetTimestamp = 0L
        )
        accounts.add(newAcc)
        SessionManager.saveAccounts(this, accounts)
        renderAccounts()
    }

    private fun renderAccounts() {
        llAccountsContainer.removeAllViews()
        cardViewsMap.clear()
        val accounts = SessionManager.getAccounts(this)
        
        for (acc in accounts) {
            val view = layoutInflater.inflate(R.layout.item_account_card, llAccountsContainer, false)
            
            val etName = view.findViewById<EditText>(R.id.etItemName)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnItemDelete)
            val tvStatus = view.findViewById<TextView>(R.id.tvItemStatus)
            val btnLogin = view.findViewById<Button>(R.id.btnItemLogin)
            val tvDetail = view.findViewById<TextView>(R.id.tvItemDetail)
            
            val cbCollapsed = view.findViewById<android.widget.CheckBox>(R.id.cbItemShowCollapsed)
            val cbExpanded = view.findViewById<android.widget.CheckBox>(R.id.cbItemShowExpanded)
            val cbAlertOnReset = view.findViewById<android.widget.CheckBox>(R.id.cbItemAlertOnReset)
            val spinnerResetTimeMode = view.findViewById<Spinner>(R.id.spinnerResetTimeMode)
            
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("남은 시간", "예상 시각"))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerResetTimeMode.adapter = adapter
            
            val llAlertsContainer = view.findViewById<android.widget.LinearLayout>(R.id.llAlertsContainer)
            val btnAddAlert = view.findViewById<Button>(R.id.btnAddAlert)

            etName.setText(acc.name)
            tvStatus.text = if (acc.cookie.isNotEmpty()) getString(R.string.logged_in) else getString(R.string.not_logged_in)
            tvDetail.text = formatDetailJson(acc.quotaDetails)

            // 모델별 사용량 표시 + 토글 UI
            val llSessionContainer = view.findViewById<android.widget.LinearLayout>(R.id.llSessionModelsContainer)
            val llWeeklyContainer = view.findViewById<android.widget.LinearLayout>(R.id.llWeeklyModelsContainer)
            val cbShowModelUsage = view.findViewById<android.widget.CheckBox>(R.id.cbShowModelUsage)
            val cbShowModelRequests = view.findViewById<android.widget.CheckBox>(R.id.cbShowModelRequests)
            val cbShowModelUsagePerReq = view.findViewById<android.widget.CheckBox>(R.id.cbShowModelUsagePerReq)

            cbShowModelUsage.isChecked = acc.showModelUsage
            cbShowModelRequests.isChecked = acc.showModelRequests
            cbShowModelUsagePerReq.isChecked = acc.showModelUsagePerReq

            val sVal = QuotaParser.parseVal(acc.quotaSummary, "S")
            val wVal = QuotaParser.parseVal(acc.quotaSummary, "W")

            // 모델 행(색 점 + 정보)을 동적으로 생성해서 컨테이너에 추가
            fun buildModelRows(container: android.widget.LinearLayout, modelsJson: String, totalVal: String, prefix: String) {
                container.removeAllViews()
                val models = QuotaParser.parseModelsToList(modelsJson, totalVal)
                if (models.isEmpty()) {
                    // 데이터 없음 표시
                    val emptyTv = TextView(this).apply {
                        text = "$prefix: -"
                        textSize = 11f
                        setTextColor(0xFF666666.toInt())
                    }
                    container.addView(emptyTv)
                    return
                }
                for (m in models) {
                    val infoStr = QuotaParser.formatModelInfo(
                        m, cbShowModelUsage.isChecked, cbShowModelRequests.isChecked, cbShowModelUsagePerReq.isChecked
                    )
                    if (infoStr.isEmpty()) continue

                    val row = android.widget.LinearLayout(this).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    val dotTv = TextView(this).apply {
                        text = "●"
                        textSize = 11f
                        setTextColor(QuotaParser.parseColorInt(m.color))
                        val lp = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        lp.marginEnd = 8
                        layoutParams = lp
                    }
                    val infoTv = TextView(this).apply {
                        text = "$prefix: $infoStr"
                        textSize = 11f
                        setTextColor(0xFF333333.toInt())
                    }
                    row.addView(dotTv)
                    row.addView(infoTv)
                    container.addView(row)
                }
            }

            // 체크박스 변경 시 즉시 미리보기 갱신
            val updatePreview: () -> Unit = {
                buildModelRows(llSessionContainer, acc.sessionModelsJson, sVal, "세션")
                buildModelRows(llWeeklyContainer, acc.weeklyModelsJson, wVal, "주간")
            }
            cbShowModelUsage.setOnCheckedChangeListener { _, _ -> updatePreview() }
            cbShowModelRequests.setOnCheckedChangeListener { _, _ -> updatePreview() }
            cbShowModelUsagePerReq.setOnCheckedChangeListener { _, _ -> updatePreview() }
            updatePreview()

            cbCollapsed.isChecked = acc.showCollapsed
            cbExpanded.isChecked = acc.showExpanded
            cbAlertOnReset.isChecked = acc.alertOnReset
            spinnerResetTimeMode.setSelection(acc.resetTimeDisplayMode)

            // Append reset time to summary if available
            if (acc.sessionResetTime.isNotEmpty() && acc.quotaSummary != "데이터 없음") {
                val displayStr = if (acc.resetTimeDisplayMode == 1 && acc.sessionResetTimestamp > 0) {
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(acc.sessionResetTimestamp))
                } else {
                    acc.sessionResetTime
                }
                tvDetail.text = formatDetailJson(acc.quotaDetails) + "\n(초기화: $displayStr)"
            }

            // Render Alert Rules
            val rulesArray = try { JSONArray(acc.alertRulesJson) } catch(e: Exception) { JSONArray() }
            llAlertsContainer.removeAllViews()
            
            for (i in 0 until rulesArray.length()) {
                val ruleObj = rulesArray.getJSONObject(i)
                val ruleView = layoutInflater.inflate(R.layout.item_alert_rule, llAlertsContainer, false)
                
                val spinner = ruleView.findViewById<Spinner>(R.id.spinnerAlertType)
                val etThreshold = ruleView.findViewById<EditText>(R.id.etAlertThreshold)
                val switchEnabled = ruleView.findViewById<Switch>(R.id.switchAlertEnabled)
                val btnAlertDelete = ruleView.findViewById<ImageButton>(R.id.btnAlertDelete)
                
                // Spinner logic (0: Session, 1: Weekly)
                val typeStr = ruleObj.optString("type", "SESSION")
                spinner.setSelection(if (typeStr == "WEEKLY") 1 else 0)
                
                etThreshold.setText(ruleObj.optInt("threshold", 90).toString())
                switchEnabled.isChecked = ruleObj.optBoolean("enabled", true)
                
                // Tag id for saving later
                ruleView.tag = ruleObj.optString("id", UUID.randomUUID().toString())
                
                btnAlertDelete.setOnClickListener {
                    llAlertsContainer.removeView(ruleView)
                }
                
                llAlertsContainer.addView(ruleView)
            }

            btnAddAlert.setOnClickListener {
                val ruleView = layoutInflater.inflate(R.layout.item_alert_rule, llAlertsContainer, false)
                ruleView.tag = UUID.randomUUID().toString()
                
                val btnAlertDelete = ruleView.findViewById<ImageButton>(R.id.btnAlertDelete)
                btnAlertDelete.setOnClickListener {
                    llAlertsContainer.removeView(ruleView)
                }
                
                llAlertsContainer.addView(ruleView)
            }

            // Listeners
            btnDelete.setOnClickListener {
                val updatedList = SessionManager.getAccounts(this).toMutableList()
                updatedList.removeAll { it.id == acc.id }
                SessionManager.saveAccounts(this, updatedList)
                renderAccounts()
            }
            
            btnLogin.setOnClickListener {
                startLogin(acc.id)
            }

            // 카드별 뷰 참조를 보관 (전체 설정 저장 버튼에서 사용)
            cardViewsMap[acc.id] = AccountCardViews(
                accountId = acc.id,
                etName = etName,
                cbCollapsed = cbCollapsed,
                cbExpanded = cbExpanded,
                cbAlertOnReset = cbAlertOnReset,
                spinnerResetTimeMode = spinnerResetTimeMode,
                cbShowModelUsage = cbShowModelUsage,
                cbShowModelRequests = cbShowModelRequests,
                cbShowModelUsagePerReq = cbShowModelUsagePerReq,
                llAlertsContainer = llAlertsContainer
            )

            llAccountsContainer.addView(view)
        }
    }

    /**
     * 모든 계정 카드의 설정을 한 번에 저장.
     * "전체 설정 저장" 버튼에서 호출.
     * 각 카드의 뷰 참조는 cardViewsMap에 보관되어 있음.
     */
    private fun saveAllAccountSettings() {
        if (cardViewsMap.isEmpty()) {
            Toast.makeText(this, "저장할 계정이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedList = SessionManager.getAccounts(this).toMutableList()

        // 한줄 알림창 collapsed 선택 개수 검증 (전체 계정 기준 최대 2개)
        var collapsedCount = 0
        for ((id, views) in cardViewsMap) {
            if (views.cbCollapsed.isChecked) collapsedCount++
        }
        if (collapsedCount > 2) {
            Toast.makeText(this, "한줄 알림창은 최대 2개까지만 선택할 수 있습니다. (현재 ${collapsedCount}개 선택)", Toast.LENGTH_LONG).show()
            return
        }

        var savedCount = 0
        for (acc in updatedList) {
            val views = cardViewsMap[acc.id] ?: continue
            acc.name = views.etName.text.toString()
            acc.showCollapsed = views.cbCollapsed.isChecked
            acc.showExpanded = views.cbExpanded.isChecked
            acc.alertOnReset = views.cbAlertOnReset.isChecked
            acc.resetTimeDisplayMode = views.spinnerResetTimeMode.selectedItemPosition
            acc.showModelUsage = views.cbShowModelUsage.isChecked
            acc.showModelRequests = views.cbShowModelRequests.isChecked
            acc.showModelUsagePerReq = views.cbShowModelUsagePerReq.isChecked

            // 알림 규칙 수집
            val newRulesArray = JSONArray()
            for (i in 0 until views.llAlertsContainer.childCount) {
                val ruleView = views.llAlertsContainer.getChildAt(i)
                val spinner = ruleView.findViewById<Spinner>(R.id.spinnerAlertType)
                val etThreshold = ruleView.findViewById<EditText>(R.id.etAlertThreshold)
                val switchEnabled = ruleView.findViewById<Switch>(R.id.switchAlertEnabled)

                val ruleObj = JSONObject()
                ruleObj.put("id", ruleView.tag.toString())
                ruleObj.put("type", if (spinner.selectedItemPosition == 1) "WEEKLY" else "SESSION")
                ruleObj.put("threshold", etThreshold.text.toString().toIntOrNull() ?: 90)
                ruleObj.put("enabled", switchEnabled.isChecked)
                newRulesArray.put(ruleObj)
            }
            acc.alertRulesJson = newRulesArray.toString()
            savedCount++
        }

        SessionManager.saveAccounts(this, updatedList)
        Toast.makeText(this, "전체 설정 저장됨 (${savedCount}개 계정)", Toast.LENGTH_SHORT).show()
    }

    private fun setupIntervalUI() {
        val etHours = findViewById<EditText>(R.id.etHours)
        val etMins = findViewById<EditText>(R.id.etMins)
        val etSecs = findViewById<EditText>(R.id.etSecs)
        val btnApplyInterval = findViewById<Button>(R.id.btnApplyInterval)
        val tvCurrentInterval = findViewById<TextView>(R.id.tvCurrentInterval)

        fun updateIntervalText() {
            val ms = SessionManager.getUpdateInterval(this)
            val totalSecs = ms / 1000
            val h = totalSecs / 3600
            val m = (totalSecs % 3600) / 60
            val s = totalSecs % 60
            tvCurrentInterval.text = "현재 갱신 주기: ${h}시간 ${m}분 ${s}초"
        }
        updateIntervalText()

        btnApplyInterval.setOnClickListener {
            val hStr = etHours.text.toString()
            val mStr = etMins.text.toString()
            val sStr = etSecs.text.toString()
            val h = if (hStr.isEmpty()) 0L else hStr.toLong()
            val m = if (mStr.isEmpty()) 0L else mStr.toLong()
            val s = if (sStr.isEmpty()) 0L else sStr.toLong()

            val totalMs = (h * 3600 + m * 60 + s) * 1000
            if (totalMs < 1000) {
                Toast.makeText(this, "최소 1초 이상으로 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SessionManager.saveUpdateInterval(this, totalMs)
            updateIntervalText()
            Toast.makeText(this, "주기가 적용되었습니다. 알림창에서 새로고침을 누르거나 잠시 후 반영됩니다.", Toast.LENGTH_SHORT).show()
            
            checkPermissionsAndStartService()
        }
    }

    private fun checkPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }
        
        val intent = Intent(this, QuotaForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndStartService()
        } else {
            Toast.makeText(this, "알림 권한이 거부되어 상단 알림을 띄울 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
