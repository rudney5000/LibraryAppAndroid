package ru.bmstu.data.filters

enum class SortBy {
    TITLE,
    DATE,
    AUTHOR
}

data class LibraryFilter(
    val query: String = "",
    val sortBy: SortBy = SortBy.TITLE,
    val onlyAvailable: Boolean = false
)