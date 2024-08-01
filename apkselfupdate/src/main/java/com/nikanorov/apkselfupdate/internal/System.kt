package com.nikanorov.apkselfupdate.internal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import java.io.File


internal class System(private val context: Context) {
    internal val currentVersion by lazy { context.currentAppVersionCode() }

    internal fun installApk(file: File) {
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val type = "application/vnd.android.package-archive"
                val downloadedApk =
                    FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)

                setDataAndType(downloadedApk, type)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        }
    }

    private fun Context.currentAppVersionCode(): Long? =
        try {
            val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
}