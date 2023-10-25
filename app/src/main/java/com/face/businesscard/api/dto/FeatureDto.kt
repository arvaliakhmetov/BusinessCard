package com.face.businesscard.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeatureDto(
    val feature: FloatArray
)
