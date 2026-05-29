package com.teacoffee.crm.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.teacoffee.crm.data.local.AppDatabase
import com.teacoffee.crm.util.GmailSyncWorker
import com.teacoffee.crm.util.SeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val gmailAccessToken: String = "",
    val gmailQuery: String = "subject:(inquiry OR quote OR order OR price OR product OR tea OR coffee)",
    val whatsappAccessToken: String = "",
    val whatsappPhoneNumberId: String = "",
    val whatsappApiKey: String = "",
    val seoApiKey: String = "",
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "Never",
    val syncStatus: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        seedDatabase()
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            SeedData.seedIfEmpty(database)
        }
    }

    fun updateGmailToken(token: String) {
        _state.update { it.copy(gmailAccessToken = token) }
    }

    fun updateGmailQuery(query: String) {
        _state.update { it.copy(gmailQuery = query) }
    }

    fun updateWhatsAppToken(token: String) {
        _state.update { it.copy(whatsappAccessToken = token) }
    }

    fun updateWhatsAppPhoneNumberId(id: String) {
        _state.update { it.copy(whatsappPhoneNumberId = id) }
    }

    fun updateSeoApiKey(key: String) {
        _state.update { it.copy(seoApiKey = key) }
    }

    fun syncGmail(context: Context) {
        val state = _state.value
        if (state.gmailAccessToken.isBlank()) {
            _state.update { it.copy(syncStatus = "Please configure Gmail access token first") }
            return
        }

        _state.update { it.copy(isSyncing = true, syncStatus = "Syncing Gmail...") }

        val workRequest = GmailSyncWorker.createRequest(
            accessToken = state.gmailAccessToken,
            query = state.gmailQuery
        )

        WorkManager.getInstance(context).enqueue(workRequest)

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSyncing = false,
                    syncStatus = "Gmail sync started in background",
                    lastSyncTime = java.text.SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())
                )
            }
        }
    }

    fun clearAllData(context: Context) {
        viewModelScope.launch {
            context.deleteDatabase("tea_coffee_crm_db")
            _state.update {
                it.copy(
                    syncStatus = "All data cleared. Restarting app..."
                )
            }
        }
    }
}
