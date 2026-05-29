package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = LeadEntity::class,
            parentColumns = ["id"],
            childColumns = ["leadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("leadId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leadId: Long,
    val content: String,
    val type: String,  // WHATSAPP, EMAIL, SMS
    val direction: String,  // SENT, RECEIVED
    val status: String = "PENDING",  // PENDING, SENT, DELIVERED, READ, FAILED
    val isAiGenerated: Boolean = false,
    val campaignId: Long? = null,
    val sentAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
