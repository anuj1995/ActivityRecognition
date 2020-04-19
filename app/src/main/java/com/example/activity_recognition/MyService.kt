package com.example.activity_recognition

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Binder

class LocalService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): LocalService = this@LocalService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

}
