package com.face.businessface.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonDto(
    val id: Long,
    val name: String,
    val surname : String,
    val second_name: String,
    val company: String,
    val jobtitile: String,
    val description: String,
    val favorite:Boolean,
    val activities: List<String>,
    val conts: Map<String,String>,
    val dist: Float
)
