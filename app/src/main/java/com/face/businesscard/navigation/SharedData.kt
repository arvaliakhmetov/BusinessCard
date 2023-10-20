package com.face.businesscard.navigation

sealed class SharedData<T> {
    data class Value<T>(val data: T) : SharedData<T>()
}