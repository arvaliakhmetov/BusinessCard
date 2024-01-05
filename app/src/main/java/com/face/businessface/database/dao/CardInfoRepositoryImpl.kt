package com.face.businessface.database.dao

import FaceRecognitionProcessor
import android.graphics.Bitmap
import android.util.Log
import android.util.Pair
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.database.entity.toMappedFeatures
import com.face.businessface.database.entity.toPerson
import com.google.mlkit.vision.face.Face
import cropToBBox
import flip
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.sqrt

class CardInfoRepositoryImpl(
    private val dao: CardInfoDao
): CardInfoRepository {
    override suspend fun insertCard(cardInfo: CardInfo): Long {
        return dao.insertCard(cardInfo)
    }

    override fun getKnownFaces(): Flow<List<CardInfo>> {
        return dao.getKnownFaces()
    }

    override suspend fun getPersonById(id: Long): PersonDto {
        return dao.getCardInfoById(id).toPerson()
    }

    override suspend fun deleteCard(id: Long) {
        dao.deleteCard(id)
    }

    override suspend fun setFavoriteCard(id: Long, favorite: Boolean) {
        dao.setFavoriteCard(id,favorite)
    }

    override suspend fun getNearestFace(
        face: Face,
        flip: Boolean,
        faceProcessor: FaceRecognitionProcessor,
        imageProxy: Bitmap
    ): Long? {
        val faceBox = cropToBBox(
            imageProxy,
            face.boundingBox,
            if (flip) 270f else 90f
        )?.flip(horizontal = flip)
        if (faceBox?.isSuccess == true) {
            Log.d("dist", face.headEulerAngleZ.toString())
            val feature = faceProcessor.detectInImage(listOf(face), faceBox.getOrNull()!!)
            val knownFaces = dao.getKnownFaces().first()
            if (knownFaces.isNotEmpty()) {
                val nearestFace = knownFaces.toMappedFeatures()
                    .minByOrNull { pair ->
                        feature!!.zip(pair.second)
                            .map { (a, b) -> (a - b) * (a - b) }
                            .sum()
                    }?.let { person ->
                        val squaredDistance = feature!!.zip(person.second)
                            .map { (a, b) -> (a - b) * (a - b) }
                            .sum()
                        Pair(person.first, sqrt(squaredDistance.toDouble()).toFloat())
                    }
                return dao.getCardInfoById(nearestFace!!.first).toPerson().copy(dist = nearestFace.second).id
            } else {
                return null
            }
        }else{
            return null
        }
    }
}

