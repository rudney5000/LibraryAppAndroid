package ru.bmstu.domain.models

import com.google.gson.annotations.SerializedName

data class VolumeInfo(
    val title: String?,
    val isAvailable: Boolean,
    @SerializedName("pageCount") val pages: Int?,
    val authors: List<String>?,
    val industryIdentifiers: List<IndustryIdentifier>?
) {
}