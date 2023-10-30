package com.face.businesscard.ui.recognized_faces_screen


import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import com.face.businesscard.R
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.ui.face_recognizer.Person

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecognizedFacesScreen(
    person: PersonDto?,
    navigateBack:() -> Unit,
    viewModel: RecognizedFacesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val horizontal = rememberScrollState()
    val window = LocalConfiguration.current

    SideEffect {
        person?.let {
            viewModel.getImage(person.id.toString())
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),

    ) {
        Column (
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color.Black),
            verticalArrangement = Arrangement.Top
        ){

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
                    person?.let {
                        AsyncImage(
                            model = "http://154.194.53.89:8000/get_image/?id=${it.id}",
                            contentDescription = null,
                            modifier = Modifier
                                .height((window.screenHeightDp / 2).dp),
                            contentScale = ContentScale.FillHeight
                        )

                    }
                    BoxWithConstraints(
                        modifier = Modifier
                            .background(gradient)
                            .fillMaxWidth()
                            .height((window.screenHeightDp / 2).dp),
                        contentAlignment = Alignment.BottomStart
                    ){
                        person?.let { face ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                            ) {
                                val name1 =
                                    (face.surname ?: "") + " " +
                                            (face.name ?: "") + " " +
                                            (face.second_name ?: "")
                                Text(
                                    text = name1,
                                    color = Color.White,
                                    style = TextStyle(
                                        fontSize = 36.sp,
                                        lineHeight = 40.sp,
                                        fontFamily = FontFamily(Font(R.font.inter)),
                                        fontWeight = FontWeight(700),
                                        color = Color.White,
                                    ),
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
                                onClick = navigateBack
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
                                IconButton(onClick = navigateBack) {
                                    Icon(
                                        modifier = Modifier.size(64.dp),
                                        imageVector = ImageVector.vectorResource(R.drawable.back__1_),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                person?.let{ face ->


                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .padding(vertical = 13.dp)
                                .fillMaxWidth()
                                .horizontalScroll(horizontal),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            face.activities.forEach {
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
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                        var showMore by remember { mutableStateOf(false) }

                        Column {
                            Column(modifier = Modifier
                                .animateContentSize(animationSpec = tween(100))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showMore = !showMore }) {

                                if (showMore) {
                                    Text(
                                        text = face.description?:"",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(400),
                                            color = Color.White
                                        ))
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
                                modifier = Modifier.padding(vertical = 16.dp),
                                text = "Ссылки",
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    lineHeight = 32.sp,
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
                                        val urlIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(entry.value)
                                        )
                                        context.startActivity(urlIntent)
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
        }



}

fun defaultBitmap():ImageBitmap{
    val bitmap = ImageBitmap(50,50, config = ImageBitmapConfig.Argb8888)
    val redPaint = Paint()
    redPaint.color = Color.Cyan
    val canvas = Canvas(bitmap)
    canvas.drawRect(0F,0F,50F,50F,redPaint)
    return bitmap
}



/*@Preview
@Composable
fun RecognizedFacesScreenPreview(){
    MaterialTheme(
        colorScheme = LightColorScheme,
    ) {
        val bitmap = ImageBitmap(50,50, config = ImageBitmapConfig.Argb8888)
        val redPaint = Paint()
        redPaint.color = Color.Cyan
        val canvas = Canvas(bitmap)
        canvas.drawRect(0F,0F,50F,50F,redPaint)
        RecognizedFacesScreen(
            "1",
            listOf(Person("1",FloatArray(1),bitmap))
        ){}
    }
}*/
