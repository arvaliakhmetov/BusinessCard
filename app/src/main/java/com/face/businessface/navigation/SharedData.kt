package com.face.businessface.navigation

sealed class SharedData<T> {
    data class Value<T>(val data: T) : SharedData<T>()
}