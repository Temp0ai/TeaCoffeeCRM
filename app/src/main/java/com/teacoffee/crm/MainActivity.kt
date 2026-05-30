package com.teacoffee.crm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.teacoffee.crm.ui.navigation.AppNavigation
import com.teacoffee.crm.ui.theme.TeaCoffeeCRMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TeaCoffeeCRMTheme {
                AppNavigation()
            }
        }
    }
}
