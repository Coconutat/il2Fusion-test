package com.tools.il2fusion.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.tools.il2fusion.BuildConfig
import java.io.File

sealed interface ApkInstallResult {
    data object LaunchedInstaller : ApkInstallResult
    data object PermissionRequired : ApkInstallResult
    data class Failure(val message: String) : ApkInstallResult
}

class ApkInstaller(
    context: Context
) {
    private val appContext = context.applicationContext

    fun installApk(file: File): ApkInstallResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !appContext.packageManager.canRequestPackageInstalls()
            ) {
                openUnknownSourcesSettings()
                ApkInstallResult.PermissionRequired
            } else {
                val contentUri = FileProvider.getUriForFile(
                    appContext,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                appContext.startActivity(installIntent)
                ApkInstallResult.LaunchedInstaller
            }
        } catch (throwable: Throwable) {
            ApkInstallResult.Failure(throwable.message ?: "Failed to launch installer")
        }
    }

    fun openUnknownSourcesSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }
}
