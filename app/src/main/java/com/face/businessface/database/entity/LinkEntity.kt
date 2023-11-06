package com.face.businessface.database.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class LinkEntity(
    val name:String = "",
    val link: String = ""
)