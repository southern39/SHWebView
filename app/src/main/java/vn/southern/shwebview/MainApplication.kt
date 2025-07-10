/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebview

import android.app.Application
import vn.southern.shwebviewlib.WebViewInitTask
import vn.southern.shwebviewlib.utils.ContextHolder

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextHolder.init(this)
        WebViewInitTask.init(this)
    }
}