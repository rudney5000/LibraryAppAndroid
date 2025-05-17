package ru.bmstu.data.pagination

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val isLast: Boolean,
    val isFirst: Boolean
)