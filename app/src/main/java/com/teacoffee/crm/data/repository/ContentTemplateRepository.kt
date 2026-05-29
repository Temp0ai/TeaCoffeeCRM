package com.teacoffee.crm.data.repository

import com.teacoffee.crm.data.local.dao.ContentTemplateDao
import com.teacoffee.crm.data.local.entity.ContentTemplateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentTemplateRepository @Inject constructor(
    private val dao: ContentTemplateDao
) {
    fun getAllTemplates(): Flow<List<ContentTemplateEntity>> = dao.getAllTemplates()
    fun getTemplatesByType(type: String): Flow<List<ContentTemplateEntity>> = dao.getTemplatesByType(type)
    suspend fun incrementUsage(id: Long) = dao.incrementUsage(id)
}
