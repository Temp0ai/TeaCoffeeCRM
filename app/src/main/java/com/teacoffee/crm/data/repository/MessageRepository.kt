package com.teacoffee.crm.data.repository

import com.teacoffee.crm.data.local.dao.MessageDao
import com.teacoffee.crm.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    fun getMessagesByLead(leadId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesByLead(leadId)

    fun getMessagesByCampaign(campaignId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesByCampaign(campaignId)

    fun getSentCountSince(since: Long): Flow<Int> = messageDao.getSentCountSince(since)

    fun getFailedCountSince(since: Long): Flow<Int> = messageDao.getFailedCountSince(since)

    suspend fun insertMessage(message: MessageEntity): Long =
        messageDao.insertMessage(message)

    suspend fun updateMessageStatus(id: Long, status: String) =
        messageDao.updateMessageStatus(id, status)
}
