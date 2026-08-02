package com.example.ollamaquotawidget

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var accountKey: String = ""
    private var saved = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_webview)

        accountKey = intent.getStringExtra("ACCOUNT_KEY") ?: ""
        if (accountKey.isEmpty()) {
            finish()
            return
        }

        webView = findViewById(R.id.webView)

        // 계정 간 충돌 방지: 모든 쿠키 초기화
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setAllowFileAccess(false)
        webView.settings.setAllowContentAccess(false)

        // Google 로그인 403 disallowed_useragent 우회
        val userAgent = webView.settings.userAgentString
        webView.settings.userAgentString = userAgent.replace("; wv", "")

        // 구버전 방식: shouldOverrideUrlLoading 없음 — 모든 URL이 WebView 안에서 열림
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleUrl(url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleUrl(url ?: return false)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                checkCookies()
            }
        }

        webView.loadUrl("https://ollama.com/signin")
    }

    /**
     * "session"이 포함된 쿠키가 있으면 로그인 성공으로 간주 — 자동 저장
     */
    private fun checkCookies() {
        if (saved) return
        val cookies = CookieManager.getInstance().getCookie("https://ollama.com")
        if (cookies != null && cookies.contains("session")) {
            saved = true
            CookieManager.getInstance().flush()
            SessionManager.updateAccountCookie(this, accountKey, cookies)
            Toast.makeText(this, "로그인 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show()
            QuotaWidgetProvider.updateAllWidgets(this)
            finish()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.apply {
                stopLoading()
                webChromeClient = null
                webViewClient = WebViewClient()
                removeJavascriptInterface("Android")
                destroy()
            }
        }
        super.onDestroy()
    }

    /**
     * Returns true when the URL is handled here (opened externally),
     * false when the WebView should load it. Only ollama.com loads in WebView.
     */
    private fun handleUrl(url: String): Boolean {
        val host = Uri.parse(url).host
        if (host == "ollama.com") return false
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }
}
