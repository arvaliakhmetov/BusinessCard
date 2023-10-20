package com.face.businesscard.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.face.businesscard.database.entity.CardInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface CardInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(user: CardInfo):Long

    @Query("SELECT * FROM cardinfo")
    fun getKnownFaces(): Flow<List<CardInfo>>

    @Query("DELETE FROM CardInfo WHERE id = :id")
    suspend fun deleteCard(id:Long)
}