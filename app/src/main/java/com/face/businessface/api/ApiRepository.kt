package com.face.businessface.api

import com.face.businessface.api.dto.CardCreateDto
import com.face.businessface.api.dto.FeatureDto
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.ui.ApiResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody

interface ApiRepository {
    fun createPerson(
        createDto: CardCreateDto
    ): Flow<ApiResponse<ResponseBody>>

    fun getPerson(feature: FeatureDto): Flow<ApiResponse<PersonDto>>

    fun getImage(id: String): Flow<ApiResponse<ResponseBody>>

    fun deletePerson(id: String): Flow<ApiResponse<ResponseBody>>

}