package com.face.businesscard.database.dao

import com.face.businesscard.database.entity.CardInfo
import kotlinx.coroutines.flow.Flow

interface CardInfoRepository {
    suspend fun insertCard(cardInfo: CardInfo): Long
    fun getKnownFaces(): Flow<List<CardInfo>>
    suspend fun deleteCard(id:Long)
}