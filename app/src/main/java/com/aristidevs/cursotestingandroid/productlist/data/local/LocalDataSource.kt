package com.aristidevs.cursotestingandroid.productlist.data.local

import com.aristidevs.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.aristidevs.cursotestingandroid.cart.data.local.database.entity.CartItemEntity
import com.aristidevs.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.aristidevs.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.aristidevs.cursotestingandroid.productlist.data.local.database.entity.ProductEntity
import com.aristidevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class LocalDataSource
    @Inject
    constructor(
        private val productDao: ProductDao,
        private val promotionDao: PromotionDao,
        private val cartItemDao: CartItemDao,
    ) {
        fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

        fun getProductById(productId: String): Flow<ProductEntity?> = productDao.getProductById(productId)

        fun getProductsByIds(productIds: Set<String>): Flow<List<ProductEntity>> {
            if (productIds.isEmpty()) return flowOf(emptyList())

            return productDao.getProductsByIds(productIds.toList())
        }

        fun getAllPromotions(): Flow<List<PromotionEntity>> = promotionDao.getAllPromotions()

        suspend fun saveProducts(products: List<ProductEntity>) {
            productDao.replaceAll(products)
        }

        suspend fun savePromotions(promotions: List<PromotionEntity>) {
            promotionDao.replaceAll(promotions)
        }

        fun getAllCartItems(): Flow<List<CartItemEntity>> = cartItemDao.getAllCartItems()

        suspend fun getCartItemById(productId: String): CartItemEntity? = cartItemDao.getCartItemById(productId)

        suspend fun updateCartItem(cartItemEntity: CartItemEntity): Result<Unit> =
            try {
                cartItemDao.updateCartItem(cartItemEntity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun deleteCartItem(cartItemEntity: CartItemEntity): Result<Unit> =
            try {
                cartItemDao.deleteCartItem(cartItemEntity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun clearCart(): Result<Unit> =
            try {
                cartItemDao.clearCart()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        suspend fun insertCartItem(itemEntity: CartItemEntity): Result<Unit> =
            try {
                cartItemDao.insertCartItem(itemEntity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
