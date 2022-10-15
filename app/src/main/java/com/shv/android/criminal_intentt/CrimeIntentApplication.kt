package com.shv.android.criminal_intentt

import android.app.Application

class CrimeIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialization(this)
    }
}