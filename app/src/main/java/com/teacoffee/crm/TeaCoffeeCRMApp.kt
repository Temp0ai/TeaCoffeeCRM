package com.teacoffee.crm

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.teacoffee.crm.data.local.AppDatabase
import com.teacoffee.crm.util.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TeaCoffeeCRMApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        try {
            appScope.launch {
                try {
                    DatabaseInitializer.initialize(this@TeaCoffeeCRMApp, AppDatabase.getDatabase(this@TeaCoffeeCRMApp))
                } catch (e: Exception) {
                    Log.e("TeaCoffeeCRM", "Database init failed: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TeaCoffeeCRM", "Application.onCreate failed: ${e.message}", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
