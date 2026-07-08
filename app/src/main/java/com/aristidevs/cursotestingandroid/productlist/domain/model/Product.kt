package com.aristidevs.cursotestingandroid.productlist.domain.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val imageUrl: String? = null,
)
