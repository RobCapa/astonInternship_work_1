package com.example.work_1

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
    }
}