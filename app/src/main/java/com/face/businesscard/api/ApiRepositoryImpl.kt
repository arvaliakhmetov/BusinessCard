package com.face.businesscard.api

import android.graphics.Bitmap
import android.util.Log
import com.face.businesscard.api.dto.CardCreateDto
import com.face.businesscard.api.dto.FeatureDto
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.ui.ApiRequestHelper
import com.face.businesscard.ui.ApiResponse
import com.face.businesscard.ui.recognized_faces_screen.defaultBitmap
import com.face.businesscard.ui.recognized_faces_screen.defaultBitmap1
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

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
        Log.d("DATA_A",createDto.data)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val image = defaultBitmap1()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
        val filePart = MultipartBody.Part.createFormData("file", "filename.jpg", requestFile)

        api.createPerson(name,surname,second_name,description,company,jobTitle,filePart,data)
    }

    override fun getPerson(feature: FeatureDto) = apiRequestHelper.apiRequestFlow{
        val featurePart =  MultipartBody.Part.createFormData("data",Json.encodeToString(feature))

        Log.d("DATA_A",Json.encodeToString(feature))
        api.getPerson(featurePart)
    }

    override fun getImage(id: String) = apiRequestHelper.apiRequestFlow {
        api.getImage(id)
    }
}