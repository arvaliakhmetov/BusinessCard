package com.face.businesscard.di

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.face.businesscard.database.CardDatabase
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.dao.CardInfoRepositoryImpl
import com.face.businesscard.navigation.Navigator
import com.face.businesscard.navigation.SharedDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
    fun provideNavigator(): Navigator = Navigator()

    @Provides
    @Singleton
    fun provideSharedData(): SharedDataRepository = SharedDataRepository()

}
