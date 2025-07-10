/**
 * Author: southern
 * Date: 9/7/25
 */

package vn.southern.shwebviewlib.utils

import android.widget.Toast

fun showToast(msg: String) {
    Toast.makeText(ContextHolder.application, msg, Toast.LENGTH_SHORT).show()
}