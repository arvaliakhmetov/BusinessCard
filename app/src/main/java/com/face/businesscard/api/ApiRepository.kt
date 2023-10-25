package com.face.businesscard.api

import com.face.businesscard.api.dto.CardCreateDto
import com.face.businesscard.api.dto.FeatureDto
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.ui.ApiResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody

interface ApiRepository {
    fun createPerson(
        createDto: CardCreateDto
    ): Flow<ApiResponse<String>>

    fun getPerson(feature: FeatureDto): Flow<ApiResponse<PersonDto>>

    fun getImage(id: String): Flow<ApiResponse<ResponseBody>>

}