package com.example.ollamaquotawidget

import android.annotation.SuppressLint
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


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                checkCookies()
            }
        }

        webView.loadUrl("https://ollama.com/signin") // Or /login depending on their actual URL
    }

    private fun checkCookies() {
        val cookies = CookieManager.getInstance().getCookie("https://ollama.com")
        if (cookies != null && cookies.contains("session")) { // Assuming they use 'session' or similar cookie
            // We got a session cookie!
            // Let's save the entire cookie string for this domain.
            SessionManager.updateAccountCookie(this, accountKey, cookies)
            
            Toast.makeText(this, "로그인 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show()
            
            // Also trigger a widget update
            QuotaWidgetProvider.updateAllWidgets(this)
            
            finish()
        }
    }
}
