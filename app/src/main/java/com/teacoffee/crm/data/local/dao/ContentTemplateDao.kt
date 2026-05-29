package com.teacoffee.crm.data.local.dao

import androidx.room.*
import com.teacoffee.crm.data.local.entity.ContentTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentTemplateDao {
    @Query("SELECT * FROM content_templates ORDER BY usageCount DESC")
    fun getAllTemplates(): Flow<List<ContentTemplateEntity>>

    @Query("SELECT * FROM content_templates WHERE type = :type ORDER BY usageCount DESC")
    fun getTemplatesByType(type: String): Flow<List<ContentTemplateEntity>>

    @Query("SELECT * FROM content_templates WHERE platform = :platform ORDER BY usageCount DESC")
    fun getTemplatesByPlatform(platform: String): Flow<List<ContentTemplateEntity>>

    @Query("SELECT * FROM content_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): ContentTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ContentTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<ContentTemplateEntity>): List<Long>

    @Update
    suspend fun updateTemplate(template: ContentTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: ContentTemplateEntity)

    @Query("UPDATE content_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)
}
