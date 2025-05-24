package ru.bmstu.data.repositories.impl

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.bmstu.common.utils.hasInternetConnection
import ru.bmstu.data.network.GoogleBooksApiService
import ru.bmstu.domain.exceptions.LibraryException
import ru.bmstu.domain.models.GoogleBook
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.GoogleBooksRepository

class GoogleBooksRepositoryImpl(
    private val googleBooksService: GoogleBooksApiService,
    private val context: Context
) : GoogleBooksRepository {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun searchBooks(author: String?, title: String?): Result<List<LibraryItemType.Book>> = withContext(
        Dispatchers.IO) {
        if (author.isNullOrBlank() && title.isNullOrBlank()) {
            return@withContext Result.success(emptyList())
        }

        if (!hasInternetConnection(context)) {
            return@withContext Result.failure(LibraryException.NetworkError("Нет подключения к интернету"))
        }

        val query = buildQuery(author, title)
        return@withContext try {
            val response = googleBooksService.searchBooks(query)

            if (response.isSuccessful) {
                val body = response.body()
                val books = body?.items?.mapNotNull {
                    it.toLibraryItem()
                } ?: emptyList()

                Result.success(books)
            } else {
                val errorMsg = "Ошибка API: ${response.code()} ${response.message()}"
                Result.failure(LibraryException.NetworkError(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(LibraryException.NetworkError(e.message ?: "Сеть недоступна"))
        }
    }

    private fun buildQuery(author: String?, title: String?): String {
        val parts = mutableListOf<String>()

        if (!author.isNullOrBlank()) {
            parts.add("inauthor:${author.trim()}")
        }

        if (!title.isNullOrBlank()) {
            parts.add("intitle:${title.trim()}")
        }

        val finalQuery = parts.joinToString("+")
        return finalQuery
    }

    private fun GoogleBook.toLibraryItem(): LibraryItemType.Book? {
        val volumeInfo = this.volumeInfo
        val identifier = volumeInfo.industryIdentifiers?.firstOrNull {
            it.type in listOf("ISBN_10", "ISBN_13")
        }?.identifier


        val finalIdentifier = if (identifier.isNullOrBlank()) {
            val backupId = volumeInfo.title?.hashCode()?.toString() ?: return null
            backupId
        } else {
            identifier
        }

        val authors = volumeInfo.authors?.joinToString(", ") ?: "Неизвестный автор"

        return LibraryItemType.Book(
            id = finalIdentifier.hashCode(),
            title = volumeInfo.title ?: "Без названия",
            isAvailable = true,
            pages = volumeInfo.pages ?: 0,
            author = authors
        )
    }
}