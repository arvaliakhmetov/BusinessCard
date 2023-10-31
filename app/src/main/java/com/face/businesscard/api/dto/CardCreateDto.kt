package com.face.businesscard.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CardCreateDto (
    val name: String,
    val surname: String,
    val second_name: String,
    val company: String = "",
    val jobtitle: String = "",
    val description: String,
    val data: String
)