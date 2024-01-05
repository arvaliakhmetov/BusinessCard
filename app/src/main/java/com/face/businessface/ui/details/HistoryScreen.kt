package com.face.businessface.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.face.businessface.R

@Composable
fun HistoryScreen(
  component: HistoryScreenComponent
) {
  val persons by component.personsList.subscribeAsState()


  Scaffold(
    modifier = Modifier.background(Color.Black),
    topBar = {
      Text(
        modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
        text = "История поиска",
        style = TextStyle(
          fontSize = 28.sp,
          lineHeight = 28.sp,
          fontFamily = FontFamily(Font(R.font.inter)),
          fontWeight = FontWeight(700),
          color = Color.White,
        )
      )
    }
  ) {innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(innerPadding)
    ) {
      items(persons){ person ->
        Row(
          modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clickable {
              component.navigateToPerson(person.id!!)
            },
          verticalAlignment = Alignment.CenterVertically
        ) {
          AsyncImage(
            model = "http://154.194.53.89:8000/get_image/?id=${person.id}",
            contentDescription = null,
            modifier = Modifier
              .size(60.dp),
            contentScale = ContentScale.FillHeight
          )
          Column (
            modifier = Modifier.padding(start = 10.dp, top = 6.dp)
          ){
            val name1 =
              (person.surname.removePrefix(" ") ?: "") + " " +
                      (person.name.removePrefix(" ") ?: "") + " " +
                      (person.secondName.removePrefix(" ") ?: "")
            val job =
              person.jobtitle.removePrefix(" ") +
                      (if (person.jobtitle.removePrefix(" ").isNotEmpty() && person.company.removePrefix(" ").isNotEmpty())
                        " | " else "" ) + person.company.removePrefix(" ")
            Text(text = name1)
            Row(
              Modifier.padding(top = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                  text = job,
                  style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontFamily = FontFamily(Font(R.font.inter)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF858585),
                  )
                )
            }
          }

        }
      }
    }
  }

}