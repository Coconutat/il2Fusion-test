package com.tools.il2fusion.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.tools.il2fusion.BuildConfig
import com.tools.il2fusion.R
import java.io.File
import java.security.MessageDigest

sealed interface ApkInstallResult {
    data object LaunchedInstaller : ApkInstallResult
    data object PermissionRequired : ApkInstallResult
    data class Failure(val message: String) : ApkInstallResult
}

class ApkInstaller(
    context: Context
) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    fun installApk(file: File): ApkInstallResult {
        return try {
            val validationFailure = validateArchiveForUpdate(file)
            if (validationFailure != null) {
                return ApkInstallResult.Failure(validationFailure)
            }
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

    @Suppress("DEPRECATION")
    private fun validateArchiveForUpdate(file: File): String? {
        val archiveInfo = packageManager.getPackageArchiveInfo(
            file.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES
        ) ?: return appContext.getString(R.string.settings_update_invalid_apk)

        if (archiveInfo.packageName != appContext.packageName) {
            return appContext.getString(R.string.settings_update_package_mismatch)
        }

        val installedInfo = packageManager.getPackageInfo(
            appContext.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )

        val installedDigests = installedInfo.signingDigests()
        val archiveDigests = archiveInfo.signingDigests()
        if (installedDigests.isNotEmpty() && archiveDigests.isNotEmpty() && installedDigests != archiveDigests) {
            return appContext.getString(R.string.settings_update_signature_mismatch)
        }

        return null
    }

    private fun PackageInfo.signingDigests(): Set<String> {
        val info = signingInfo ?: return emptySet()
        val signatures = if (info.hasMultipleSigners()) {
            info.apkContentsSigners
        } else {
            info.signingCertificateHistory
        }
        return signatures
            ?.map { it.sha256Digest() }
            ?.toSet()
            .orEmpty()
    }

    private fun Signature.sha256Digest(): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
