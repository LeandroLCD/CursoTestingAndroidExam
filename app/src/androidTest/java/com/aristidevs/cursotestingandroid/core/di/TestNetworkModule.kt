package com.aristidevs.cursotestingandroid.core.di

import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.di.NetworkModule
import com.aristidevs.cursotestingandroid.productlist.data.remote.MiniMarketApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class],
)
object TestNetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit
            .Builder()
            .baseUrl(MockWebServerUrlHolder.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideMiniMarketApiService(retrofit: Retrofit): MiniMarketApiService = retrofit.create(MiniMarketApiService::class.java)
}
