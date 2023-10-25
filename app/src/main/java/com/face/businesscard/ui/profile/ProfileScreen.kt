package com.face.businesscard.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.ui.face_recognizer.Person
import com.face.businesscard.ui.theme.BusinessCardTheme

@Composable
fun ProfileScreen(
    faces: List<CardInfo>,
    deleteCard: (Long) -> Unit,
    navigateToFaceRegistrationScreen: () ->Unit,
){
    val scrollstate = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 18.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.solar_settings_outline), contentDescription = null)
                }
            }
        }
    ) {innerPadding->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(scrollstate)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp,bottom = 32.dp)
            ) {
                if(faces.isEmpty()) {
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 36.dp),
                        shape = RectangleShape,
                        onClick = navigateToFaceRegistrationScreen
                    ) {
                        Text(
                            text = "Создать визитку",
                            style = TextStyle(
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(500),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                }else{
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        faces.forEach {
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                                .clickable {
                                    //navigateToRecognizedface("1", listOf(Person("1", faceVector = it.arrayOfFeatures.first(),null)))
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .weight(1f),
                                        text = "Визитка",
                                        textAlign = TextAlign.Start,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(400),
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                        )
                                    )
                                    IconButton(onClick = { deleteCard(it.id!!) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                                Text(
                                    modifier = Modifier
                                        .padding(start = 20.dp, top = 5.dp),
                                    text = it.id.toString(),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        fontFamily = FontFamily(Font(R.font.inter)),
                                        fontWeight = FontWeight(300),
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = (it.surname) + " " + (it.name),
                                        textAlign = TextAlign.End,
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            lineHeight = 20.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(700),
                                            color = Color.White,
                                            textAlign = TextAlign.Right,
                                        )
                                    )

                                }
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                BasicTextField(
                    value = "test@test@mail.kz",
                    readOnly = true,
                    onValueChange = {},
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.inter)),
                        fontWeight = FontWeight(400),
                        color = Color.White,
                        letterSpacing = 0.15.sp,
                    ),
                    decorationBox = {innerTextField ->
                        Column {
                            innerTextField.invoke()
                            Divider(Modifier.padding(top = 16.dp))
                        }
                    }
                )

                Text(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .clickable {
                        },
                    text = "Выйти из аккаунта",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily(Font(R.font.inter)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF858585),

                        textAlign = TextAlign.Center,
                    )
                )

            }

        }
    }
}

/*
@Preview
@Composable
fun ProfilePreview(){
    BusinessCardTheme {
        ProfileScreen(faces = emptyList(), navigateToFaceRegistrationScreen = {})
    }
}*/
