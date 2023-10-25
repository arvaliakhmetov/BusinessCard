package com.face.businesscard.ui.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.face.businesscard.R
import com.face.businesscard.ui.face_recognizer.Person
import com.face.businesscard.ui.recognized_faces_screen.defaultBitmap
import com.face.businesscard.ui.theme.BusinessCardTheme

@Composable
fun DetailScreen(
  shareData: List<Person?>?,
 // navigateToRecognizedFace:(String,List<Person?>)->Unit,
) {

  val scrollState = rememberScrollState()
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(scrollState)
    ) {
      shareData?.forEach { person ->
        Row(
          modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clickable {
              //navigateToRecognizedFace(person!!.id, listOf(person))
            },
          verticalAlignment = Alignment.Top
        ) {
          Image(
            bitmap = person!!.faceBitmap?: defaultBitmap(),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
            )
          Column (
            modifier = Modifier.padding(start = 10.dp, top = 6.dp)
          ){
            Text(text = "Иванов Александр ")
            Row(
              Modifier.padding(top = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ){
              Text(
                text = "Инвестиции",
                style = TextStyle(
                  fontSize = 14.sp,
                  lineHeight = 14.sp,
                  fontFamily = FontFamily(Font(R.font.inter)),
                  fontWeight = FontWeight(400),
                  color = Color(0xFF858585),
                )
              )
              Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(8.dp))
              Text(
                text = "Финансы",
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

@Preview(showBackground = true)
@Composable
fun PreviewHome(){
  val bitmap = ImageBitmap(50,50, config = ImageBitmapConfig.Argb8888)
  val redPaint = Paint()
  redPaint.color = Color.Cyan
  val canvas = Canvas(bitmap)
  canvas.drawRect(0F,0F,50F,50F,redPaint)
  BusinessCardTheme {
    DetailScreen(listOf(Person("1", FloatArray(1),bitmap)))
  }
}