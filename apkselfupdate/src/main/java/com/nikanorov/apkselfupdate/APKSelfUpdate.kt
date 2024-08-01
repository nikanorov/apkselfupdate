package com.nikanorov.apkselfupdate

import android.content.Context
import android.webkit.URLUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.nikanorov.apkselfupdate.di.DependenciesImpl
import com.nikanorov.apkselfupdate.internal.API
import com.nikanorov.apkselfupdate.internal.Downloader
import com.nikanorov.apkselfupdate.internal.System
import com.nikanorov.apkselfupdate.util.firstSuccessResponse
import com.nikanorov.apkselfupdate.util.isOneOf
import com.nikanorov.apkselfupdate.value.APKSelfUpdateState
import com.nikanorov.apkselfupdate.value.AppUpdateCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
class APKSelfUpdate internal constructor(dependencies: DependenciesImpl) {

    private val api: API = dependencies.api
    private val system: System = dependencies.system
    private val downloader: Downloader = dependencies.downloader

    private val _state = MutableStateFlow<APKSelfUpdateState>(APKSelfUpdateState.Unknown)
    val state: StateFlow<APKSelfUpdateState> = _state

    fun checkForUpdate(
        lifecycleOwner: LifecycleOwner,
        callback: (AppUpdateCheckResult) -> Unit = {},
    ) {

        val currentState = state.value
        if (!currentState.isInCanDownloadState()) return

        _state.value = APKSelfUpdateState.Loading
        lifecycleOwner.lifecycleScope.launch {
            val latestVersionDetails = api.updateInfo().firstSuccessResponse()?.data
            val needUpdate = latestVersionDetails?.let {
                val currentVersionCode = system.currentVersion
                currentVersionCode != null && it.versionCode > currentVersionCode
            } ?: false

            latestVersionDetails?.let {
                callback(AppUpdateCheckResult(needUpdate, it.apkUrl))
                _state.value = if (needUpdate) {
                    APKSelfUpdateState.UpdateAvailable(it)
                } else {
                    APKSelfUpdateState.UpdateUnavailable
                }
            } ?: run {
                _state.value = APKSelfUpdateState.UpdateUnavailable
            }
        }
    }

    fun downloadUpdate(
        lifecycleOwner: LifecycleOwner,
        percent: (Int?) -> Unit = {},
        downloaded: (String) -> Unit = {},
    ) {
        val currentState = state.value as? APKSelfUpdateState.UpdateAvailable ?: return

        _state.value = APKSelfUpdateState.Downloading()

        lifecycleOwner.lifecycleScope.launch {
            val updateUrl = currentState.updateInfo.apkUrl

            try {
                downloader.downloadFile(updateUrl, percentUpdateCallback = {
                    _state.value = APKSelfUpdateState.Downloading(it)
                    percent(it)
                }, downloadCompletedCallback = {
                    _state.value = APKSelfUpdateState.Downloaded(it)
                    downloaded(it)
                })
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = APKSelfUpdateState.Error(e.message, e)
            }
        }
    }

    fun installUpdate(
    ) {
        val currentState = state.value as? APKSelfUpdateState.Downloaded ?: return
        system.installApk(File(currentState.filePath))
    }

    fun nextStep(lifecycleOwner: LifecycleOwner) {
        when (state.value) {
            is APKSelfUpdateState.Error, is APKSelfUpdateState.Unknown, is APKSelfUpdateState.UpdateUnavailable -> checkForUpdate(
                lifecycleOwner
            )

            is APKSelfUpdateState.UpdateAvailable -> downloadUpdate(lifecycleOwner)
            is APKSelfUpdateState.Downloaded -> installUpdate()
            else -> {}
        }
    }


    companion object {

        @Volatile
        private var _instance: APKSelfUpdate? = null
        val instance: APKSelfUpdate
            get() = _instance ?: synchronized(this) { _instance }
            ?: error("APKSelfUpdate is not initialized")

        private fun init(apkselfupdate: APKSelfUpdate) {
            synchronized(this) {
                check(_instance == null) { "APKSelfUpdate already initialized" }
                _instance = apkselfupdate
            }
        }

        fun init(
            context: Context, apkSelfUpdateOptionsBuilder: APKSelfUpdateOptionsBuilder.() -> Unit = {}
        ) {
            val builder = APKSelfUpdateOptionsBuilder().apply(apkSelfUpdateOptionsBuilder)
            check(URLUtil.isValidUrl(builder.updateCheckUrl)) { "Update check URL is mandatory" }
            init(
                APKSelfUpdate(DependenciesImpl(context, builder.updateCheckUrl))
            )
        }
    }

    private fun APKSelfUpdateState.isInCanDownloadState(): Boolean = this.isOneOf(
        APKSelfUpdateState.Error::class,
        APKSelfUpdateState.Unknown::class,
        APKSelfUpdateState.UpdateUnavailable::class
    )
}

