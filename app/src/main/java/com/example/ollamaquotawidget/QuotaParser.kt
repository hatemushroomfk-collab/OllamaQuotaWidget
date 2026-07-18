package com.example.ollamaquotawidget

import org.json.JSONArray
import org.json.JSONObject

/**
 * ollama.com/settings 페이지 스크래핑 결과.
 *
 * @param summary       "S: 1.7% | W: 18%" 형식의 요약 문자열 (기존 UI 호환)
 * @param sessionVal    "1.7%"
 * @param weeklyVal     "18%"
 * @param detailsJson   (레거시 호환, 항상 "[]")
 * @param resetTime     "Resets in 2 hours" 등의 표시용 텍스트
 * @param resetTimeIso  "2026-07-18T07:00:00Z" — 초기화 시각 ISO 문자열
 * @param sessionModelsJson  [{model, requests, share, color}] — 세션 기준 모델별 사용량
 * @param weeklyModelsJson   [{model, requests, share, color}] — 주간 기준 모델별 사용량
 */
data class ScrapeResult(
    val summary: String,
    val sessionVal: String,
    val weeklyVal: String,
    val detailsJson: String,
    val resetTime: String,
    val resetTimeIso: String,
    val sessionModelsJson: String,
    val weeklyModelsJson: String
)

/**
 * ollama.com/settings HTML 파싱 공통 로직.
 *
 * 페이지 구조 (2025-07 기준):
 *   - Session usage:  <div data-usage-track aria-label="Session usage 1.7% used">
 *                       <button data-usage-segment data-model="glm-5.2" data-requests="7"
 *                               style="width: 100%; background: #3b82f6" ...>
 *   - Weekly usage:   동일한 구조. 여러 모델 버튼이 나열됨.
 *   - Resets 시각:    <div class="local-time" data-time="2026-07-18T07:00:00Z">Resets in 2 hours.</div>
 *
 * 버튼의 `width`는 전체 사용량(예: 18%) 중 해당 모델의 비중(예: 40.4%).
 * 모델별 실제 사용량% = 전체% * (share / 100). UI에서 필요 시 계산.
 */
object QuotaParser {

    private val sessionRegex = Regex("Session usage\\s*([0-9.]+%)\\s*used", RegexOption.IGNORE_CASE)
    private val weeklyRegex = Regex("Weekly usage\\s*([0-9.]+%)\\s*used", RegexOption.IGNORE_CASE)
    private val sessionResetRegex = Regex("Session usage.*?Resets in\\s*([0-9]+\\s*[a-zA-Z]+)", RegexOption.IGNORE_CASE)

    /**
     * "Resets in 2 hours." → "2 hours"
     * "Resets in 1 day."   → "1 day"
     */
    fun extractResetText(bodyText: String): String =
        sessionResetRegex.find(bodyText)?.groups?.get(1)?.value ?: ""

