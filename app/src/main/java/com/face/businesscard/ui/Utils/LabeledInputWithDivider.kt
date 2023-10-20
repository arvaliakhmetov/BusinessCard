package com.face.businesscard.ui.Utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.face.businesscard.R

@Composable
fun LabeledInputWithDivider(
    labelText: String,
    value: String,
    hint:String,
    onValueChange:(String)-> Unit,
    readOnly: Boolean,
    singleLine: Boolean,
){
    Text(
        text = labelText,
        style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily(Font(R.font.inter)),
            fontWeight = FontWeight(400),
            color = Color(0xFF858585),
        )
    )
    BasicTextField(
        value = value,
        readOnly = readOnly,
        singleLine = singleLine,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.inter)),
            fontWeight = FontWeight(400),
            color = Color.White,
            letterSpacing = 0.15.sp,
        ),
        decorationBox = {innerTextField ->
            Column {
                Box {
                    if (value.isBlank()) {
                        Text(
                            hint, style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF4D4D4D),
                            )
                        )
                    }
                    innerTextField.invoke()
                }
                Divider(Modifier.padding(top = 16.dp))
            }
        }
    )
}