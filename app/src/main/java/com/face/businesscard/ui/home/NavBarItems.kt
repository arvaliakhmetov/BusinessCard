package com.face.businesscard.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.List
import androidx.compose.material.icons.twotone.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavBarItems(val label: String,val icon: ImageVector) {
    Home("home", Icons.TwoTone.Home),
    History("history", Icons.TwoTone.List),
    Profile("profile", Icons.TwoTone.Person)
}