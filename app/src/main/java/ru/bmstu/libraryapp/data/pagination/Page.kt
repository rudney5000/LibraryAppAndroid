package ru.bmstu.libraryapp.data.pagination

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val isLast: Boolean
)