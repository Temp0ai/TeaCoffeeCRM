package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val messageTemplate: String,
    val categoryIds: String = "",  // comma-separated
    val clientTypes: String = "",  // comma-separated
    val status: String = "DRAFT",  // DRAFT, SCHEDULED, RUNNING, COMPLETED, CANCELLED
    val scheduledAt: Long = 0L,
    val sentCount: Int = 0,
    val deliveredCount: Int = 0,
    val readCount: Int = 0,
    val failedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
