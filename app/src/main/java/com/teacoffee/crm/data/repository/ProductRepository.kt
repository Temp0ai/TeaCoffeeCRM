package com.teacoffee.crm.data.repository

import com.teacoffee.crm.data.local.dao.ProductDao
import com.teacoffee.crm.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(category)

    fun getWhatsappCatalogProducts(): Flow<List<ProductEntity>> =
        productDao.getWhatsappCatalogProducts()

    suspend fun insertProduct(product: ProductEntity): Long =
        productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) =
        productDao.deleteProduct(product)
}
