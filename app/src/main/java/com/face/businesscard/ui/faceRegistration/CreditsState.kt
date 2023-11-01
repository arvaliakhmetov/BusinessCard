package com.face.businesscard.ui.faceRegistration

data class CreditsState(
    val name: String = " ",
    val surname: String = " ",
    val secondName: String = " ",
    val company: String = " ",
    val jobtitle: String = " ",
    val description: String = "",
    val links: Map<Int,Link> = mapOf()
)
