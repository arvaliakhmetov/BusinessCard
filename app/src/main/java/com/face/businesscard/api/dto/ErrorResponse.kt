package com.face.businesscard.api.dto

import com.face.businesscard.ui.ApiResponse
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