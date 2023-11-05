package com.face.businessface.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.face.businessface.database.dao.CardInfoDao
import com.face.businessface.database.entity.CardInfo

@Database(
    entities = [CardInfo::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CardDatabase: RoomDatabase() {
    abstract val cardInfo: CardInfoDao
}