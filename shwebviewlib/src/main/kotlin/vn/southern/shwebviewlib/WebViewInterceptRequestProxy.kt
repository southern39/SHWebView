/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebviewlib

import android.app.Application
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

object WebViewInterceptRequestProxy {
    private const val TAG = "SH_WebViewInterceptRequestProxy"

    private lateinit var application: Application

    private val webViewResourceCacheDir by lazy {
        File(application.cacheDir, "SHWebView")
    }

    private var allowInspect: Boolean = false

    private var okHttpClient: OkHttpClient? = null

    private fun initOkHttpClient() {
        val builder =  OkHttpClient.Builder().cache(Cache(webViewResourceCacheDir, 600L * 1024 * 1924))
            .followRedirects(false)
            .followSslRedirects(false)
            .addNetworkInterceptor(getChuckerInterceptor(application))
            .addNetworkInterceptor(getWebViewCacheInterceptor())
        if (allowInspect) {
            builder.addNetworkInterceptor(getChuckerInterceptor(application))
        }
        okHttpClient = builder.build()
    }

    private fun getChuckerInterceptor(application: Application): Interceptor {
        return ChuckerInterceptor.Builder(application)
            .collector(ChuckerCollector(application))
            .maxContentLength(250000L)
            .alwaysReadResponseBody(true)
            .build()
    }

    private fun getWebViewCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            response.newBuilder()
                .removeHeader("pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "max-age=" + (360L * 24 * 60 * 60))
                .build()
        }
    }

    fun init(application: Application) {
        this.application = application
    }

    fun shouldInterceptRequest(
        webResourceRequest: WebResourceRequest,
        allowInspect: Boolean = false
    ): WebResourceResponse? {
        this.allowInspect = allowInspect
        if (toProxy(webResourceRequest)) {
            return getHttpResource(webResourceRequest)
        }
        return null
    }

    private fun toProxy(webResourceRequest: WebResourceRequest): Boolean {
        if (webResourceRequest.isForMainFrame) {
            return false
        }
        val url = webResourceRequest.url ?: return false
        if (!webResourceRequest.method.equals("GET", true)) {
            return false
        }
        if (url.scheme == "https" || url.scheme == "http") {
            val urlString = url.toString()
            if (urlString.endsWith(".js", true) ||
                urlString.endsWith(".css", true) ||
                urlString.endsWith(".jpg", true) ||
                urlString.endsWith(".png", true) ||
                urlString.endsWith(".webp", true) ||
                urlString.endsWith(".awebp", true)
            ) {
                return true
            }
        }
        return false
    }

    private fun getHttpResource(webResourceRequest: WebResourceRequest): WebResourceResponse? {
        try {
            val url = webResourceRequest.url.toString()
            val requestBuilder = Request.Builder()
                .url(url)
                .method(webResourceRequest.method, null)
            webResourceRequest.requestHeaders?.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }
            if (okHttpClient == null) initOkHttpClient()
            okHttpClient?.let {
                val response = it
                    .newCall(requestBuilder.build())
                    .execute()
                val body = response.body
                val code = response.code
                if (body == null || code != 200) {
                    return null
                }
                val mimeType = response.header("content-type", body.contentType()?.type)
                val encoding = response.header("content-encoding", "utf-8")
                val responseHeaders = buildMap {
                    response.headers.map {
                        put(it.first, it.second)
                    }
                }
                var message = response.message
                if (message.isBlank()) {
                    message = "OK"
                }
                val resourceResponse = WebResourceResponse(mimeType, encoding, body.byteStream())
                resourceResponse.responseHeaders = responseHeaders
                resourceResponse.setStatusCodeAndReasonPhrase(code, message)
                return resourceResponse
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    private fun getAssetsImage(url: String): WebResourceResponse? {
        if (url.contains(".jpg")) {
            try {
                val inputStream = application.assets.open("ic_launcher.webp")
                return WebResourceResponse(
                    "image/webp",
                    "utf-8", inputStream
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Throwable: $e")
            }
        }
        return null
    }
}