package com.teacoffee.crm.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacoffee.crm.data.repository.LeadRepository
import com.teacoffee.crm.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val totalLeads: Int = 0,
    val newLeads: Int = 0,
    val convertedLeads: Int = 0,
    val leadsThisWeek: Int = 0,
    val messagesSentToday: Int = 0,
    val messagesFailedToday: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            leadRepository.getLeadCount().collect { total ->
                _state.update { it.copy(totalLeads = total) }
            }
        }

        viewModelScope.launch {
            leadRepository.getNewLeadCount().collect { new ->
                _state.update { it.copy(newLeads = new) }
            }
        }

        viewModelScope.launch {
            leadRepository.getConvertedLeadCount().collect { converted ->
                _state.update { it.copy(convertedLeads = converted) }
            }
        }

        viewModelScope.launch {
            val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
            leadRepository.getLeadsCountSince(weekAgo).collect { count ->
                _state.update { it.copy(leadsThisWeek = count) }
            }
        }

        viewModelScope.launch {
            val todayStart = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            combine(
                messageRepository.getSentCountSince(todayStart),
                messageRepository.getFailedCountSince(todayStart)
            ) { sent, failed ->
                _state.update {
                    it.copy(
                        messagesSentToday = sent,
                        messagesFailedToday = failed,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        loadDashboardData()
    }
}
