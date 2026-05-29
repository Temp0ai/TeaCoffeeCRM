package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,  // PRODUCT, EQUIPMENT, CLIENT_TYPE
    val description: String = "",
    val color: String = "#FF1B5E20",
    val createdAt: Long = System.currentTimeMillis()
)
