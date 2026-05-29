package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gmail_messages")
data class GmailMessageEntity(
    @PrimaryKey val id: String,  // Gmail message ID
    val threadId: String,
    val fromAddress: String,
    val fromName: String = "",
    val subject: String,
    val body: String,
    val receivedAt: Long,
    val isRead: Boolean = false,
    val isLeadExtracted: Boolean = false,
    val leadId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
