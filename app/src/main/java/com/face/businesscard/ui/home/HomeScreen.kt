package com.face.businesscard.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.face.businesscard.R
import com.face.businesscard.ui.theme.BusinessCardTheme

@Composable
fun HomeScreen(
  navigateToFaceDetector: () -> Unit,
) {
  Scaffold(
    modifier = Modifier.background(Color.Black),
    topBar = {
      Text(
        modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
        text = "Главная",
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
    ConstraintLayout(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      val (imgButton,text) = createRefs()
      Image(
        imageVector = ImageVector.vectorResource(id = R.drawable.ellipse),
        contentDescription = null,
        modifier = Modifier
          .constrainAs(imgButton) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
          }
          .clip(CircleShape)
          .clickable {
            navigateToFaceDetector.invoke()
          }
      )
      Text(
        text = "Нажмите, чтобы начать поиск",
        style = TextStyle(
          fontSize = 16.sp,
          lineHeight = 28.sp,
          fontFamily = FontFamily(Font(R.font.inter)),
          fontWeight = FontWeight(500),
          color = Color(0xFF858585),
          textAlign = TextAlign.Center,
        ),
        modifier = Modifier.constrainAs(text){
          top.linkTo(parent.top)
          bottom.linkTo(imgButton.top)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }
      )
    }
  }

}

@Preview(showBackground = true)
@Composable
fun PreviewHome(){
  BusinessCardTheme {
    HomeScreen(
      navigateToFaceDetector = {}
    )
  }
}