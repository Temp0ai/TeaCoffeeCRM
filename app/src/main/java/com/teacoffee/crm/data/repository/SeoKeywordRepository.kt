package com.teacoffee.crm.data.repository

import com.teacoffee.crm.data.local.dao.SeoKeywordDao
import com.teacoffee.crm.data.local.entity.SeoKeywordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeoKeywordRepository @Inject constructor(
    private val dao: SeoKeywordDao
) {
    fun getAllKeywords(): Flow<List<SeoKeywordEntity>> = dao.getAllKeywords()
    fun getKeywordsByCategory(category: String): Flow<List<SeoKeywordEntity>> = dao.getKeywordsByCategory(category)
    fun searchKeywords(query: String): Flow<List<SeoKeywordEntity>> = dao.searchKeywords(query)
    suspend fun insertKeyword(keyword: SeoKeywordEntity) = dao.insertKeyword(keyword)
    suspend fun toggleTracking(id: Long, tracked: Boolean) = dao.toggleTracking(id, tracked)
}
