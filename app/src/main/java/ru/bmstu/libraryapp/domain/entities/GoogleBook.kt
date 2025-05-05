package ru.bmstu.libraryapp.domain.entities

import kotlin.random.Random

data class GoogleBook(
    val volumeInfo: VolumeInfo
) {
    fun toLocalBook(): LibraryItemType.Book {
        return LibraryItemType.Book(
            id = volumeInfo.getIsbn()?.hashCode() ?: Random.nextInt(),
            title = volumeInfo.title ?: "Без названия",
            isAvailable = true,
            pages = volumeInfo.pages ?: 0,
            author = volumeInfo.authors
        )
    }
}