package com.teacoffee.crm.data.repository

import com.teacoffee.crm.data.local.dao.LeadDao
import com.teacoffee.crm.data.local.entity.LeadEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadRepository @Inject constructor(
    private val leadDao: LeadDao
) {
    fun getAllLeads(): Flow<List<LeadEntity>> = leadDao.getAllLeads()

    fun getLeadsByCategory(categoryId: Long): Flow<List<LeadEntity>> =
        leadDao.getLeadsByCategory(categoryId)

    fun getLeadsByClientType(clientType: String): Flow<List<LeadEntity>> =
        leadDao.getLeadsByClientType(clientType)

    fun getLeadsByStatus(status: String): Flow<List<LeadEntity>> =
        leadDao.getLeadsByStatus(status)

    fun getLeadsBySource(source: String): Flow<List<LeadEntity>> =
        leadDao.getLeadsBySource(source)

    fun searchLeads(query: String): Flow<List<LeadEntity>> =
        leadDao.searchLeads(query)

    fun getLeadCount(): Flow<Int> = leadDao.getLeadCount()

    fun getNewLeadCount(): Flow<Int> = leadDao.getNewLeadCount()

    fun getConvertedLeadCount(): Flow<Int> = leadDao.getConvertedLeadCount()

    fun getLeadsCountSince(since: Long): Flow<Int> = leadDao.getLeadsCountSince(since)

    fun getHighScoreLeadsSince(since: Long): Flow<List<LeadEntity>> =
        leadDao.getHighScoreLeadsSince(since)

    suspend fun getLeadById(id: Long): LeadEntity? = leadDao.getLeadById(id)

    suspend fun getLeadByEmail(email: String): LeadEntity? = leadDao.getLeadByEmail(email)

    suspend fun insertLead(lead: LeadEntity): Long = leadDao.insertLead(lead)

    suspend fun insertLeads(leads: List<LeadEntity>): List<Long> = leadDao.insertLeads(leads)

    suspend fun updateLead(lead: LeadEntity) = leadDao.updateLead(lead)

    suspend fun deleteLead(lead: LeadEntity) = leadDao.deleteLead(lead)

    suspend fun deleteLeadById(id: Long) = leadDao.deleteLeadById(id)

    suspend fun updateLeadStatus(id: Long, status: String) =
        leadDao.updateLeadStatus(id, status)

    suspend fun updateLeadCategory(id: Long, categoryId: Long?) =
        leadDao.updateLeadCategory(id, categoryId)
}
