package com.face.businessface.navigation

import kotlinx.coroutines.flow.MutableStateFlow

class SharedDataRepository {
  private val sharedData: MutableStateFlow<SharedData.Value<*>?> = MutableStateFlow(null)

  fun setData(data: SharedData.Value<*>) {
    sharedData.value = data
  }

  fun getShareData(): SharedData.Value<*>? {
    return sharedData.value
  }
}