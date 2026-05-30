package com.teacoffee.crm

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class TeaCoffeeCRMApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        private const val TAG = "TeaCoffeeCRM"
        private var logFile: File? = null

        init {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    val sw = StringWriter()
                    throwable.printStackTrace(PrintWriter(sw))
                    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    val msg = "[$ts] FATAL CRASH on thread '${thread.name}':\n$sw\n"
                    logFile?.appendText(msg)
                    File("/sdcard/Download/TeaCoffeeCRM_crash.log").appendText(msg)
                } catch (_: Exception) {}
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            }
        }
    }

    override fun attachBaseContext(base: android.content.Context?) {
        super.attachBaseContext(base)
        try {
            logFile = File(filesDir, "crash.log")
            logFile?.writeText("=== TeaCoffeeCRM Log ===\n")
            File("/sdcard/Download/TeaCoffeeCRM_startup.log").writeText("=== Startup Log ===\n")
            log("attachBaseContext OK, filesDir=${filesDir}")
        } catch (e: Exception) {
            Log.e(TAG, "attachBaseContext error", e)
        }
    }

    override fun onCreate() {
        log("Application.onCreate() START")
        super.onCreate()
        log("super.onCreate() done")

        try {
            log("Starting database init...")
            val db = com.teacoffee.crm.data.local.AppDatabase.getDatabase(this)
            log("Database instance obtained: $db")
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    com.teacoffee.crm.util.DatabaseInitializer.initialize(this@TeaCoffeeCRMApp, db)
                    log("Database init DONE")
                } catch (e: Exception) {
                    log("DB_INIT_ERROR: ${e.message}\n${Log.getStackTraceString(e)}")
                }
            }
        } catch (e: Exception) {
            log("ONCREATE_ERROR: ${e.message}\n${Log.getStackTraceString(e)}")
        }

        log("Application.onCreate() END")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun log(msg: String) {
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val line = "[$ts] $msg\n"
        Log.d(TAG, msg)
        try {
            logFile?.appendText(line)
            File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText(line)
        } catch (_: Exception) {}
    }
}
