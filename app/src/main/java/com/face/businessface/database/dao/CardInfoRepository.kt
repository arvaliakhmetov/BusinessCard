package com.face.businessface.database.dao

import FaceRecognitionProcessor
import android.graphics.Bitmap
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.entity.CardInfo
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.flow.Flow

interface CardInfoRepository {
    suspend fun insertCard(cardInfo: CardInfo): Long
    fun getKnownFaces(): Flow<List<CardInfo>>

    suspend fun getPersonById(id:Long):PersonDto
    suspend fun deleteCard(id:Long)

    suspend fun setFavoriteCard(id: Long, favorite: Boolean)

    suspend fun getNearestFace(face: Face,
                               flip: Boolean,
                               faceProcessor: FaceRecognitionProcessor,
                               imageProxy: Bitmap) : Long?
}