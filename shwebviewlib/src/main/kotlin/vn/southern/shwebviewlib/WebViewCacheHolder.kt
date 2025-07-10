/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebviewlib

import android.app.Application
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Looper
import android.util.Log
import java.util.Stack

object WebViewCacheHolder {

    private val webViewCacheStack = Stack<SHWebView>()

    private const val CACHE_WEB_VIEW_MAX_NUM = 4

    private lateinit var application: Application

    private const val TAG = "SH_WebViewCacheHolder"

    fun init(application: Application) {
        this.application = application
        prepareWebView()
    }

    fun prepareWebView() {
        if (webViewCacheStack.size < CACHE_WEB_VIEW_MAX_NUM) {
            Looper.myQueue().addIdleHandler {
                Log.d(TAG, "WebViewCacheStack Size: ${webViewCacheStack.size}")
                if (webViewCacheStack.size < CACHE_WEB_VIEW_MAX_NUM) {
                    webViewCacheStack.push(createSHWebView(MutableContextWrapper(application)))
                }
                false
            }
        }
    }

    fun acquireWebViewInternal(context: Context): SHWebView {
        if (webViewCacheStack.empty()) {
            return createSHWebView(context)
        }
        val webView = webViewCacheStack.pop()
        val contextWrapper = webView.context as MutableContextWrapper
        contextWrapper.baseContext = context
        return webView
    }

    private fun createSHWebView(context: Context): SHWebView {
        return SHWebView(context)
    }
}