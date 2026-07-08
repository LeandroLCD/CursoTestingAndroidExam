package com.aristidevs.cursotestingandroid.cart.data.mapper

import com.aristidevs.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem

fun CartItemEntity.toDomain(): CartItem =
    CartItem(
        productId = productId,
        quantity = quantity,
    )

fun CartItem.toEntity(): CartItemEntity =
    CartItemEntity(
        productId = productId,
        quantity = quantity,
    )
