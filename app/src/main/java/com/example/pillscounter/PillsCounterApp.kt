package com.example.pillscounter

import android.app.Application
import com.example.pillscounter.data.AppDatabase

class PillsCounterApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PillsCounterApp
            private set
    }
}
