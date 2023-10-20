package com.face.businesscard.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CardInfo(
    val name: String = "",
    val surname: String = "",
    val secondName: String = "",
    val description: String = "",
    val activities: List<String> = emptyList(),
    val links: List<LinkEntity> = emptyList(),
    val arrayOfFeatures: List<FloatArray> = emptyList(),
    @PrimaryKey
    val id: Long? = null
)
