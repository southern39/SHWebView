/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebviewlib

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.tencent.smtt.export.external.interfaces.JsPromptResult
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import com.tencent.smtt.sdk.CookieManager
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import java.io.File

interface WebViewListener {
    fun onProgressChanged(webView: SHWebView, progress: Int)
    fun onReceivedTitle(webView: SHWebView, title: String)
    fun onPageFinished(webView: SHWebView, url: String)
}

class SHWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    WebView(context, attrs) {

    private val baseCacheDir by lazy {
        File(context.cacheDir, "webView")
    }

    private val databaseCachePath by lazy {
        File(baseCacheDir, "databaseCache").absolutePath
    }

    private val appCachePath by lazy {
        File(baseCacheDir, "appCache").absolutePath
    }

    var hostLifecycleOwner: LifecycleOwner? = null

    var webViewListener: WebViewListener? = null

    private val mWebChromeClient = object : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            Log.d(TAG, "onProgressChanged: $newProgress")
            webViewListener?.onProgressChanged(this@SHWebView, newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String?) {
            super.onReceivedTitle(view, title)
            Log.d(TAG, "onReceivedTitle: $title")
            webViewListener?.onReceivedTitle(this@SHWebView, title ?: "")
        }

        override fun onJsAlert(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            Log.d(TAG, "onJsAlert: $view $message")
            return super.onJsAlert(view, url, message, result)
        }

        override fun onJsConfirm(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            Log.d(TAG, "onJsConfirm: $url $message")
            return super.onJsConfirm(view, url, message, result)
        }

        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            Log.d(TAG, "onJsPrompt: $url $message $defaultValue")
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }
    }

    private val mWebViewClient = object : WebViewClient() {

        private var startTime = 0L

        override fun shouldOverrideUrlLoading(
            webView: WebView,
            url: String
        ): Boolean {
            webView.loadUrl(url)
            return true
        }

        override fun onPageStarted(webView: WebView, url: String?, favicon: Bitmap?) {
            super.onPageStarted(webView, url, favicon)
            startTime = System.currentTimeMillis()
        }

        override fun onPageFinished(webView: WebView, url: String?) {
            super.onPageFinished(webView, url)
            Log.d(TAG, "onPageFinished: $url")
            webViewListener?.onPageFinished(this@SHWebView, url ?: "")
            Log.d(TAG, "onPageFinished duration： " + (System.currentTimeMillis() - startTime))
        }

        override fun onReceivedSslError(
            webView: WebView,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            Log.e(TAG, "onReceivedSslError-$error")
            super.onReceivedSslError(webView, handler, error)
        }

        override fun shouldInterceptRequest(webView: WebView, url: String): WebResourceResponse? {
            return super.shouldInterceptRequest(webView, url)
        }

        override fun shouldInterceptRequest(
            webView: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return WebViewInterceptRequestProxy.shouldInterceptRequest(request)
                ?: super.shouldInterceptRequest(webView, request)
        }
    }

    init {
        webViewClient = mWebViewClient
        webChromeClient = mWebChromeClient
        initWebViewSettings(this)
        initWebViewSettingsExtension(this)
        // Add JS Interface if need here
        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.d(
                TAG,
                "setDownloadListener: $url \n $userAgent \n $contentDisposition \n $mimetype \n $contentLength"
            )
        }
    }

    fun toLoadUrl(url: String, cookie: String) {
        val cookieManager = CookieManager.getInstance()
        cookieManager?.setCookie(url, cookie)
        cookieManager?.flush()
        loadUrl(url)
    }

    fun toGoBack(): Boolean {
        if (canGoBack()) {
            goBack()
            return false
        }
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings(webView: WebView) {
        with(webView.settings) {
            javaScriptEnabled = true
            pluginsEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            loadsImagesAutomatically = true
            safeBrowsingEnabled = false
            domStorageEnabled = true
            databaseEnabled = true
            databasePath = databaseCachePath
            setAppCacheEnabled(true)
            setAppCachePath(appCachePath)
            cacheMode = WebSettings.LOAD_DEFAULT
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun initWebViewSettingsExtension(webView: WebView) {
        val settingsExtension = webView.settingsExtension
        settingsExtension?.setContentCacheEnable(true)
        settingsExtension?.setDisplayCutoutEnable(true)
        settingsExtension?.setDayOrNight(true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow : $context")
        (hostLifecycleOwner ?: findLifecycleOwner(context))?.let {
            addHostLifecycleObserver(it)
        }
    }

    private fun findLifecycleOwner(context: Context): LifecycleOwner? {
        if (context is LifecycleOwner) {
            return context
        }
        if (context is MutableContextWrapper) {
            val baseContext = context.baseContext
            if (baseContext is LifecycleOwner) {
                return baseContext
            }
        }
        return null
    }

    private fun addHostLifecycleObserver(lifecycleOwner: LifecycleOwner) {
        Log.d(TAG, "addHostLifecycleObserver")
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                onHostResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                onHostPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                onHostDestroy()
            }
        })
    }

    private fun onHostResume() {
        Log.d(TAG, "onHostResume")
        onResume()
    }

    private fun onHostPause() {
        Log.d(TAG, "onHostPause")
        onPause()
    }

    private fun onHostDestroy() {
        Log.d(TAG, "onHostDestroy")
        release()
    }

    private fun release() {
        hostLifecycleOwner = null
        webViewListener = null
        webChromeClient = null
        webViewClient = null
        (parent as? ViewGroup)?.removeView(this)
        destroy()
    }

    companion object {
        private const val TAG = "SH_WebView"
    }
}