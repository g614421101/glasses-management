package com.glasses.app

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.glasses.app.ui.SplashFragment
import com.glasses.app.webview.AppWebViewClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var fragmentContainer: FrameLayout
    private var currentUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        setupWebView()
        showSplash()
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(false)
            builtInZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = AppWebViewClient(
            onError = { handleWebViewError() }
        )

        webView.webChromeClient = WebChromeClient()
    }

    private fun showSplash() {
        val splash = SplashFragment.newInstance()
        splash.onConnected = { url ->
            currentUrl = url
            loadWebApp(url)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, splash)
            .commit()
    }

    private fun loadWebApp(url: String) {
        fragmentContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
    }

    private fun handleWebViewError() {
        lifecycleScope.launch {
            webView.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            showSplash()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
