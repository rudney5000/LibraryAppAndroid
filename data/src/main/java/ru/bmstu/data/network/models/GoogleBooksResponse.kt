package ru.bmstu.data.network.models

import ru.bmstu.domain.models.GoogleBook

data class GoogleBooksResponse(
    val totalItems: Int,
    val items: List<GoogleBook>?
)