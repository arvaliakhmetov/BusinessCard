package com.face.businessface.api

import android.graphics.Bitmap
import android.util.Log
import com.face.businessface.api.dto.CardCreateDto
import com.face.businessface.api.dto.FeatureDto
import com.face.businessface.ui.ApiRequestHelper
import com.face.businessface.ui.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.time.Instant

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
        val jobTitle = MultipartBody.Part.createFormData("jobtitle",createDto.jobtitle)
        val company = MultipartBody.Part.createFormData("company",createDto.company)
        val data =  MultipartBody.Part.createFormData("data",createDto.data)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val image = createDto.image
        if(image!= null){
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
            val filePart = MultipartBody.Part.createFormData("file", "${Instant.now().toEpochMilli()}.jpg", requestFile)
            api.createPersonWImage(name,surname,second_name,description,company,jobTitle,filePart,data)
        }else{
            api.createPerson(name,surname,second_name,description,company,jobTitle,data)
        }



    }

    override fun getPerson(feature: FeatureDto) = apiRequestHelper.apiRequestFlow{
        val featurePart =  MultipartBody.Part.createFormData("data",Json.encodeToString(feature))

        Log.d("DATA_A",Json.encodeToString(feature))
        api.getPerson(featurePart)
    }

    override fun deletePerson(id: String)= apiRequestHelper.apiRequestFlow{
        api.deletePerson(id)
    }

    override fun getImage(id: String) = apiRequestHelper.apiRequestFlow {
        api.getImage(id)
    }
}