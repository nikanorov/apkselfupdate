package com.nikanorov.apkselfupdate.di

import android.content.Context
import com.nikanorov.apkselfupdate.internal.API
import com.nikanorov.apkselfupdate.internal.Downloader
import com.nikanorov.apkselfupdate.internal.System

internal open class DependenciesImpl (appContext: Context, updateCheckUrl: String) : Dependencies {
    override val api = API(updateCheckUrl)
    override val system = System(appContext)
    override val downloader = Downloader(appContext)
}