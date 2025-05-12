package ru.bmstu.libraryapp.domain.entities

data class VolumeInfo(
    val title: String?,
    val isAvailable: Boolean,
    val pages: Int?,
    val authors: List<String>?,
    val industryIdentifiers: List<IndustryIdentifier>?
) {
}