package com.face.businessface.database

import androidx.room.TypeConverter
import com.face.businessface.database.entity.LinkEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class Converters {

    @TypeConverter
    fun listToJsonString(value: List<String>?): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonStringToList(value: String) = Json.decodeFromString<List<String>>(value)

    @TypeConverter
    fun listToJsonString1(value: List<LinkEntity>?): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonStringToList1(value: String) = Json.decodeFromString<List<LinkEntity>>(value)

    @TypeConverter
    fun listToJsonString2(value: List<FloatArray>?): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonStringToList2(value: String) = Json.decodeFromString<List<FloatArray>>(value)


}