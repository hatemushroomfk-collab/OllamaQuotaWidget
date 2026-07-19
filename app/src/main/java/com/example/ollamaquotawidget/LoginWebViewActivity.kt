package com.example.ollamaquotawidget

import android.annotation.SuppressLint
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

    // 허용 도메인 화이트리스트 — ollama.com 및 로그인 제공자
    private val allowedHosts = setOf(
        "ollama.com",
        "signin.ollama.com",
        "accounts.google.com",
        "github.com",
        "login.microsoftonline.com"
    )

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

        // 계정 간 충돌을 막기 위해 모든 쿠키를 초기화합니다. (매번 이메일을 쳐야 하는 원본 상태)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // Google 로그인 403 disallowed_useragent 에러 우회를 위해 WebView 식별자 제거
        val userAgent = webView.settings.userAgentString
        webView.settings.userAgentString = userAgent.replace("; wv", "")

        webView.webViewClient = object : WebViewClient() {
            // URL 화이트리스트 — 허용되지 않은 도메인은 외부 브라우저로 열기
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                val host = url.host ?: return false
                // 화이트리스트 도메인 또는 그 서브도메인만 허용
                val isAllowed = allowedHosts.any { host == it || host.endsWith(".$it") }
                if (!isAllowed) {
                    // 외부 브라우저로 열기 (앱 내 WebView에서 차단)
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url.toString()))
                        startActivity(intent)
                    } catch (e: Exception) {
                        // 브라우저가 없으면 무시
                    }
                    return true  // WebView 로드 중단
                }
                return false  // WebView에서 로드 허용
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                checkCookies()
            }
        }

        webView.loadUrl("https://ollama.com/signin")
    }

    private fun checkCookies() {
        val cookies = CookieManager.getInstance().getCookie("https://ollama.com")
        // ollama.com 로그인 후 생성되는 쿠키 중 세션 토큰 감지
        // 일반적으로 세션 쿠키는 "session" 또는 인증 관련 키워드를 포함
        if (cookies != null && cookies.contains("session")) {
            SessionManager.updateAccountCookie(this, accountKey, cookies)

            Toast.makeText(this, "로그인 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show()

            // 위젯 업데이트 트리거
            QuotaWidgetProvider.updateAllWidgets(this)

            finish()
        }
    }

    override fun onDestroy() {
        // WebView 메모리 누수 방지 — 명시적으로 destroy 호출
        webView.apply {
            stopLoading()
            webChromeClient = null
            webViewClient = WebViewClient()
            removeJavascriptInterface("Android")
            destroy()
        }
        super.onDestroy()
    }
}