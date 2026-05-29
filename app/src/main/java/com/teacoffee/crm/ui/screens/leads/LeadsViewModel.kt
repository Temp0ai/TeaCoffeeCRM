package com.teacoffee.crm.ui.screens.leads

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacoffee.crm.data.local.entity.CategoryEntity
import com.teacoffee.crm.data.local.entity.LeadEntity
import com.teacoffee.crm.data.repository.CategoryRepository
import com.teacoffee.crm.data.repository.LeadRepository
import com.teacoffee.crm.util.AiEngine
import com.teacoffee.crm.util.ExcelImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeadsState(
    val leads: List<LeadEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategory: Long? = null,
    val selectedClientType: String? = null,
    val selectedStatus: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val importResult: ExcelImporter.ImportResult? = null,
    val showImportDialog: Boolean = false
)

@HiltViewModel
class LeadsViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val categoryRepository: CategoryRepository,
    private val excelImporter: ExcelImporter,
    private val aiEngine: AiEngine
) : ViewModel() {

    private val _state = MutableStateFlow(LeadsState())
    val state: StateFlow<LeadsState> = _state.asStateFlow()

    init {
        loadCategories()
        loadLeads()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadLeads() {
        viewModelScope.launch {
            combine(
                _state.map { it.selectedCategory },
                _state.map { it.selectedClientType },
                _state.map { it.selectedStatus },
                _state.map { it.searchQuery }
            ) { cat, clientType, status, query ->
                when {
                    query.isNotBlank() -> leadRepository.searchLeads(query)
                    cat != null -> leadRepository.getLeadsByCategory(cat)
                    clientType != null -> leadRepository.getLeadsByClientType(clientType)
                    status != null -> leadRepository.getLeadsByStatus(status)
                    else -> leadRepository.getAllLeads()
                }
            }.flatMapLatest { it }.collect { leads ->
                _state.update { it.copy(leads = leads, isLoading = false) }
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _state.update {
            it.copy(
                selectedCategory = categoryId,
                selectedClientType = null,
                selectedStatus = null,
                searchQuery = ""
            )
        }
    }

    fun selectClientType(clientType: String?) {
        _state.update {
            it.copy(
                selectedClientType = clientType,
                selectedCategory = null,
                selectedStatus = null,
                searchQuery = ""
            )
        }
    }

    fun selectStatus(status: String?) {
        _state.update {
            it.copy(
                selectedStatus = status,
                selectedCategory = null,
                selectedClientType = null,
                searchQuery = ""
            )
        }
    }

    fun search(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                selectedCategory = null,
                selectedClientType = null,
                selectedStatus = null
            )
        }
    }

    fun updateLeadStatus(leadId: Long, status: String) {
        viewModelScope.launch {
            leadRepository.updateLeadStatus(leadId, status)
        }
    }

    fun assignCategory(leadId: Long, categoryId: Long?) {
        viewModelScope.launch {
            leadRepository.updateLeadCategory(leadId, categoryId)
        }
    }

    fun importExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = excelImporter.importFromUri(context, uri)
            if (result.leads.isNotEmpty()) {
                leadRepository.insertLeads(result.leads)
            }
            _state.update { it.copy(importResult = result, showImportDialog = true) }
        }
    }

    fun dismissImportDialog() {
        _state.update { it.copy(importResult = null, showImportDialog = false) }
    }

    fun deleteLead(lead: LeadEntity) {
        viewModelScope.launch {
            leadRepository.deleteLead(lead)
        }
    }
}
