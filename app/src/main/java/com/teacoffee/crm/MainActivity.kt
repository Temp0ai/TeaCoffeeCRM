package com.teacoffee.crm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.teacoffee.crm.ui.navigation.AppNavigation
import com.teacoffee.crm.ui.theme.TeaCoffeeCRMTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("[$ts] MainActivity.onCreate() START\n")
        Log.d("TeaCoffeeCRM", "MainActivity.onCreate() START")

        super.onCreate(savedInstanceState)

        File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("[$ts] super.onCreate() done\n")
        Log.d("TeaCoffeeCRM", "super.onCreate() done")

        try {
            enableEdgeToEdge()
            File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("[$ts] enableEdgeToEdge() done\n")
        } catch (e: Exception) {
            val msg = "[$ts] enableEdgeToEdge ERROR: ${e.message}\n${Log.getStackTraceString(e)}"
            Log.e("TeaCoffeeCRM", msg)
            File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("$msg\n")
        }

        try {
            setContent {
                TeaCoffeeCRMTheme {
                    AppNavigation()
                }
            }
            File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("[$ts] setContent() done\n")
            Log.d("TeaCoffeeCRM", "setContent() done")
        } catch (e: Exception) {
            val msg = "[$ts] setContent ERROR: ${e.message}\n${Log.getStackTraceString(e)}"
            Log.e("TeaCoffeeCRM", msg)
            File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("$msg\n")
        }

        File("/sdcard/Download/TeaCoffeeCRM_startup.log").appendText("[$ts] MainActivity.onCreate() END\n")
    }
}
