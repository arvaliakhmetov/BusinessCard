package com.face.businessface.ui.faceRegistration


import FaceDirection
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.sharp.ArrowForward
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.face.businessface.R
import com.face.businessface.ui.Utils.LabeledInputWithDivider
import com.face.businessface.ui.face_detector.CameraView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin
import java.nio.MappedByteBuffer

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FaceCreationScreen(
    component: FaceCreationComponent
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showCamera = remember{ mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember{ mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    val loading by component.showLoader.collectAsState()
    val credsStep by component.showCredsScreen.collectAsState()
    val closeCamera by component.closeCamera.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0){3}
    val credsState by component.credsState.collectAsState()
    val showContentFilter by component.showContentFilter.collectAsState()
    val itemsList by component.itemsList.collectAsState()
    val scrollState = rememberScrollState()
    val faceNotRecognized by component.faceNotRecognised.collectAsState()
    val focusManager = LocalFocusManager.current
    val target by component.recognitionIterator.collectAsState()
    var targetList by remember{ mutableStateOf(listOf(FaceDirection.FACE_FAR)) }
    val saveRespons by component.createCardresponse.collectAsState()
    val userImage by component.userImage.collectAsState()
    val smile by component.smile.collectAsState()

    val transition = rememberInfiniteTransition()

    val alpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ), label = "asd"
    )

    val imageCropLauncher = rememberLauncherForActivityResult(
        CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            val bitmap = if (Build.VERSION.SDK_INT <= 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, croppedUri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, croppedUri!!)
                ImageDecoder.decodeBitmap(source)
            }
            bitmap?.let {
                component.userImage.value = it
            }
        } else {
            // an error occurred cropping
            val exception = result.error
        }
    }

    val cropOptions = CropImageOptions(
        cropShape = CropImageView.CropShape.RECTANGLE,
        aspectRatioX = 1,
        aspectRatioY = 1,
        fixAspectRatio = true,
        backgroundColor = Color.Black.toArgb(),
        activityBackgroundColor = Color.Black.toArgb()
    )


    LaunchedEffect(target){
        targetList = target
    }


    SideEffect {
        scope.launch {
            if (component.faceProcessor == null) component.setRecognitionModel()
            showCamera.value = true
        }
    }
    LaunchedEffect(credsStep){
        if(credsStep){
            pagerState.animateScrollToPage(1)
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
                    if(showCamera.value) {
                        CameraView(
                            context = context,
                            analyzer = component.analyze(lensFacing),
                            lensFacing = lensFacing,
                            lifecycleOwner = lifecycleOwner,
                            cameraExit = closeCamera,
                            showScreenSHot = {},
                            scale = PreviewView.ScaleType.FIT_CENTER
                        ){}
                    }
                    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
                        val circlePath = Path().apply {
                            addOval(Rect(center, size.minDimension / 2.4f))
                        }
                        val centerX = size.width / 2
                        val centerY = size.height / 2

                        val lineBrush =
                            Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.LightGray))
                        val lineBrush1 =
                            Brush.horizontalGradient(colors = listOf(Color.Green, Color.Green))

                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(SolidColor(Color.Black.copy(alpha = 1f)))
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_RIGHT))lineBrush1 else lineBrush,
                                startAngle = 346f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_RIGHT) alpha else 0.8f
                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_RIGHT_BOTTOM_1))lineBrush1 else lineBrush,
                                startAngle = 16f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_RIGHT_BOTTOM_1) alpha else 0.8f

                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_RIGHT_BOTTOM_2))lineBrush1 else lineBrush,
                                startAngle = 46f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_RIGHT_BOTTOM_2) alpha else 0.8f

                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_BOTTOM))lineBrush1 else lineBrush,
                                startAngle = 76f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_BOTTOM) alpha else 0.8f

                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_LEFT_BOTTOM_2))lineBrush1 else lineBrush,
                                startAngle = 106f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_LEFT_BOTTOM_2) alpha else 0.8f

                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_LEFT_BOTTOM_1))lineBrush1 else lineBrush,
                                startAngle = 136f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_LEFT_BOTTOM_1) alpha else 0.8f

                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_LEFT))lineBrush1 else lineBrush,
                                startAngle = 166f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_LEFT) alpha else 0.8f
                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_LEFT_TOP_1))lineBrush1 else lineBrush,
                                startAngle = 196f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_LEFT_TOP_1) alpha else 0.8f
                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_LEFT_TOP_2))lineBrush1 else lineBrush,
                                startAngle = 226f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_LEFT_TOP_2) alpha else 0.8f
                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_TOP))lineBrush1 else lineBrush,
                                startAngle = 256f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_TOP) alpha else 0.8f
                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_RIGHT_TOP_1))lineBrush1 else lineBrush,
                                startAngle = 286f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_RIGHT_TOP_1) alpha else 0.8f
                            )
                            drawArc(
                                brush = if(targetList.contains(FaceDirection.FACE_RIGHT_TOP_2))lineBrush1 else lineBrush,
                                startAngle = 316f,
                                sweepAngle = 29f,
                                useCenter = true,
                                topLeft = Offset(centerX-size.minDimension / 2f+25f,centerY-size.minDimension / 2f+25f),
                                size = Size(size.minDimension-50f ,size.minDimension-50f ),
                                alpha = if(targetList.last() == FaceDirection.FACE_RIGHT_TOP_2) alpha else 0.8f
                            )
                            drawCircle(
                                brush = lineBrush,
                                center = Offset(centerX, centerY),
                                radius = if(targetList.last() == FaceDirection.FACE_CENTER) alpha else 0.8f,
                                style = Stroke(6f)
                            )
                            drawCircle(
                                brush = lineBrush,
                                center = Offset(centerX, centerY),
                                radius = if(targetList.last().name.contains(FaceDirection.FACE_EXTRA.name)) alpha else 0.8f,
                                style = Stroke(6f)
                            )
                            drawCircle(
                                brush = lineBrush,
                                center = Offset(centerX, centerY),
                                radius = if(targetList.last().name.contains(FaceDirection.ANGLE_SMILE.name)) alpha else 0.8f,
                                style = Stroke(6f)
                            )
                        }
                    })


                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            faceNotRecognized?.let {
                                val text = when (faceNotRecognized) {
                                    FaceDirection.FACE_CLOSE -> "  Лицо слишком близко  "
                                    FaceDirection.FACE_FAR -> "  Лицо слишком далеко  "
                                    else -> ""
                                }

                                Text(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .background(
                                            Color.Black,
                                            RoundedCornerShape(20.dp)
                                        ),
                                    text = text,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = FontFamily(Font(R.font.inter)),
                                        fontWeight = FontWeight(400),
                                        color = Color.White,
                                        letterSpacing = 0.15.sp,
                                    )
                                )
                            }
                            if (smile) {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .background(
                                            Color.Black,
                                            RoundedCornerShape(20.dp)
                                        ),
                                    text = " Улыбнитесь) ",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = FontFamily(Font(R.font.inter)),
                                        fontWeight = FontWeight(400),
                                        color = Color.White,
                                        letterSpacing = 0.15.sp,
                                    )
                                )
                            }
                        }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = component::navigateOnSave) {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(if (faceNotRecognized != null) 5.dp else 0.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
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
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = "Поместите лицо в центр круга и, не отрывая взгляд от экрана, плавно направляйте нос в сторону мигающих сегментов",
                                textAlign = TextAlign.Center,
                                style = TextStyle(fontSize = 13.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.inter)),
                                    fontWeight = FontWeight(400),
                                    color = Color.White,
                                    letterSpacing = 0.15.sp,
                                ))
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
                            onClick = component::navigateOnSave
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
                                        component.showContentFilter()
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            focusManager.clearFocus(force = true)
                                            delay(400)
                                            imageCropLauncher.launch(
                                                CropImageContractOptions(
                                                    uri = null,
                                                    cropImageOptions = cropOptions
                                                )
                                            )
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if(userImage != null) {
                                        Image(
                                            bitmap = userImage!!.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                        )
                                    }else{
                                        Image(
                                            imageVector = ImageVector.vectorResource(id = R.drawable.phot),
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp)
                                        )
                                    }
                                    Text(
                                        text = if(userImage == null)"Добавить фотографию" else "Изменить фотографию",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.inter)),
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF858585),
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        LabeledInputWithDivider(
                            labelText = "Имя*",
                            value = credsState.name,
                            hint = "Иван",
                            onValueChange = { component.setCreds(credsState.copy(name = it)) },
                            singleLine = true,
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "Фамилия*",
                            value = credsState.surname,
                            hint = "Иванов",
                            singleLine = true,
                            onValueChange = { component.setCreds(credsState.copy(surname = it)) },
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "Отчество",
                            value = credsState.secondName,
                            hint = "Иванович",
                            singleLine = true,
                            onValueChange = { component.setCreds(credsState.copy(secondName = it)) },
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "Компания",
                            value = credsState.company,
                            hint = "ОАО ТРЕСТИНВЕСТ",
                            singleLine = true,
                            onValueChange = { component.setCreds(credsState.copy(company = it)) },
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "Должность",
                            value = credsState.jobtitle,
                            hint = "Менеджер",
                            singleLine = true,
                            onValueChange = { component.setCreds(credsState.copy(jobtitle = it)) },
                            readOnly = false
                        )
                        LabeledInputWithDivider(
                            labelText = "О себе*",
                            value = credsState.description,
                            hint = "Главный заместитель чат бота \"Олег\" в Тинькофф...",
                            singleLine = false,
                            onValueChange = { component.setCreds(credsState.copy(description = it)) },
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
                                IconButton(onClick = { component.deleteLink(i)}) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                                }
                            }
                            LabeledInputWithDivider(
                                labelText = "Название",
                                value = pair.name,
                                hint = "Курс: 1 миллион за секунду",
                                singleLine = true,
                                onValueChange = {
                                        component.addNameToLink(i, it)
                                },
                                readOnly = false
                            )
                            LabeledInputWithDivider(
                                labelText = "Ссылка",
                                value = pair.link,
                                hint = "https://www.google.com/",
                                singleLine = true,
                                onValueChange = {
                                    component.addLinkToLink(i, it)

                                },
                                readOnly = false
                            )
                        }
                        TextButton(onClick = component::createLink) {
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
                                        component.saveCard()
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
                                            component.updateSelection(
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
