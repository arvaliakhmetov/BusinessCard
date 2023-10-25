package com.face.businesscard.di

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.face.businesscard.api.ApiRepository
import com.face.businesscard.api.ApiRepositoryImpl
import com.face.businesscard.api.BusinessFaceApi
import com.face.businesscard.api.RemoteConsts
import com.face.businesscard.database.CardDatabase
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.dao.CardInfoRepositoryImpl
import com.face.businesscard.navigation.Navigator
import com.face.businesscard.navigation.SharedDataRepository
import com.face.businesscard.ui.ApiRequestHelper
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val json = Json { ignoreUnknownKeys = true }
    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application): CardDatabase {
        return Room.databaseBuilder(
            app,
            CardDatabase::class.java,
            "card_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providesCardInfoRepository(db: CardDatabase):CardInfoRepository{
        return CardInfoRepositoryImpl(db.cardInfo)
    }

    @Provides
    @Singleton
    fun provideApiRepository(api: BusinessFaceApi, apiRequestHelper: ApiRequestHelper): ApiRepository {
        return ApiRepositoryImpl(api,apiRequestHelper)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(RemoteConsts.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaTypeOrNull()!!))

    @Provides
    @Singleton
    fun provideNavigator(): Navigator = Navigator()

    @Provides
    @Singleton
    fun provideSharedData(): SharedDataRepository = SharedDataRepository()

    @Singleton
    @Provides
    fun provideMainAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): BusinessFaceApi =
        retrofit
            .client(okHttpClient)
            .build()
            .create(BusinessFaceApi::class.java)

}
