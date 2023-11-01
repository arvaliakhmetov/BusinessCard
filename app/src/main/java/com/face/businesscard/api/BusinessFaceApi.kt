package com.face.businesscard.api

import com.face.businesscard.api.dto.FeatureDto
import com.face.businesscard.api.dto.PersonDto
import com.google.android.gms.common.Feature
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BusinessFaceApi {

    @Multipart
    @POST("/create_person/")
    suspend fun createPerson(
        @Part name: MultipartBody.Part,
        @Part surname: MultipartBody.Part,
        @Part second_name: MultipartBody.Part,
        @Part description: MultipartBody.Part,
        @Part company: MultipartBody.Part,
        @Part jobTitle: MultipartBody.Part,
        @Part data: MultipartBody.Part
        ): Response<String>

    @Multipart
    @POST("/create_person/")
    suspend fun createPersonWImage(
        @Part name: MultipartBody.Part,
        @Part surname: MultipartBody.Part,
        @Part second_name: MultipartBody.Part,
        @Part description: MultipartBody.Part,
        @Part company: MultipartBody.Part,
        @Part jobTitle: MultipartBody.Part,
        @Part file: MultipartBody.Part,
        @Part data: MultipartBody.Part
    ): Response<String>

    @Multipart
    @POST("/get_best_person/")
    suspend fun getPerson(@Part feature: MultipartBody.Part): Response<PersonDto>

    @GET("/get_image/")
    suspend fun getImage(@Query("id") id:String):Response<ResponseBody>
}