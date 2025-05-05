package ru.bmstu.libraryapp.data.network.models

import ru.bmstu.libraryapp.domain.entities.GoogleBook

data class GoogleBooksResponse(
    val items: List<GoogleBook>?
)