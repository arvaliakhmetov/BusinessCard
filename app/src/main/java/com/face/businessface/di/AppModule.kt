package com.face.businessface.di

import android.app.Application
import androidx.room.Room
import com.face.businessface.api.ApiRepository
import com.face.businessface.api.ApiRepositoryImpl
import com.face.businessface.api.BusinessFaceApi
import com.face.businessface.api.RemoteConsts
import com.face.businessface.database.CardDatabase
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.database.dao.CardInfoRepositoryImpl
import com.face.businessface.navigation.Navigator
import com.face.businessface.navigation.SharedDataRepository
import com.face.businessface.ui.ApiRequestHelper
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
