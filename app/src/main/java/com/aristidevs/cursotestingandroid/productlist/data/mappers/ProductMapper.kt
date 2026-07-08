package com.aristidevs.cursotestingandroid.productlist.data.mappers

import com.aristidevs.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import com.aristidevs.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.aristidevs.cursotestingandroid.productlist.domain.model.Product

fun ProductResponse.toEntity(): ProductEntity {
    val finalPrice = priceCents?.div(100.0) ?: 0.0

    return ProductEntity(
        id = id,
        name = name,
        description = description,
        price = finalPrice,
        category = category,
        stock = stock,
        imageUrl = imageUrl,
    )
}

fun ProductEntity.toDomain(): Product? {
    if (category.isNullOrEmpty()) return null
    return Product(
        id = id,
        name = name,
        description = description.orEmpty(),
        price = price,
        category = category,
        stock = stock ?: 0,
        imageUrl = imageUrl,
    )
}