    /**
     * 전체 사용량 % 텍스트("1.7%")에서 숫자 부분만 추출해 Long(원 단위)으로 반환.
     * 계산 편의를 위해 소수점 1자리까지 보존: "1.7%" -> 17L
     */
    fun parsePercentToLong(percentStr: String): Long {
        return try {
            val v = percentStr.replace("%", "").trim().toFloat()
            (v * 10).toLong() // 1.7 -> 17
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 트랙(div[data-usage-track]) 내부의 버튼들을 순회하며
     * [{model, requests, share, color}] JSON 배열 문자열 생성.
     *
     * Jsoup Document는 파싱한 HTML에서 직접 넘겨받는다.
     */
    fun parseModelsFromTrack(document: org.jsoup.nodes.Document, trackAriaLabelPrefix: String): String {
        val jsonArray = JSONArray()
        try {
            // aria-label이 "Session usage" 또는 "Weekly usage"로 시작하는 트랙 찾기
            val track = document.selectFirst("div[data-usage-track][aria-label^=$trackAriaLabelPrefix]")
                ?: return jsonArray.toString()

            // 트랙 하위의 모든 data-usage-segment 버튼
            val segments = track.select("button[data-usage-segment]")
            for (seg in segments) {
                val model = seg.attr("data-model")
                val requests = seg.attr("data-requests").toIntOrNull() ?: 0
                val style = seg.attr("style")
                val share = extractStyleWidth(style)
                val color = extractStyleBackground(style)

                jsonArray.put(JSONObject().apply {
                    put("model", model)
                    put("requests", requests)
                    put("share", share)   // 예: 40.4  (버튼 width %)
                    put("color", color)   // 예: "#22c55e"
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonArray.toString()
    }

    /**
     * style="width: 40.4%; background: #22c55e" → 40.4 (Double)
     * style="width: 100%; background: #3b82f6"  → 100.0
     */
    private fun extractStyleWidth(style: String): Double {
        val m = Regex("width:\\s*([0-9.]+)\\s*%").find(style)
        return m?.groups?.get(1)?.value?.toDoubleOrNull() ?: 0.0
    }

    private fun extractStyleBackground(style: String): String {
        val m = Regex("background:\\s*(#[0-9a-fA-F]{3,8})").find(style)
        return m?.groups?.get(1)?.value ?: ""
    }

    /**
     * 세션 초기화 시각 ISO 문자열 추출.
     *
     * 실제 HTML 구조:
     *   <div class="relative group" data-usage-meter="">
     *     <div data-usage-bubble>...</div>
     *     <div data-usage-track aria-label="Session usage ...">  <!-- 여기 -->
     *       <button>...</button>
     *     </div>
     *   </div>
     *   <div class="local-time" data-time="2026-07-18T07:00:00Z">Resets in 2 hours.</div>
     *
     * track.nextElementSibling()은 data-usage-meter 내부에서만 찾으므로 null.
     * 따라서 track.parent().nextElementSibling()으로 한 단계 올라가서 찾거나,
     * 문서 전체에서 첫 번째 local-time div를 사용 (HTML 순서상 세션이 항상 먼저).
     */
    fun extractResetIso(document: org.jsoup.nodes.Document): String {
        return try {
            // 1차: 세션 트랙의 조부모(트랙의 부모 = data-usage-meter)의 다음 형제에서 찾기
            val track = document.selectFirst("div[data-usage-track][aria-label^=Session usage]")
            val localTimeDiv = track?.parent()?.nextElementSibling()?.selectFirst("div.local-time[data-time]")
                // 2차 fallback: 문서 전체의 첫 번째 local-time div (세션이 항상 먼저)
                ?: document.selectFirst("div.local-time[data-time]")
                ?: return ""
            localTimeDiv.attr("data-time")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 전체 파싱 워크플로우: Jsoup Document → ScrapeResult
     */
    fun parse(document: org.jsoup.nodes.Document): ScrapeResult {
        val bodyText = document.body().text()

        // 로그인 필요 체크
        if (bodyText.contains("Sign In", ignoreCase = true) ||
            document.title().contains("Sign In")) {
            return loginRequiredResult()
        }

        val sessionVal = sessionRegex.find(bodyText)?.groups?.get(1)?.value ?: "0%"
        val weeklyVal = weeklyRegex.find(bodyText)?.groups?.get(1)?.value ?: "0%"
        val sessionResetVal = extractResetText(bodyText)
        val resetIso = extractResetIso(document)

        val summary = if (sessionVal == "0%" && weeklyVal == "0%") "데이터 없음"
                      else "S: $sessionVal | W: $weeklyVal"

        val sessionModelsJson = parseModelsFromTrack(document, "Session usage")
        val weeklyModelsJson = parseModelsFromTrack(document, "Weekly usage")

        return ScrapeResult(
            summary = summary,
            sessionVal = sessionVal,
            weeklyVal = weeklyVal,
            detailsJson = "[]",
            resetTime = sessionResetVal,
            resetTimeIso = resetIso,
            sessionModelsJson = sessionModelsJson,
            weeklyModelsJson = weeklyModelsJson
        )
    }

    fun loginRequiredResult(): ScrapeResult = ScrapeResult(
        summary = "로그인 필요",
        sessionVal = "0%",
        weeklyVal = "0%",
        detailsJson = "[]",
        resetTime = "",
        resetTimeIso = "",
        sessionModelsJson = "[]",
        weeklyModelsJson = "[]"
    )

    fun networkErrorResult(): ScrapeResult = ScrapeResult(
        summary = "네트워크 에러",
        sessionVal = "0%",
        weeklyVal = "0%",
        detailsJson = "[]",
        resetTime = "",
        resetTimeIso = "",
        sessionModelsJson = "[]",
        weeklyModelsJson = "[]"
    )

    /** "S: 1.7% | W: 18%" → "1.7%" */
    fun parseVal(quota: String, prefix: String): String {
        return try {
            val split = quota.split("|")
            for (part in split) {
                if (part.trim().startsWith(prefix)) {
                    return part.substringAfter(":").trim()
                }
            }
            "0%"
        } catch (e: Exception) {
            "0%"
        }
    }

    /** "1.7%" → 1 (Int, 소수점 버림) */
    fun parsePercent(str: String): Int {
        return try { str.replace("%", "").toFloat().toInt() } catch (e: Exception) { 0 }
    }

    /**
     * 모델 JSON 배열을 표시용 문자열로 변환.
     *
     * 토글 플래그로 표시 항목 제어:
     *   - showUsage:      모델별 usage % (예: "1.0%")
     *   - showRequests:   모델별 requests 수 (예: "7req")
     *   - showUsagePerReq: 1 request당 usage % (예: "0.14%/req")
     *
     * 세 토글 모두 false → 빈 문자열 (모델 자체 미표시).
     *
     * 모델별 usage % = 전체 사용량% × share/100 (share는 버튼 width%).
     * usagePerReq = 모델 usage% / requests (requests가 0이면 표시 안 함).
     *
     * 예 (모두 true):
     *   "glm-5.2: 7req (1.0%, 0.14%/req)"
     * 예 (usage + requests만):
     *   "glm-5.2: 7req (1.0%)"
     */
    fun formatModels(
        modelsJson: String,
        totalPercent: String,
        showUsage: Boolean = true,
        showRequests: Boolean = true,
        showUsagePerReq: Boolean = false
    ): String {
        return try {
            val arr = JSONArray(modelsJson)
            if (arr.length() == 0) return ""
            // 세 토글 모두 꺼져 있으면 모델 자체를 표시하지 않음
            if (!showUsage && !showRequests && !showUsagePerReq) return ""

            val totalVal = parsePercentToLong(totalPercent) // "1.7%" -> 17L (소수 1자리 보존)
            val sb = StringBuilder()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val model = obj.optString("model", "")
                val requests = obj.optInt("requests", 0)
                val share = obj.optDouble("share", 0.0)

                // 모델별 usage%: totalVal(소수1자리*10) * share / 100 → 소수 1자리로 복원
                // 예: totalVal=170(=17.0%), share=40.4 → 170*40.4/100 = 68.68 → /10 = 6.868% → 표시 6.9%
                val modelUsageScaled = (totalVal * share / 100.0) / 10.0 // 실제 %
                val modelUsageStr = if (modelUsageScaled >= 0.1) String.format("%.1f%%", modelUsageScaled) else "0%"

                // usage per request: modelUsageScaled / requests
                val usagePerReqStr = if (requests > 0 && modelUsageScaled > 0) {
                    val perReq = modelUsageScaled / requests
                    String.format("%.3f%%/req", perReq)
                } else {
                    null
                }

                // 괄호 안 항목 조립
                val parenItems = mutableListOf<String>()
                if (showUsage) parenItems.add(modelUsageStr)
                if (showUsagePerReq && usagePerReqStr != null) parenItems.add(usagePerReqStr)

                val modelPart = if (showRequests) "$model: ${requests}req" else model
                val fullPart = if (parenItems.isNotEmpty()) "$modelPart (${parenItems.joinToString(", ")})" else modelPart

                if (sb.isNotEmpty()) sb.append(", ")
                sb.append(fullPart)
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 초기화 시각 ISO 문자열 → epoch millis.
     * "2026-07-18T07:00:00Z" → Long
     */
    fun isoToEpoch(iso: String): Long {
        if (iso.isBlank()) return 0L
        return try {
            // ISO 8601 with 'Z' suffix
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(java.time.ZoneOffset.UTC)
            java.time.Instant.from(formatter.parse(iso)).toEpochMilli()
        } catch (e: Exception) {
            try {
                // fallback: offset 없는 경우 로컬 파싱
                java.time.LocalDateTime.parse(iso.substringBefore("Z"))
                    .toEpochSecond(java.time.ZoneOffset.UTC) * 1000
            } catch (e2: Exception) {
                0L
            }
        }
    }
}