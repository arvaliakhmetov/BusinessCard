package com.face.businesscard.ui.face_detector

import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.face.businesscard.R
import com.face.businesscard.ui.face_recognizer.Person
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.common.FileUtil


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanSurface(
    navigateBack:() -> Unit,
    navigateToRecognizedFacesScreen:(id: String,List<Person?>) -> Unit,
    viewModel: FaceRecognitionViewModel = hiltViewModel(),
) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val context = LocalContext.current
    val working = viewModel.working
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentSheetValue = remember {
        SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    }
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = currentSheetValue
    )
    var stopCamera by remember { mutableStateOf(false) }
    val recognitionModel = FileUtil.loadMappedFile(context, "mobile_face_net.tflite")
    val scope = rememberCoroutineScope()
    val window = LocalConfiguration.current
    val detectorInitialize = "Поиск лиц активен"
    val detectorStop = "Поиск остановлен"

    //val screenWidth = remember { mutableStateOf(context.resources.displayMetrics.widthPixels) }
    //val screenHeight = remember { mutableStateOf(context.resources.displayMetrics.heightPixels) }
    //var bitmap = remember{ mutableStateOf<Bitmap?>(null) }
    val faceBitmapList by viewModel.faceSLIST.collectAsState()
    //val imageWidth = remember { mutableStateOf(0) }
    // val imageHeight = remember { mutableStateOf(0) }
    LaunchedEffect(sheetState.bottomSheetState.currentValue) {
        val isExpanded = sheetState.bottomSheetState.currentValue == SheetValue.Expanded
        delay(500)
        viewModel.needAnalyzer(!isExpanded)
    }
    LaunchedEffect(stopCamera) {
        currentSheetValue = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    }

    SideEffect {
        if (viewModel.faceProcessor == null) viewModel.setRecognitionModel(recognitionModel)
    }




    BottomSheetScaffold(
        sheetDragHandle = {
            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = "Найдено лиц: ${faceBitmapList.size}",
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily(Font(R.font.inter)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF858585),
                )
            )
        },
        sheetSwipeEnabled = true,
        scaffoldState = sheetState,
        sheetPeekHeight = 70.dp,
        sheetContent = {
            BoxWithConstraints(
                modifier = Modifier.defaultMinSize(minHeight = (window.screenHeightDp / 2).dp),
                contentAlignment = Alignment.TopStart
            ) {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    faceBitmapList.forEach { person ->
                        person?.faceBitmap?.let {
                            Box(modifier = Modifier) {
                                Image(
                                    bitmap = it,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clickable {
                                            scope
                                                .launch {
                                                    sheetState.bottomSheetState.partialExpand()
                                                }
                                                .invokeOnCompletion {
                                                    navigateToRecognizedFacesScreen(
                                                        person.id,
                                                        faceBitmapList
                                                    )
                                                }
                                        }
                                        .clip(CircleShape),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    }

                }
            }

        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraView(
                context = context,
                lensFacing = lensFacing,
                lifecycleOwner = lifecycleOwner,
                analyzer = viewModel.analyze(lensFacing)
            ) //DrawFaces(faces = facesList, imageHeight.value, imageWidth.value, screenWidth.value, screenHeight.value)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = !viewModel.needAnalyzer.collectAsState().value,
                    enter = slideInVertically { -it }.plus(fadeIn()),
                    exit = slideOutVertically { -it }.plus(fadeOut())
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 24.dp)
                            .background(Color.White, RoundedCornerShape(20.dp)),
                        text = "  Поиск остановлен  ",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily(Font(R.font.inter)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF000000),
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.padding(20.dp),
                    onClick = {
                        // Toggle between front and back lenses
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.switch_icon),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.size(64.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}



