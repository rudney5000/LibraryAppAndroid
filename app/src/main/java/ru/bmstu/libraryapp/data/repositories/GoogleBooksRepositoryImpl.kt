package ru.bmstu.libraryapp.data.repositories

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import ru.bmstu.libraryapp.data.network.GoogleBooksApiService
import ru.bmstu.libraryapp.domain.entities.GoogleBook
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.exceptions.LibraryException
import ru.bmstu.libraryapp.domain.repositories.GoogleBooksRepository
import ru.bmstu.libraryapp.presentation.utils.hasInternetConnection

class GoogleBooksRepositoryImpl(
    private val googleBooksService: GoogleBooksApiService,
    private val context: Context
) : GoogleBooksRepository {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun searchBooks(author: String?, title: String?): Result<List<LibraryItemType.Book>> {
//        Log.d("GoogleBooksRepo", "searchBooks: author=$author, title=$title")
        Log.d("GoogleBooksRepo", "searchBooks вызван: author=$author, title=$title")

        if (author.isNullOrBlank() && title.isNullOrBlank()) {
            Log.w("GoogleBooksRepo", "Поля пустые")
            return Result.success(emptyList())
        }

        if (!hasInternetConnection(context)) {
            Log.e("GoogleBooksRepo", "Нет интернета")
            return Result.failure(LibraryException.NetworkError("Нет подключения к интернету"))
        }

        val query = buildQuery(author, title)
        Log.d("GoogleBooksRepo", "Формируем запрос: $query")

        return try {
            val response = googleBooksService.searchBooks(query)

            if (response.isSuccessful) {
                val body = response.body()
                val books = body?.items?.mapNotNull {
                    Log.d("GoogleBooksRepo", "Конвертируем книгу: ${it.volumeInfo.title}")
                    it.toLibraryItem()
                } ?: emptyList()

                Log.d("GoogleBooksRepo", "Найдено: ${books.size} книг")
                Result.success(books)
            } else {
                val errorMsg = "Ошибка API: ${response.code()} ${response.message()}"
                Log.e("GoogleBooksRepo", errorMsg)
                Result.failure(LibraryException.NetworkError(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GoogleBooksRepo", "Исключение при поиске", e)
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
        Log.d("GoogleBooksRepo", "Сформирован запрос: $finalQuery")
        return finalQuery
    }

    private fun GoogleBook.toLibraryItem(): LibraryItemType.Book? {
        val volumeInfo = this.volumeInfo
        val identifier = volumeInfo.industryIdentifiers?.firstOrNull {
            it.type in listOf("ISBN_10", "ISBN_13")
        }?.identifier

        if (identifier.isNullOrBlank()) {
            Log.w("GoogleBooksRepo", "Нет ISBN для книги: ${volumeInfo.title}")
            return null
        }

        val authors = volumeInfo.authors?.joinToString(", ") ?: "Неизвестный автор"

        return LibraryItemType.Book(
            id = identifier.hashCode(),
            title = volumeInfo.title ?: "Без названия",
            isAvailable = true,
            pages = volumeInfo.pages ?: 0,
            author = authors
        )
    }
}