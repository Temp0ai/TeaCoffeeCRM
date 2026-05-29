package com.teacoffee.crm.ui.screens.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacoffee.crm.data.local.entity.CampaignEntity
import com.teacoffee.crm.data.local.entity.CategoryEntity
import com.teacoffee.crm.data.local.entity.LeadEntity
import com.teacoffee.crm.data.repository.CampaignRepository
import com.teacoffee.crm.data.repository.CategoryRepository
import com.teacoffee.crm.data.repository.LeadRepository
import com.teacoffee.crm.util.AiEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeadDetailState(
    val lead: LeadEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val isGeneratingMessage: Boolean = false,
    val generatedMessage: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class LeadDetailViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val categoryRepository: CategoryRepository,
    private val campaignRepository: CampaignRepository,
    private val aiEngine: AiEngine
) : ViewModel() {

    private val _state = MutableStateFlow(LeadDetailState())
    val state: StateFlow<LeadDetailState> = _state.asStateFlow()

    fun loadLead(leadId: Long) {
        viewModelScope.launch {
            val lead = leadRepository.getLeadById(leadId)
            categoryRepository.getAllCategories().collect { categories ->
                _state.update {
                    it.copy(lead = lead, categories = categories, isLoading = false)
                }
            }
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            _state.value.lead?.let { lead ->
                leadRepository.updateLeadStatus(lead.id, status)
                _state.update { it.copy(lead = it.lead?.copy(status = status)) }
            }
        }
    }

    fun assignCategory(categoryId: Long?) {
        viewModelScope.launch {
            _state.value.lead?.let { lead ->
                leadRepository.updateLeadCategory(lead.id, categoryId)
                _state.update { it.copy(lead = it.lead?.copy(categoryId = categoryId)) }
            }
        }
    }

    fun generateAiFollowUp() {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingMessage = true) }
            try {
                val lead = _state.value.lead ?: return@launch
                val message = aiEngine.generateFollowUpMessage(
                    lead = lead,
                    context = "Follow up on ${lead.productRequirement} inquiry"
                )
                _state.update { it.copy(generatedMessage = message, isGeneratingMessage = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isGeneratingMessage = false) }
            }
        }
    }

    fun saveAsCampaign(name: String) {
        viewModelScope.launch {
            val lead = _state.value.lead ?: return@launch
            val message = _state.value.generatedMessage
            if (message.isBlank()) return@launch
            campaignRepository.insertCampaign(
                CampaignEntity(
                    name = name,
                    messageTemplate = message,
                    categoryIds = lead.categoryId?.toString() ?: "",
                    clientTypes = lead.clientType
                )
            )
        }
    }
}
