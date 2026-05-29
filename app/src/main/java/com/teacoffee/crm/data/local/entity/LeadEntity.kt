package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "leads",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("email"), Index("phone")]
)
data class LeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val company: String = "",
    val designation: String = "",
    val productRequirement: String = "",
    val orderDetails: String = "",
    val inquiryDetails: String = "",
    val source: String = "",  // GMAIL, MANUAL, EXCEL, WHATSAPP
    val categoryId: Long? = null,
    val clientType: String = "",  // SOCIETY, CAFE, RESTAURANT, OFFICE, MANUFACTURER, RETAILER
    val status: String = "NEW",  // NEW, CONTACTED, FOLLOW_UP, CONVERTED, CLOSED
    val leadScore: Int = 0,
    val notes: String = "",
    val lastContactedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
