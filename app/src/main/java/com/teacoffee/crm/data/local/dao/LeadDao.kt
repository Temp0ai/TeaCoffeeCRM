package com.teacoffee.crm.data.local.dao

import androidx.room.*
import com.teacoffee.crm.data.local.entity.LeadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY updatedAt DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE categoryId = :categoryId ORDER BY updatedAt DESC")
    fun getLeadsByCategory(categoryId: Long): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE clientType = :clientType ORDER BY updatedAt DESC")
    fun getLeadsByClientType(clientType: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE status = :status ORDER BY updatedAt DESC")
    fun getLeadsByStatus(status: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun getLeadById(id: Long): LeadEntity?

    @Query("SELECT * FROM leads WHERE email = :email LIMIT 1")
    suspend fun getLeadByEmail(email: String): LeadEntity?

    @Query("SELECT * FROM leads WHERE phone = :phone LIMIT 1")
    suspend fun getLeadByPhone(phone: String): LeadEntity?

    @Query("SELECT * FROM leads WHERE source = :source ORDER BY updatedAt DESC")
    fun getLeadsBySource(source: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    fun searchLeads(query: String): Flow<List<LeadEntity>>

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE status = 'NEW'")
    fun getNewLeadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE status = 'CONVERTED'")
    fun getConvertedLeadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE createdAt >= :since")
    fun getLeadsCountSince(since: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<LeadEntity>): List<Long>

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteLeadById(id: Long)

    @Query("UPDATE leads SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLeadStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE leads SET categoryId = :categoryId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLeadCategory(id: Long, categoryId: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE leads SET leadScore = :score, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLeadScore(id: Long, score: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM leads WHERE updatedAt >= :since ORDER BY leadScore DESC")
    fun getHighScoreLeadsSince(since: Long): Flow<List<LeadEntity>>
}
