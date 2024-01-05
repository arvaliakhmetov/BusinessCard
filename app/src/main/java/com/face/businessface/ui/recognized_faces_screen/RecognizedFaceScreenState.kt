package com.face.businessface.ui.recognized_faces_screen


import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.mvi.State

data class RecognizedFaceScreenState(
    val person: PersonDto? = null
): State
