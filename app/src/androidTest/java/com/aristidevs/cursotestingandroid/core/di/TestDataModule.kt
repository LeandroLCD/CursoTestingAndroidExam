package com.aristidevs.cursotestingandroid.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
import com.aristidevs.cursotestingandroid.di.DataModule
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
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

private val Context.testingDataStore: DataStore<Preferences> by preferencesDataStore("testing_settings")

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
object TestDataModule {
    @Provides
    @Singleton
    fun provideDispatchersProvider(defaultDispatchersProvider: DefaultDispatchersProvider): DispatchersProvider = defaultDispatchersProvider

    @Provides
    @Singleton
    fun provideProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository = productRepositoryImpl

    @Provides
    @Singleton
    fun providePromotionRepository(promotionRepositoryImpl: PromotionRepositoryImpl): PromotionRepository = promotionRepositoryImpl

    @Provides
    fun providesProductDao(database: MiniMarketDatabase): ProductDao = database.productDao()

    @Provides
    fun providesPromotionDao(database: MiniMarketDatabase): PromotionDao = database.promotionDao()

    @Provides
    fun providesCartItemDao(database: MiniMarketDatabase): CartItemDao = database.cartItemDao()

    @Provides
    @Singleton
    fun providesDatabase(): MiniMarketDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room
            .inMemoryDatabaseBuilder(
                context,
                MiniMarketDatabase::class.java,
            ).build()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository = settingsRepositoryImpl

    @Provides
    @Singleton
    fun provideCartRepository(cartItemRepositoryImpl: CartItemRepositoryImpl): CartItemRepository = cartItemRepositoryImpl

    @Provides
    @Singleton
    fun provideOrderRepository(orderRepositoryImpl: OrderRepositoryImpl): OrderRepository = orderRepositoryImpl

    @Provides
    @Singleton
    fun provideClock(systemClock: SystemClock): Clock = systemClock

    @Provides
    @Singleton
    fun provideDataStore(): DataStore<Preferences> = ApplicationProvider.getApplicationContext<Context>().testingDataStore
}
