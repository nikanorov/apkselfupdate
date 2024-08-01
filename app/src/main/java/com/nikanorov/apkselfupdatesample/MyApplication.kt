package com.nikanorov.apkselfupdatesample

import android.app.Application
import com.nikanorov.apkselfupdate.APKSelfUpdate

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        APKSelfUpdate.init(this) {
            updateCheckUrl = TODO("Add update URL here")
        }
    }
}