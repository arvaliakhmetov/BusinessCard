package com.face.businesscard.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeDataButtonToNavigateToDetail(navigateToDetail: () -> Unit) {
  Button(
    onClick = { navigateToDetail() },
    modifier = Modifier.padding(16.dp)
  ) {
    Text("Set Shared Data")
  }
}