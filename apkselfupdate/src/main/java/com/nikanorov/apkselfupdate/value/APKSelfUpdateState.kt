package com.nikanorov.apkselfupdate.value

sealed class APKSelfUpdateState {
    data class Downloaded(val filePath: String) : APKSelfUpdateState()
    data class Downloading(val percent: Int? = null) : APKSelfUpdateState()
    data class UpdateAvailable(val updateInfo: APKSelfUpdateInfoUpdate) : APKSelfUpdateState()
    data class Error(val message: String?, val error: Throwable) : APKSelfUpdateState()
    data object UpdateUnavailable : APKSelfUpdateState()
    data object Loading : APKSelfUpdateState()
    data object Unknown : APKSelfUpdateState()
}