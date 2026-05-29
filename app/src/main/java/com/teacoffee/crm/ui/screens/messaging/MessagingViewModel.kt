package com.teacoffee.crm.ui.screens.messaging

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacoffee.crm.data.local.entity.*
import com.teacoffee.crm.data.repository.*
import com.teacoffee.crm.util.AiEngine
import com.teacoffee.crm.util.WhatsAppWebAutomation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagingState(
    val leads: List<LeadEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val campaigns: List<CampaignEntity> = emptyList(),
    val templates: List<ContentTemplateEntity> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedClientTypes: Set<String> = emptySet(),
    val selectedLeadIds: Set<Long> = emptySet(),
    val messageContent: String = "",
    val campaignName: String = "",
    val isAiGenerating: Boolean = false,
    val sendProgress: Pair<Int, Int>? = null,
    val waAuthState: WhatsAppWebAutomation.AuthState = WhatsAppWebAutomation.AuthState.DISCONNECTED,
    val isLoading: Boolean = true
)

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val categoryRepository: CategoryRepository,
    private val campaignRepository: CampaignRepository,
    private val templateRepository: ContentTemplateRepository,
    private val messageRepository: MessageRepository,
    private val aiEngine: AiEngine,
    private val waAutomation: WhatsAppWebAutomation
) : ViewModel() {

    private val _state = MutableStateFlow(MessagingState())
    val state: StateFlow<MessagingState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                leadRepository.getAllLeads(),
                categoryRepository.getAllCategories(),
                campaignRepository.getAllCampaigns(),
                templateRepository.getAllTemplates()
            ) { leads, categories, campaigns, templates ->
                _state.update {
                    it.copy(
                        leads = leads,
                        categories = categories,
                        campaigns = campaigns,
                        templates = templates,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun toggleCategory(categoryId: Long) {
        _state.update { state ->
            val updated = state.selectedCategoryIds.toMutableSet()
            if (updated.contains(categoryId)) updated.remove(categoryId)
            else updated.add(categoryId)
            state.copy(selectedCategoryIds = updated)
        }
    }

    fun toggleClientType(clientType: String) {
        _state.update { state ->
            val updated = state.selectedClientTypes.toMutableSet()
            if (updated.contains(clientType)) updated.remove(clientType)
            else updated.add(clientType)
            state.copy(selectedClientTypes = updated)
        }
    }

    fun toggleLeadSelection(leadId: Long) {
        _state.update { state ->
            val updated = state.selectedLeadIds.toMutableSet()
            if (updated.contains(leadId)) updated.remove(leadId)
            else updated.add(leadId)
            state.copy(selectedLeadIds = updated)
        }
    }

    fun updateMessageContent(content: String) {
        _state.update { it.copy(messageContent = content) }
    }

    fun updateCampaignName(name: String) {
        _state.update { it.copy(campaignName = name) }
    }

    fun loadTemplate(template: ContentTemplateEntity) {
        _state.update { it.copy(messageContent = template.body) }
        viewModelScope.launch { templateRepository.incrementUsage(template.id) }
    }

    fun generateAiMessage() {
        viewModelScope.launch {
            _state.update { it.copy(isAiGenerating = true) }
            try {
                val filteredLeads = getFilteredLeads()
                val message = if (filteredLeads.isNotEmpty()) {
                    val categories = _state.value.categories
                        .filter { it.id in _state.value.selectedCategoryIds }
                        .joinToString(", ") { it.name }
                    aiEngine.generateBulkMessage(filteredLeads, categories, "promotion")
                } else {
                    aiEngine.generateFollowUpMessage(
                        LeadEntity(name = "Customer", phone = "", email = ""),
                        "Promotional message for tea and coffee products"
                    )
                }
                _state.update { it.copy(messageContent = message, isAiGenerating = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isAiGenerating = false) }
            }
        }
    }

    fun sendBulkMessages(webView: WebView) {
        viewModelScope.launch {
            val leadsToSend = getFilteredLeads().ifEmpty {
                _state.value.leads
            }
            if (leadsToSend.isEmpty() || _state.value.messageContent.isBlank()) return@launch

            val campaignId = if (_state.value.campaignName.isNotBlank()) {
                campaignRepository.insertCampaign(
                    CampaignEntity(
                        name = _state.value.campaignName,
                        messageTemplate = _state.value.messageContent,
                        categoryIds = _state.value.selectedCategoryIds.joinToString(","),
                        clientTypes = _state.value.selectedClientTypes.joinToString(",")
                    )
                )
            } else null

            val result = waAutomation.sendBulkMessages(
                leads = leadsToSend,
                messageContent = _state.value.messageContent,
                webView = webView,
                onProgress = { current, total ->
                    _state.update { it.copy(sendProgress = Pair(current, total)) }
                }
            )

            if (campaignId != null) {
                campaignRepository.updateCampaignStats(
                    campaignId, result.sentCount, result.sentCount, 0, result.failedCount
                )
            }

            leadsToSend.forEach { lead ->
                messageRepository.insertMessage(
                    MessageEntity(
                        leadId = lead.id,
                        content = _state.value.messageContent,
                        type = "WHATSAPP",
                        direction = "SENT",
                        status = if (lead.phone in result.failedNumbers) "FAILED" else "SENT",
                        campaignId = campaignId,
                        isAiGenerated = false
                    )
                )
            }

            _state.update { it.copy(sendProgress = null) }
        }
    }

    fun getWaAuthState() = waAutomation.getAuthState()

    private fun getFilteredLeads(): List<LeadEntity> {
        val state = _state.value
        var leads = state.leads

        if (state.selectedLeadIds.isNotEmpty()) {
            leads = leads.filter { it.id in state.selectedLeadIds }
        }
        if (state.selectedCategoryIds.isNotEmpty()) {
            leads = leads.filter { it.categoryId in state.selectedCategoryIds }
        }
        if (state.selectedClientTypes.isNotEmpty()) {
            leads = leads.filter { it.clientType in state.selectedClientTypes }
        }

        return leads.filter { it.phone.isNotBlank() }
    }
}

