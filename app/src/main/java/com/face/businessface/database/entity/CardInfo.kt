package com.face.businessface.database.entity

import android.app.Person
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.face.businessface.api.dto.PersonDto
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CardInfo(
    val name: String = "",
    val surname: String = "",
    val secondName: String = "",
    val description: String = "",
    val jobtitle: String = "",
    val company: String = "",
    val favorite: Boolean = false,
    val activities: List<String> = emptyList(),
    val links: List<LinkEntity> = emptyList(),
    val arrayOfFeatures: List<FloatArray> = emptyList(),
    @PrimaryKey
    val id: Long? = null
)

fun CardInfo.toPerson(): PersonDto{
    return PersonDto(
        id = id?:-1,
        name = name,
        surname = surname,
        second_name = secondName,
        company = company,
        favorite = favorite,
        jobtitile = jobtitle,
        activities = activities,
        conts = links.associate { it.name to it.link },
        description = description,
        dist = -1f
    )
}
fun List<CardInfo>.toMappedFeatures(): List<kotlin.Pair<Long,FloatArray>> {
    val listOfpairs = mutableListOf<kotlin.Pair<Long,FloatArray>>()
    this.forEach { cardInfo ->
        cardInfo.arrayOfFeatures.forEach {floatArray ->
            listOfpairs.add(kotlin.Pair(cardInfo.id!!, floatArray))
        }
    }
    return listOfpairs
}
