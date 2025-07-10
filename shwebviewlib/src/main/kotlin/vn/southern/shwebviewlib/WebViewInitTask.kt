/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebviewlib

import android.app.Application
import android.content.Context
import android.util.Log
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk

object WebViewInitTask {
    private const val TAG = "SH_WebViewInitTask"

    fun init(application: Application) {
        initWebView(application)
        WebViewCacheHolder.init(application)
        WebViewInterceptRequestProxy.init(application)
    }

    private fun initWebView(context: Context) {
        QbSdk.setDownloadWithoutWifi(true)
        val map = mutableMapOf<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_PRIVATE_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        val callback = object : QbSdk.PreInitCallback {
            override fun onViewInitFinished(isX5Core: Boolean) {
                Log.d(TAG, "onViewInitFinished: $isX5Core")
            }

            override fun onCoreInitFinished() {
                Log.d(TAG, "onCoreInitFinished")
            }
        }
        QbSdk.initX5Environment(context, callback)
    }
}