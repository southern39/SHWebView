/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebviewlib.utils

import android.app.Application

object ContextHolder {

    lateinit var application: Application
        private set

    fun init(application: Application) {
        this.application = application
    }

}