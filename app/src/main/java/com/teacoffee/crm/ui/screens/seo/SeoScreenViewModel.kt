package com.teacoffee.crm.ui.screens.seo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacoffee.crm.data.local.entity.SeoKeywordEntity
import com.teacoffee.crm.data.repository.SeoKeywordRepository
import com.teacoffee.crm.util.AiEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeoState(
    val keywords: List<SeoKeywordEntity> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val aiSuggestions: List<AiEngine.KeywordSuggestion> = emptyList(),
    val isGenerating: Boolean = false,
    val contentResults: String = ""
)

@HiltViewModel
class SeoScreenViewModel @Inject constructor(
    private val keywordRepository: SeoKeywordRepository,
    private val aiEngine: AiEngine
) : ViewModel() {

    private val _state = MutableStateFlow(SeoState())
    val state: StateFlow<SeoState> = _state.asStateFlow()

    init {
        loadKeywords()
    }

    private fun loadKeywords() {
        viewModelScope.launch {
            combine(
                _state.map { it.selectedCategory },
                _state.map { it.searchQuery }
            ) { category, query ->
                when {
                    query.isNotBlank() -> keywordRepository.searchKeywords(query)
                    category != null -> keywordRepository.getKeywordsByCategory(category)
                    else -> keywordRepository.getAllKeywords()
                }
            }.flatMapLatest { it }.collect { keywords ->
                _state.update { it.copy(keywords = keywords, isLoading = false) }
            }
        }
    }

    fun selectCategory(category: String?) {
        _state.update { it.copy(selectedCategory = category, isLoading = true) }
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query, isLoading = true) }
    }

    fun toggleTracking(keywordId: Long, tracked: Boolean) {
        viewModelScope.launch {
            keywordRepository.toggleTracking(keywordId, tracked)
        }
    }

    fun generateAiKeywords() {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            val suggestions = aiEngine.generateSeoKeywords(
                businessType = "Tea and Coffee Products Business",
                products = listOf("Tea Premix", "Coffee Premix", "Nescafe Premix", "Vending Machines"),
                targetMarkets = listOf("Local", "National", "International")
            )
            suggestions.forEach { suggestion ->
                keywordRepository.insertKeyword(
                    SeoKeywordEntity(
                        keyword = suggestion.keyword,
                        searchVolume = suggestion.volume,
                        competition = suggestion.competition,
                        difficulty = suggestion.difficulty,
                        source = "AI_GENERATED",
                        category = "PRODUCT"
                    )
                )
            }
            _state.update { it.copy(isGenerating = false, aiSuggestions = suggestions) }
        }
    }

    fun generateSocialContent() {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            val content = aiEngine.generateSocialMediaContent(
                productName = "Premium Tea & Coffee",
                platform = "Instagram",
                targetAudience = "Cafes, Offices, Restaurants, Hotel Owners",
                contentType = "Promotional Post"
            )
            _state.update {
                it.copy(
                    isGenerating = false,
                    contentResults = "${content.caption}\n\n${content.hashtags.joinToString(" ")}"
                )
            }
        }
    }
}
