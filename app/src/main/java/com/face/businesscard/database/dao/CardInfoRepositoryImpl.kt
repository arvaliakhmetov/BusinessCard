package com.face.businesscard.database.dao

import com.face.businesscard.database.entity.CardInfo
import kotlinx.coroutines.flow.Flow

class CardInfoRepositoryImpl(
    private val dao: CardInfoDao
): CardInfoRepository {
    override suspend fun insertCard(cardInfo: CardInfo): Long {
        return dao.insertCard(cardInfo)
    }

    override fun getKnownFaces(): Flow<List<CardInfo>> {
        return dao.getKnownFaces()
    }

    override suspend fun deleteCard(id: Long) {
        return dao.deleteCard(id)
    }
}