package com.example.flav_pof

import android.app.Activity
import android.util.Patterns
import android.widget.Toast


class Util {

    fun Util() { /* */
    }

    fun showToast(activity: Activity?, msg: String?) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    fun isStorageUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url)
            .matches() && url.contains("https://firebasestorage.googleapis.com/v0/b/flavmvp-9fe0d.appspot.com/o/posts")
    }

    fun storageUrlToName(url: String): String? {
        return url.split("\\?").toTypedArray()[0].split("%2F").toTypedArray()[url.split("\\?")
            .toTypedArray()[0].split("%2F").toTypedArray().size - 1]
    }


}