package com.teacoffee.crm.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacoffee.crm.data.local.entity.ProductEntity
import com.teacoffee.crm.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogState(
    val products: List<ProductEntity> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.map { it.selectedCategory }
                .flatMapLatest { category ->
                    if (category != null) productRepository.getProductsByCategory(category)
                    else productRepository.getAllProducts()
                }
                .collect { products ->
                    _state.update { it.copy(products = products, isLoading = false) }
                }
        }
    }

    fun selectCategory(category: String?) {
        _state.update { it.copy(selectedCategory = category, isLoading = true) }
    }

    fun toggleProductActive(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.updateProduct(product.copy(isActive = !product.isActive))
        }
    }
}
