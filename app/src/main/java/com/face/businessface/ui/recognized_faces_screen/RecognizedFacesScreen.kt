package com.face.businessface.ui.recognized_faces_screen


import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.face.businessface.R
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RecognizedFacesScreen(
    component: RecognizedFaceComponent,
    onAction: (action: RecognizedFaceScreenAction) -> Unit,
) {
    val state by component.state.subscribeAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val horizontal = rememberScrollState()
    val window = LocalConfiguration.current
    var showLoader = remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pass by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pass) {
        if ( pass == "DELETE" ) {
            onAction(RecognizedFaceScreenAction.OnDelete)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),

        ) {
        if (state.person?.id != -1L) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(Color.Black)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.Top
            ) {

                val localDensity = LocalDensity.current
                val gradient = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = (window.screenHeightDp / 3) * localDensity.density, // Starting Y position of the gradient
                    endY = (window.screenHeightDp / 2) * localDensity.density // End
                )
                Box(modifier = Modifier.fillMaxSize())
                BoxWithConstraints(
                    modifier = Modifier
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.TopCenter
                ) {
                    state.person?.let {
                        /*AsyncImage(
                            model = "http://154.194.53.89:8000/get_image/?id=${it.id}",
                            contentDescription = null,
                            modifier = Modifier
                                .height((window.screenHeightDp / 2).dp),
                            contentScale = ContentScale.FillHeight
                        )*/
                    }
                    BoxWithConstraints(
                        modifier = Modifier
                            .background(gradient)
                            .fillMaxWidth()
                            .height((window.screenHeightDp / 2).dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        state.person?.let { face ->
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 4.dp)
                            ) {
                                val name1 =
                                    (face.surname.removePrefix(" ") ?: "") + " " +
                                            (face.name.removePrefix(" ") ?: "") + " " +
                                            (face.second_name.removePrefix(" ") ?: "")
                                val job =
                                    state.person!!.jobtitile.removePrefix(" ") +
                                            (if (face.jobtitile.removePrefix(" ").isNotEmpty() && face.company.removePrefix(" ").isNotEmpty())
                                     " | " else "" ) + face.company.removePrefix(" ")
                                Text(
                                    text = name1.removePrefix(" "),
                                    color = Color.White,
                                    style = TextStyle(
                                        fontSize = 36.sp,
                                        lineHeight = 40.sp,
                                        fontFamily = FontFamily(Font(R.font.inter)),
                                        fontWeight = FontWeight(700),
                                        color = Color.White,
                                    ),
                                )
                                Text(
                                    text = job,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        lineHeight = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.inter)),
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFF858585),
                                    )
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        onAction(RecognizedFaceScreenAction.OnClose)
                                    }
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.size(36.dp),
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!showLoader.value) {
                                    IconButton(onClick = {
                                        showDeleteDialog = true
                                    }) {
                                        Icon(
                                            modifier = Modifier.size(32.dp),
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    state.person?.let {
                                        onAction(RecognizedFaceScreenAction.OnFavoriteClick)
                                    }

                                }) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        imageVector = ImageVector.vectorResource(R.drawable.back__1_),
                                        contentDescription = null,
                                        tint = if(state.person != null && state.person!!.favorite) Color.Red else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                state.person?.let { face ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .padding(top = 4.dp, bottom = 8.dp)
                                .fillMaxWidth()
                                .horizontalScroll(horizontal),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            face.activities.forEachIndexed { i, it ->
                                if (i != 0) {
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.tag_dot),
                                        contentDescription = null,
                                        alignment = Alignment.Center,
                                        modifier = Modifier.fillMaxHeight()
                                    )
                                }
                                Text(
                                    text = it,
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
                        var showMore by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier
                                .animateContentSize(animationSpec = tween(100))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showMore = !showMore }) {

                                if (showMore) {
                                    Text(
                                        text = face.description,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(400),
                                            color = Color.White
                                        )
                                    )
                                } else {
                                    Text(
                                        text = face.description,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(400),
                                            color = Color.White,
                                        )
                                    )
                                }
                            }
                        }
                        if (face.conts.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                text = "Ссылки",
                                style = TextStyle(
                                    fontSize = 27.sp,
                                    lineHeight = 28.sp,
                                    fontFamily = FontFamily(Font(R.font.inter)),
                                    fontWeight = FontWeight(700),
                                    color = Color.White,
                                )
                            )
                            face.conts.forEach { entry ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.iconoir_arrow_br),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    TextButton(onClick = {
                                        try {
                                            val urlIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(entry.value.lowercase(Locale.getDefault()))
                                            )
                                            context.startActivity(urlIntent)
                                        } catch (_: Throwable) {
                                            scope.launch {
                                                Toast.makeText(
                                                    context,
                                                    "Указана невалидная ссылка",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    }) {
                                        Text(
                                            text = entry.key, style = TextStyle(
                                                fontSize = 20.sp,
                                                lineHeight = 20.sp,
                                                fontFamily = FontFamily(Font(R.font.inter)),
                                                fontWeight = FontWeight(500),
                                                color = Color.White,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDE38)
                    )
                ) {
                    Text(text = "Перевод RUB")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Пользователь не найден",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily(Font(R.font.inter)),
                        fontWeight = FontWeight(500),
                        color = Color.White,
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                IconButton(
                    onClick = {
                        onAction(RecognizedFaceScreenAction.OnClose)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

        }
        if (showDeleteDialog) {
            Dialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    showLoader.value = false
                    pass = ""
                }
            ) {
                showLoader.value = true
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
                                Text(text = "Для удаления визитки напишите DELETE")
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