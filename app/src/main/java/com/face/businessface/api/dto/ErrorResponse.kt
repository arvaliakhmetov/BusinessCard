package com.face.businessface.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val detail : List<ContentF>
)

@Serializable
data class ContentF(
    val msg: String,
    val type: String
)