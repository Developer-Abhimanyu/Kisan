package com.agentic.quartet.kisan.di

import com.agentic.quartet.kisan.data.remote.MarketApiService
import com.agentic.quartet.kisan.data.repository.MarketRepositoryImpl
import com.agentic.quartet.kisan.domain.repository.MarketRepository
import com.agentic.quartet.kisan.domain.usecase.GetMarketPriceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Provides
    @Singleton
    fun provideMarketApiService(client: HttpClient): MarketApiService =
        MarketApiService(client)

    @Provides
    @Singleton
    fun provideMarketRepository(apiService: MarketApiService): MarketRepository =
        MarketRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideMarketPriceUseCase(repository: MarketRepository): GetMarketPriceUseCase =
        GetMarketPriceUseCase(repository)
}