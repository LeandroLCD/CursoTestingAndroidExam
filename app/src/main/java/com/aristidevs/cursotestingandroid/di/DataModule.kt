package com.aristidevs.cursotestingandroid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.aristidevs.cursotestingandroid.cart.data.local.database.dao.CartItemDao
import com.aristidevs.cursotestingandroid.cart.data.repository.CartItemRepositoryImpl
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.checkout.data.repository.OrderRepositoryImpl
import com.aristidevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.aristidevs.cursotestingandroid.core.data.coroutines.DefaultDispatchersProvider
import com.aristidevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.aristidevs.cursotestingandroid.core.data.util.SystemClock
import com.aristidevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import com.aristidevs.cursotestingandroid.core.domain.util.Clock
import com.aristidevs.cursotestingandroid.productlist.data.local.database.dao.ProductDao
import com.aristidevs.cursotestingandroid.productlist.data.local.database.dao.PromotionDao
import com.aristidevs.cursotestingandroid.productlist.data.repository.ProductRepositoryImpl
import com.aristidevs.cursotestingandroid.productlist.data.repository.PromotionRepositoryImpl
import com.aristidevs.cursotestingandroid.productlist.data.repository.SettingsRepositoryImpl
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDispatchersProvider(defaultDispatchersProvider: DefaultDispatchersProvider): DispatchersProvider =
        defaultDispatchersProvider

    @Provides
    @Singleton
    fun provideProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository =
        productRepositoryImpl

    @Provides
    @Singleton
    fun providePromotionRepository(promotionRepositoryImpl: PromotionRepositoryImpl): PromotionRepository =
        promotionRepositoryImpl

    @Provides
    fun providesProductDao(database: MiniMarketDatabase): ProductDao = database.productDao()

    @Provides
    fun providesPromotionDao(database: MiniMarketDatabase): PromotionDao = database.promotionDao()

    @Provides
    fun providesCartItemDao(database: MiniMarketDatabase): CartItemDao = database.cartItemDao()

    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
    ): MiniMarketDatabase =
        Room
            .databaseBuilder(
                context = context,
                klass = MiniMarketDatabase::class.java,
                name = "minimarket_database",
            ).build()

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository =
        settingsRepositoryImpl

    @Provides
    @Singleton
    fun provideCartRepository(cartItemRepositoryImpl: CartItemRepositoryImpl): CartItemRepository =
        cartItemRepositoryImpl

    @Provides
    @Singleton
    fun provideOrderRepository(orderRepositoryImpl: OrderRepositoryImpl): OrderRepository {
        return orderRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideClock(systemClock: SystemClock): Clock = systemClock
}
