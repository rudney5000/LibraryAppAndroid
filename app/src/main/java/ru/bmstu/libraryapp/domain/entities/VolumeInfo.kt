package ru.bmstu.libraryapp.domain.entities

data class VolumeInfo(
    val title: String?,
    val isAvailable: Boolean,
    val pages: Int?,
    val authors: String,
    val industryIdentifiers: List<IndustryIdentifier>?
) {
    fun getIsbn(): String? {
        return industryIdentifiers?.firstOrNull {
            it.type == "ISBN_13" || it.type == "ISBN_10"
        }?.identifier
    }
}