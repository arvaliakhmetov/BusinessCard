package com.face.businessface.ui.recognized_faces_screen


sealed class RecognizedFaceScreenAction {

    data object OnFavoriteClick: RecognizedFaceScreenAction()

    data object OnDelete: RecognizedFaceScreenAction()

    data object OnClose: RecognizedFaceScreenAction()

    data object Init: RecognizedFaceScreenAction()
}