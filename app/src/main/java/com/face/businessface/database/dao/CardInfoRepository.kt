package com.face.businessface.database.dao

import com.face.businessface.database.entity.CardInfo
import kotlinx.coroutines.flow.Flow

interface CardInfoRepository {
    suspend fun insertCard(cardInfo: CardInfo): Long
    fun getKnownFaces(): Flow<List<CardInfo>>
    suspend fun deleteCard(id:Long)
}