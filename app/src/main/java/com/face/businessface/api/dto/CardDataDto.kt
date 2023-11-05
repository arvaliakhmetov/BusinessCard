package com.face.businessface.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CardDataDto(
    val activities: List<String>,
    val contacts: Map<String,String>,
    val features: List<FloatArray>
)

fun CardDataDto.toJSONString():String{
    return Json.encodeToString(this)
}
