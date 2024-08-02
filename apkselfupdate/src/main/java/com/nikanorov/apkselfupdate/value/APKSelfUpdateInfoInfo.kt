package com.nikanorov.apkselfupdate.value

import kotlinx.serialization.Serializable

@Serializable
internal data class APKSelfUpdateInfoResponse (val updates: List<APKSelfUpdateInfoUpdate>)

@Serializable
data class APKSelfUpdateInfoUpdate(val versionCode: Long, val versionNumber: String, val changelog: String, val apkUrl: String)