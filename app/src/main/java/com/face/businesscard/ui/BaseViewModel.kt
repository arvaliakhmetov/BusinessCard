package com.face.businesscard.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class BaseViewModel : ViewModel() {
    private var mJob: Job? = null

    protected fun <T> baseRequest(liveData: MutableStateFlow<T>, errorHandler: CoroutinesErrorHandler, request: () -> Flow<T>) {
        mJob = viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, error ->
            viewModelScope.launch(Dispatchers.Main) {
                delay(500)
                if(error.localizedMessage != null) {
                    if (error.localizedMessage!!.contains("Unable to resolve host") ||
                        error.localizedMessage!!.contains("failed to connect") ||
                        error.localizedMessage!!.contains("timeout"))   {
                        errorHandler.onError(
                            error.localizedMessage!!
                        )
                    }else{
                            errorHandler.onError(
                                error.localizedMessage!!
                            )
                        }

                }else{
                    Log.d("КАКАЯ ТО ОШИБКА",error.stackTraceToString())
                    errorHandler.onError(
                        "Error occurred! Please try again."
                    )
                }


            }
        }){
            request().collect {
                withContext(Dispatchers.Main) {
                    liveData.value = it
                    if(it is ApiResponse.Failure){
                        Log.d("ERROR_TOKEN",it.code.toString())
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mJob?.let {
            if (it.isActive)
                it.cancel()
            }
        }
    }


interface CoroutinesErrorHandler {
    fun onError(message:String)
}