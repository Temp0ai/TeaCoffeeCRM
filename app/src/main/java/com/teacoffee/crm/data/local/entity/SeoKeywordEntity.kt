package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seo_keywords")
data class SeoKeywordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val searchVolume: Int = 0,
    val competition: String = "LOW",  // LOW, MEDIUM, HIGH
    val difficulty: Int = 0,
    val source: String = "",  // UBERSUGGEST, SEMRUSH, MANUAL
    val category: String = "",  // PRODUCT, BRAND, GENERIC
    val isTracked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
