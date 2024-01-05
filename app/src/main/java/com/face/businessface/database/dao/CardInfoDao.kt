package com.face.businessface.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.face.businessface.database.entity.CardInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface CardInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(user: CardInfo):Long

    @Query("SELECT * FROM CardInfo")
    fun getKnownFaces(): Flow<List<CardInfo>>

    @Query("DELETE FROM CardInfo WHERE id = :id")
    suspend fun deleteCard(id:Long)

    @Query("SELECT * FROM CardInfo WHERE id = :id")
    suspend fun getCardInfoById(id: Long): CardInfo

    @Query("UPDATE cardinfo SET favorite=:favorite WHERE id=:id")
    suspend fun setFavoriteCard(id: Long, favorite: Boolean)
}