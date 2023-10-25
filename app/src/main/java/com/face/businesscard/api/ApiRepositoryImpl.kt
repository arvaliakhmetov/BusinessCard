package com.face.businesscard.api

import android.util.Log
import com.face.businesscard.api.dto.CardCreateDto
import com.face.businesscard.api.dto.FeatureDto
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.ui.ApiRequestHelper
import com.face.businesscard.ui.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody

class ApiRepositoryImpl(
    private val api: BusinessFaceApi,
    private val apiRequestHelper: ApiRequestHelper
): ApiRepository{
    override fun createPerson(
        createDto: CardCreateDto
    )= apiRequestHelper.apiRequestFlow{
        val name =  MultipartBody.Part.createFormData("name",createDto.name)
        val surname =  MultipartBody.Part.createFormData("surname",createDto.surname)
        val second_name =  MultipartBody.Part.createFormData("second_name",createDto.second_name)
        val description =  MultipartBody.Part.createFormData("description",createDto.description)
        val data =  MultipartBody.Part.createFormData("data",createDto.data)
        api.createPerson(name,surname,second_name,description,data)
    }

    override fun getPerson(feature: FeatureDto) = apiRequestHelper.apiRequestFlow{
        val featurePart =  MultipartBody.Part.createFormData("data",Json.encodeToString(feature))
        api.getPerson(featurePart)
    }

    override fun getImage(id: String) = apiRequestHelper.apiRequestFlow {
        api.getImage(id)
    }
}