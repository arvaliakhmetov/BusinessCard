package com.face.businesscard.ui.recognized_faces_screen


import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.face.businesscard.R
import com.face.businesscard.ui.face_recognizer.Person

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecognizedFacesScreen(
    selectedFace: String,
    faces: List<Person?>,
    navigateBack:() -> Unit,
    viewModel: RecognizedFacesViewModel = hiltViewModel()
) {
    val foundedFace by viewModel.foundedFace.collectAsState()
    val currentFace by viewModel.currentFaceV.collectAsState()
    val context = LocalContext.current
    val contacts = hashMapOf(
        "YouTube" to "https://www.youtube.com/watch?v=bKDdT_nyP54&ab_channel=AkonVEVO",
        "Вконтакте" to "https://vk.com/durov",
        "Фирменный рецепт" to "https://povar.ru/recipes/syrniki_ot_iulii_vysockoi-43099.html"
    )
    val scrollState = rememberScrollState()
    val window = LocalConfiguration.current
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            initialValue = if (faces.size <= 1) SheetValue.Hidden else SheetValue.PartiallyExpanded,
            skipHiddenState = faces.size > 1,
            skipPartiallyExpanded = faces.size < 1
        )
    )

    SideEffect {
        viewModel.findFace(faces.find { it?.id == selectedFace })
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black))
    BottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        scaffoldState = sheetState,
        sheetDragHandle = {
            if (faces.size > 1) {
                Text(
                    modifier = Modifier.padding(vertical = 12.dp),
                    text = "Другие определенные лица",
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily(Font(R.font.inter)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF858585),
                    )
                )
            }
        },
        sheetContent = {
            if (faces.size > 1) {
                LazyRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(faces.size) { face ->
                            Image(
                                bitmap = (faces[face]!!.faceBitmap!!),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.resetFace()
                                        viewModel.findFace(faces[face])
                                    },
                                contentScale = ContentScale.FillWidth
                            )

                    }
                }
            }
        }
    ) {
        Column (Modifier.fillMaxSize().verticalScroll(scrollState)
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
                    Image(
                        modifier = Modifier
                            .height((window.screenHeightDp / 2).dp)
                            .blur(20.dp),
                        bitmap = currentFace?.faceBitmap?: defaultBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight
                    )
                    Box(
                        modifier = Modifier
                            .background(gradient)
                            .fillMaxWidth()
                            .height((window.screenHeightDp / 2).dp)
                    )
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
                foundedFace?.let{ face ->
                    Box(modifier = Modifier
                        .padding(horizontal = 16.dp)
                    ){
                        val name1 =
                            (foundedFace?.surname?:"") + " " +
                                    (foundedFace?.name?:"") + " " +
                                    (foundedFace?.secondName?:"")
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

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .padding(vertical = 13.dp)
                                .fillMaxWidth(),
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
                                        text = foundedFace?.description?:"",
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
                        if (face.links.isNotEmpty()) {
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
                            face.links.forEach {
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
                                            Uri.parse(it.link)
                                        )
                                        context.startActivity(urlIntent)
                                    }) {
                                        Text(
                                            text = it.name, style = TextStyle(
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
