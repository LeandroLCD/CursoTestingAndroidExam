package com.aristidevs.cursotestingandroid.productlist.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val category: String?,
    val stock: Int?,
    val imageUrl: String? = null,
)
