package com.teacoffee.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",  // TEA_PREMIX, COFFEE_PREMIX, NESCAFE_PREMIX, MACHINE
    val imageUrl: String = "",
    val whatsappCatalogId: String = "",
    val isActive: Boolean = true,
    val stockQuantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
