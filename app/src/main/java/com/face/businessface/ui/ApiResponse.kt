package com.face.businessface.ui

import android.util.Log
import com.face.businessface.api.dto.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject

sealed class ApiResponse<out T> {
    object Loading: ApiResponse<Nothing>()
    object Idling: ApiResponse<Nothing>()

    data class Success<out T>(
        val data: T
    ): ApiResponse<T>()

    data class Failure(
        val timestamp: String? = null,
        val status: String? = null,
        val errorMessage: String? = null,
        val msg: String? = null,
        val code: Int? = null,
        val details: String? = null
    ): ApiResponse<Nothing>()
}
class ApiRequestHelper @Inject constructor(

) {

    fun <T> apiRequestFlow(call: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {
        emit(ApiResponse.Loading)

        withTimeoutOrNull(20000L) {
            val response = call()
            try {
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        kotlinx.coroutines.delay(60)
                        Log.d("PARSED_ERROR",response.toString())
                        emit(ApiResponse.Success(data))
                    }
                } else {
                    response.errorBody()?.let { error ->
                        kotlinx.coroutines.delay(60)
                        error.close()
                        val json = Json{ignoreUnknownKeys = true}
                        val parsedError: ErrorResponse = json.decodeFromString(error.charStream().readText())
                        emit(
                            ApiResponse.Failure(
                                errorMessage = parsedError.detail.first().msg,
                                code = null,
                                status = null,
                                timestamp = null,
                                details = null
                            )
                        )

                    }
                }
            } catch (e: Exception) {
                emit(ApiResponse.Failure(errorMessage = e.message ?: e.toString(), code = 400))
            }
        } ?: emit(ApiResponse.Failure(errorMessage = "Timeout! Please try again.", code = 408))
    }.flowOn(Dispatchers.IO)
}