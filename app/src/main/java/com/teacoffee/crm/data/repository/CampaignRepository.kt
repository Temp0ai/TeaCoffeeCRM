package com.teacoffee.crm.data.repository

import com.teacoffee.crm.data.local.dao.CampaignDao
import com.teacoffee.crm.data.local.entity.CampaignEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampaignRepository @Inject constructor(
    private val campaignDao: CampaignDao
) {
    fun getAllCampaigns(): Flow<List<CampaignEntity>> = campaignDao.getAllCampaigns()
    suspend fun insertCampaign(campaign: CampaignEntity): Long = campaignDao.insertCampaign(campaign)
    suspend fun updateCampaignStats(id: Long, sent: Int, delivered: Int, readCount: Int, failed: Int) =
        campaignDao.updateCampaignStats(id, sent, delivered, readCount, failed)
}
