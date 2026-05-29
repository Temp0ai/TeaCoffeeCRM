package com.teacoffee.crm.data.local.dao

import androidx.room.*
import com.teacoffee.crm.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE leadId = :leadId ORDER BY createdAt DESC")
    fun getMessagesByLead(leadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE campaignId = :campaignId ORDER BY createdAt DESC")
    fun getMessagesByCampaign(campaignId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE type = :type ORDER BY createdAt DESC")
    fun getMessagesByType(type: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE direction = 'SENT' AND createdAt >= :since")
    fun getSentCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE status = 'FAILED' AND createdAt >= :since")
    fun getFailedCountSince(since: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>): List<Long>

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?
}
