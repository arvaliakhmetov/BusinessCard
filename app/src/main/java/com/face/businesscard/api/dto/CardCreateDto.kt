package com.face.businesscard.api.dto

import android.graphics.Bitmap
import com.canhub.cropper.CropImageView
import kotlinx.serialization.Serializable


data class CardCreateDto (
    val image: Bitmap? = null,
    val name: String,
    val surname: String,
    val second_name: String,
    val company: String = "",
    val jobtitle: String = "",
    val description: String,
    val data: String
)