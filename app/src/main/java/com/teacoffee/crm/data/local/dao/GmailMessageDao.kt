package com.teacoffee.crm.data.local.dao

import androidx.room.*
import com.teacoffee.crm.data.local.entity.GmailMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GmailMessageDao {
    @Query("SELECT * FROM gmail_messages ORDER BY receivedAt DESC")
    fun getAllMessages(): Flow<List<GmailMessageEntity>>

    @Query("SELECT * FROM gmail_messages WHERE isLeadExtracted = 0 ORDER BY receivedAt DESC")
    fun getUnprocessedMessages(): Flow<List<GmailMessageEntity>>

    @Query("SELECT * FROM gmail_messages WHERE id = :id")
    suspend fun getMessageById(id: String): GmailMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GmailMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<GmailMessageEntity>)

    @Update
    suspend fun updateMessage(message: GmailMessageEntity)

    @Query("UPDATE gmail_messages SET isLeadExtracted = 1, leadId = :leadId WHERE id = :messageId")
    suspend fun markAsExtracted(messageId: String, leadId: Long)

    @Query("SELECT COUNT(*) FROM gmail_messages WHERE isLeadExtracted = 0")
    fun getUnprocessedCount(): Flow<Int>
}
