package com.face.businessface.api.dto

import android.graphics.Bitmap


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