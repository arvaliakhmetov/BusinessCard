package com.face.businesscard.navigation

import androidx.lifecycle.MutableLiveData
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