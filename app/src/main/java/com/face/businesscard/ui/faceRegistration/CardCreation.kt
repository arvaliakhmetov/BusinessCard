package com.face.businesscard.ui.faceRegistration


import FaceDirection
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.sharp.ArrowForward
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.face.businesscard.R
import com.face.businesscard.ui.ApiResponse
import com.face.businesscard.ui.Utils.LabeledInputWithDivider
import com.face.businesscard.ui.face_detector.CameraView
import com.face.businesscard.ui.face_detector.FaceRecognitionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import org.tensorflow.lite.support.common.FileUtil
import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CardCreation(
    navigateToMain: () -> Unit,
    viewModel: CardCreationViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember{ mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    val recognitionModel = FileUtil.loadMappedFile(context, "mobile_face_net.tflite")
    val loading by viewModel.showLoader.collectAsState()
    val credsStep by viewModel.showCredsScreen.collectAsState()
    val closeCamera by viewModel.closeCamera.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0){3}
    val credsState by viewModel.credsState.collectAsState()
    val showContentFilter by viewModel.showContentFilter.collectAsState()
    val itemsList by viewModel.itemsList.collectAsState()
    val scrollState = rememberScrollState()
    val faceNotRecognized by viewModel.faceNotRecognised.collectAsState()
    val focusManager = LocalFocusManager.current
    val dotsNeedColor by viewModel.sizeOfrecognizedPoses.collectAsState()
    val saveRespons by viewModel.createCardresponse.collectAsState()

    LaunchedEffect(saveRespons){
        if(saveRespons is ApiResponse.Success){
            Toast.makeText(context,"Визитка успешно создана",Toast.LENGTH_SHORT).show()
            navigateToMain.invoke()

        }
    }




    SideEffect {
        if (viewModel.faceProcessor == null) viewModel.setRecognitionModel(recognitionModel)
    }
    LaunchedEffect(credsStep){
        if(credsStep){
            pagerState.animateScrollToPage(1)
        }
    }
    LaunchedEffect(faceNotRecognized){
        if(faceNotRecognized){
            Toast.makeText(context,"Требуется повторная проверка",Toast.LENGTH_SHORT).show()
        }
    }

    HorizontalPager(
        modifier = Modifier,
        userScrollEnabled = false,
        state = pagerState
    ) { page ->
        when (page) {
            0 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraView(
                        context = context,
                        analyzer = viewModel.analyze(lensFacing),
                        lensFacing = lensFacing,
                        lifecycleOwner = lifecycleOwner,
                        cameraExit = closeCamera,
                        imageCapture = null,
                        showScreenSHot = {}
                    )
                    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
                        val circlePath = Path().apply {
                            addOval(Rect(center, size.minDimension / 2.4f))
                        }
                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(SolidColor(Color.Black.copy(alpha = 1f)))
                        }
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.minDimension / 2.1f
                        val startAngle = 90f

                        // Define a brush for the lines
                        val lineBrush =
                            Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.LightGray))
                        val lineBrush1 =
                            Brush.horizontalGradient(colors = listOf(Color.Green, Color.Green))
                        val numPoints = 40



                        // Calculate and draw the points on the circle
                        for (i in 0 until numPoints) {
                            val angle = startAngle+ (i * 360f / numPoints).toFloat()
                            val pointX = centerX + radius * cos(PI * angle / 180f).toFloat()
                            val pointY = centerY + radius * sin(PI * angle / 180f).toFloat()
                                drawCircle(
                                    brush = lineBrush,
                                    center = Offset(pointX, pointY),
                                    radius = 6f,
                                    style = Stroke(6f)
                                )

                        }
                        for (i in 0..dotsNeedColor){
                            val angle = startAngle+ (i * 360f / numPoints).toFloat()
                            val pointX = centerX + radius * cos(PI * angle / 180f).toFloat()
                            val pointY = centerY + radius * sin(PI * angle / 180f).toFloat()
                            drawCircle(
                                brush = lineBrush1,
                                center = Offset(pointX, pointY),
                                radius = 6f,
                                style = Stroke(6f)
                            )
                        }
                    })
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick =navigateToMain) {
                                Icon(
                                    modifier = Modifier.size(64.dp),
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White)
                            }
                        }
                        Text(
                            modifier = Modifier,
                            text = "Отсканируйте своё лицо",
                            style = TextStyle(
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(700),
                                color = Color.White,

                                )
                        )
                    }
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 40.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(loading){
                                CircularProgressIndicator(
                                    color = Color.White
                                )
                                Text(
                                    modifier = Modifier.padding(start = 16.dp),
                                    text = "Проверяем",
                                    style = TextStyle(fontSize = 18.sp,
                                    lineHeight = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.inter)),
                                    fontWeight = FontWeight(400),
                                    color = Color.White,
                                    letterSpacing = 0.15.sp,
                                ))
                            }
                        }
                    }
                }
            }
            1->{
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    topBar = {
                        IconButton(
                            modifier = Modifier.padding(vertical = 16.dp),
                            onClick = navigateToMain
                        ) {
                            Icon(
                                imageVector = Icons.Sharp.Close,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    },
                    floatingActionButton = {
                        if(showContentFilter && pagerState.currentPage == 1) {
                            FloatingActionButton(
                                modifier = Modifier.padding(bottom= 60.dp),
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                shape = CircleShape,
                                onClick = {
                                    scope.launch {
                                        focusManager.clearFocus(true)
                                        delay(200)
                                        pagerState.animateScrollToPage(2)
                                        viewModel.showContentFilter()
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Sharp.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(it)
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Расскажите о себе",
                            modifier = Modifier.padding(bottom=20.dp),
                            style = TextStyle(
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(700),
                                color = Color.White,
                            )
                        )
                        LabeledInputWithDivider(
                            labelText = "Имя*",
                            value = credsState.name,
                            hint = "Иван",
                            onValueChange = { viewModel.setCreds(credsState.copy(name = it)) },
                            singleLine = true,
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "Фамилия*",
                            value = credsState.surname,
                            hint = "Иванов",
                            singleLine = true,
                            onValueChange = { viewModel.setCreds(credsState.copy(surname = it)) },
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "Отчество",
                            value = credsState.secondName,
                            hint = "Иванович",
                            singleLine = true,
                            onValueChange = { viewModel.setCreds(credsState.copy(secondName = it)) },
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "О себе*",
                            value = credsState.description,
                            hint = "Главный заместитель чат бота \"Олег\" в Тинькофф...",
                            singleLine = false,
                            onValueChange = { viewModel.setCreds(credsState.copy(description = it)) },
                            readOnly = false
                        )
                        Text(
                            text = "Ссылки",
                            modifier = Modifier.padding(top = 12.dp),
                            style = TextStyle(
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(700),
                                color = Color.White,
                            )
                        )
                        credsState.links.forEach { (i, pair) ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = i.toString())
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.deleteLink(i)}) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                                }
                            }
                            LabeledInputWithDivider(
                                labelText = "Название",
                                value = pair.name,
                                hint = "Курс: 1 миллион за секунду",
                                singleLine = true,
                                onValueChange = {
                                        viewModel.addNameToLink(i, it)
                                },
                                readOnly = false
                            )
                            LabeledInputWithDivider(
                                labelText = "Ссылка",
                                value = pair.link,
                                hint = "https://www.google.com/",
                                singleLine = true,
                                onValueChange = {
                                    viewModel.addLinkToLink(i, it)

                                },
                                readOnly = false
                            )
                        }
                        TextButton(onClick = viewModel::createLink) {
                            Text(
                                text = "+ Добавить",
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
                }
            }
            2 ->{
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    topBar = {
                             IconButton(
                                 modifier = Modifier.padding(vertical = 16.dp),
                                 onClick = { scope.launch { 
                                     pagerState.animateScrollToPage(1)
                                 } }
                             ) {
                                 Icon(
                                     imageVector = Icons.Sharp.KeyboardArrowLeft,
                                     contentDescription = null,
                                     modifier = Modifier.size(64.dp)
                                 )
                             }
                    },
                    bottomBar = {
                        if(itemsList.values.contains(true)){
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.saveCard()
                                    }
                                ) {
                                    Text(text = "Создать визитку",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(600),
                                            color = Color(0xFF000000),

                                            textAlign = TextAlign.Center,
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(it)
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Сферы деятельности",
                            modifier = Modifier.padding(bottom = 16.dp),
                            style = TextStyle(
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(700),
                                color = Color.White,
                            )
                        )
                        Text(
                            text = "Выберите от 1 до 3 направлений",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 28.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFF858585),
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier.padding(bottom = 40.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsList.forEach { item->
                                Row(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (item.value) Color.White else Color.Black,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                                        .clickable {
                                            viewModel.updateSelection(
                                                item
                                                    .toPair()
                                                    .copy(second = !item.value)
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically

                                ) {
                                    Text(
                                        text = item.key,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(600),
                                            color = if(item.value) Color.Black else Color.White,

                                            textAlign = TextAlign.Center,
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

@Composable
fun DirectionDots(
    list: MutableList<FaceDirection>
){
    list.forEachIndexed { i, faceDirection ->
        Canvas(modifier = Modifier.fillMaxSize()) {
            val numPoints = 40
            val lineBrush =
                Brush.horizontalGradient(colors = listOf(Color.Green, Color.Green))
            val lineBrushTest =
                Brush.horizontalGradient(colors = listOf(Color.Red, Color.Red))
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2.1f
            val direction = FaceDirection.entries[i]
            val angle = (i * 360f / numPoints).toFloat()
            val pointX = centerX + radius * cos(PI * angle / 180f).toFloat()
            val pointY = centerY + radius * sin(PI * angle / 180f).toFloat()
            Log.d("angele", angle.toString())
            drawCircle(
                brush = lineBrushTest,
                center = Offset(pointX, pointY),
                radius = 6f,
                style = Stroke(6f)
            )
        }
    }
}

val spheresOfActivity = arrayOf(
    "Финансы",
    "IT",
    "Юриспруденция",
    "Здравоохранение",
    "Образование",
    "Маркетинг",
    "Производство",
    "Искусство",
    "Спорт",
    "Туризм",
    "Наука",
    "Сельское хозяйство"
)
