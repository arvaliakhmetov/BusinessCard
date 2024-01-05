package com.face.businessface.navigation

import FaceRecognitionProcessor
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.ui.details.HistoryScreenComponent
import com.face.businessface.ui.faceRegistration.FaceCreationComponent
import com.face.businessface.ui.face_detector.FaceRecognitionComponent
import com.face.businessface.ui.home.MainScreenComponent
import com.face.businessface.ui.home.NavBarItem
import com.face.businessface.ui.profile.ProfileScreenComponent
import com.face.businessface.ui.recognized_faces_screen.RecognizedFaceComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.MappedByteBuffer
import javax.inject.Inject


class RootComponent(
    componentContext: ComponentContext,
    private val cardInfoRepository: CardInfoRepository,
    private val recognitionModel: FaceRecognitionProcessor,
    private val faceNetModel: MappedByteBuffer
) : ComponentContext by componentContext {



    private val navigation = StackNavigation<Configuration>()
    val childStack = childStack(
        source = navigation,
        serializer = Configuration.serializer(),
        initialConfiguration = Configuration.MainScreen,
        handleBackButton = true,
        childFactory = ::createChild
    )
    val showNavBar = MutableValue(true)

    fun selectNavBar(navBarItem: NavBarItem){
        try {
            when (navBarItem) {
                NavBarItem.Home -> {
                    navigation.replaceCurrent(Configuration.MainScreen)
                }

                NavBarItem.History -> {
                    navigation.replaceCurrent(Configuration.HistoryScreen)
                }

                NavBarItem.Profile -> {
                    navigation.replaceCurrent(Configuration.ProfileScreen)
                }
            }
        }catch (_ : Throwable){}

    }

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createChild(
        config: Configuration,
        context: ComponentContext
    ): Child {
        return when(config) {
            Configuration.MainScreen -> Child.MainScreen(
                MainScreenComponent(
                    componentContext = context,
                    onNavigateToFaceRecognitionScreen = {
                            navigation.pushNew(Configuration.FaceRecognitionScreen)
                    }
                )
            )

            Configuration.HistoryScreen -> Child.HistoryScreen(
                HistoryScreenComponent(
                    componentContext = context,
                    cardInfoRepository = cardInfoRepository,
                    onNavigateToRecognizedFaceScreen = { id ->
                        navigation.pushNew(Configuration.RecognizedFaceScreen(id = id))
                    }
                )
            )

            Configuration.ProfileScreen -> Child.ProfileScreen(
                ProfileScreenComponent(
                    componentContext = context,
                    onNavigateToCardCreation = {
                        navigation.pushNew(Configuration.FaceCreationScreen)
                    }
                )
            )

            Configuration.FaceRecognitionScreen -> Child.FaceRecognitionScreen(
                FaceRecognitionComponent(
                    componentContext = context,
                    cardInfoRepository = cardInfoRepository,
                    model = recognitionModel,
                    onNavigateToRecognizedFace = {
                        navigation.pushNew(Configuration.RecognizedFaceScreen(id = it))
                    },
                    onClose = {
                        navigation.popTo(0)
                    }
                )
            )

            is Configuration.RecognizedFaceScreen -> Child.RecognizedFaceScreen(
                RecognizedFaceComponent(
                    personId = config.id,
                    componentContext = context,
                    cardInfoRepository = cardInfoRepository,
                    onClose = {
                        navigation.popTo(0)
                    }
                )
            )

            Configuration.FaceCreationScreen -> Child.FaceCreationScreen(
                FaceCreationComponent(
                    model = faceNetModel,
                    componentContext = context,
                    cardInfoRepository = cardInfoRepository,
                    onSaveCard = {
                        navigation.pushNew(Configuration.ProfileScreen)
                    }
                )
            )
        }
    }

    sealed class Child {

        data class MainScreen(val component: MainScreenComponent) : Child()
        data class FaceRecognitionScreen(val component: FaceRecognitionComponent): Child()
        data class RecognizedFaceScreen(val component: RecognizedFaceComponent): Child()

        data class FaceCreationScreen(val component: FaceCreationComponent): Child()

        data class HistoryScreen(val component: HistoryScreenComponent): Child()

        data class ProfileScreen(val component: ProfileScreenComponent): Child()
    }



    @Serializable
    sealed class Configuration {
        @Serializable
        data object MainScreen:Configuration()

        @Serializable
        data object HistoryScreen: Configuration()

        @Serializable
        data object ProfileScreen: Configuration()
        @Serializable
        data object FaceRecognitionScreen: Configuration()

        @Serializable
        data object FaceCreationScreen: Configuration()

        @Serializable
        data class RecognizedFaceScreen(val id: Long): Configuration()
    }
}

fun ComponentContext.componentCoroutineScope(dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + dispatcher)

    if (this.lifecycle.state != Lifecycle.State.DESTROYED) {
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    } else {
        scope.cancel()
    }

    return scope
}
