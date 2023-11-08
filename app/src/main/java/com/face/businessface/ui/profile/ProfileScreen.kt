package com.face.businessface.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.face.businessface.R
import com.face.businessface.database.entity.CardInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navigateToFaceRegistrationScreen: () ->Unit,
) {
    var loader = remember { mutableStateOf(false) }
    val scrollstate = rememberScrollState()
    val scope = rememberCoroutineScope()
    var pass by remember { mutableStateOf("") }
    var showDialog by remember{ mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    LaunchedEffect(pass){
        if(pass == "Admin2023$") {
            focusManager.clearFocus(true)
            showDialog = false
            Toast.makeText(context,"Создание визитки разблокировано",Toast.LENGTH_SHORT).show()
            delay(500)
            navigateToFaceRegistrationScreen.invoke()
        }
    }
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
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.solar_settings_outline),
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(scrollstate)
                .blur(if (loader.value) 5.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 32.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 36.dp),
                    shape = RectangleShape,
                    onClick = {
                        showDialog = true
                    }
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
            }


            if (showDialog) {
                Dialog(
                    onDismissRequest = {
                        showDialog = false
                        pass = ""
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(5.dp))
                            .border(
                                0.5.dp, Color.White,
                                RoundedCornerShape(5.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = pass,
                                onValueChange = {
                                    pass = it
                                },
                                label = {
                                    Text(text = "Password")
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    fontFamily = FontFamily(Font(R.font.inter)),
                                    fontWeight = FontWeight(500),
                                    color = Color.White
                                ),
                                supportingText = {
                                    Text(text = "Для создания визитки обратитесь к представителю команды Reconnect")
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                ),
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                    }

                }
            }
        }
    }
}
