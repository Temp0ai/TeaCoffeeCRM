package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_templates")
data class ContentTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val type: String,  // FOLLOW_UP, PROMOTION, GREETING, PRODUCT_UPDATE, SOCIAL_MEDIA
    val platform: String = "WHATSAPP",  // WHATSAPP, EMAIL, SMS, INSTAGRAM, FACEBOOK
    val category: String = "",  // TEA, COFFEE, MACHINE, GENERAL
    val isAiGenerated: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
