package com.teacoffee.crm.data.local.dao

import androidx.room.*
import com.teacoffee.crm.data.local.entity.SeoKeywordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeoKeywordDao {
    @Query("SELECT * FROM seo_keywords ORDER BY searchVolume DESC")
    fun getAllKeywords(): Flow<List<SeoKeywordEntity>>

    @Query("SELECT * FROM seo_keywords WHERE category = :category ORDER BY searchVolume DESC")
    fun getKeywordsByCategory(category: String): Flow<List<SeoKeywordEntity>>

    @Query("SELECT * FROM seo_keywords WHERE isTracked = 1 ORDER BY searchVolume DESC")
    fun getTrackedKeywords(): Flow<List<SeoKeywordEntity>>

    @Query("SELECT * FROM seo_keywords WHERE keyword LIKE '%' || :query || '%'")
    fun searchKeywords(query: String): Flow<List<SeoKeywordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: SeoKeywordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeywords(keywords: List<SeoKeywordEntity>)

    @Update
    suspend fun updateKeyword(keyword: SeoKeywordEntity)

    @Delete
    suspend fun deleteKeyword(keyword: SeoKeywordEntity)

    @Query("UPDATE seo_keywords SET isTracked = :tracked WHERE id = :id")
    suspend fun toggleTracking(id: Long, tracked: Boolean)
}
