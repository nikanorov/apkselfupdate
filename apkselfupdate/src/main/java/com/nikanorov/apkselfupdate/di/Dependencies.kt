package com.nikanorov.apkselfupdate.di

import com.nikanorov.apkselfupdate.internal.API
import com.nikanorov.apkselfupdate.internal.Downloader
import com.nikanorov.apkselfupdate.internal.System

internal interface Dependencies {
    val api: API
    val system: System
    val downloader: Downloader
}
