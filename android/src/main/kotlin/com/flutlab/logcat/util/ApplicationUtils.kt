package com.flutlab.logcat.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object ApplicationUtils {
    private fun getApplicationInfo(context: Context, packageName: String): ApplicationInfo? {
        return try {
            context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun isApplicationInstalled(context: Context, packageName: String) =
            getApplicationInfo(context, packageName) != null
}