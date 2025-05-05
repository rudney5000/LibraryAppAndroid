package ru.bmstu.libraryapp.data.repositories

import android.util.Log
import ru.bmstu.libraryapp.data.network.GoogleBooksApiService
import ru.bmstu.libraryapp.domain.entities.GoogleBook
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.exceptions.LibraryException
import ru.bmstu.libraryapp.domain.repositories.GoogleBooksRepository

class GoogleBooksRepositoryImpl(
    private val googleBooksService: GoogleBooksApiService
) : GoogleBooksRepository {

    override suspend fun searchBooks(author: String?, title: String?): Result<List<LibraryItemType.Book>> {
        if (author.isNullOrBlank() && title.isNullOrBlank()) {
            Log.d("GoogleBooksRepo", "Recherche vide - auteur et titre vides")
            return Result.success(emptyList())
        }

        val query = buildQuery(author, title)
        Log.d("GoogleBooksRepo", "Requête construite: $query")

        return try {
            Log.d("GoogleBooksRepo", "Envoi de la requête à l'API...")
            val response = googleBooksService.searchBooks(query)

            if (response.isSuccessful) {
                val books = response.body()?.items?.map {
                    Log.d("GoogleBooksRepo", "Livre trouvé: ${it.volumeInfo.title}")
                    it.toLibraryItem()
                } ?: emptyList()
                Log.d("GoogleBooksRepo", "${books.size} livres trouvés")
                Result.success(books)
            } else {
                val errorMsg = "Erreur API: ${response.code()} ${response.message()}"
                Log.e("GoogleBooksRepo", errorMsg)
                Result.failure(LibraryException.NetworkError("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.message}"
            Log.e("GoogleBooksRepo", errorMsg, e)
            Result.failure(LibraryException.NetworkError(e.message ?: "Unknown error"))
        }
    }

    private fun buildQuery(author: String?, title: String?): String {
        val queryParts = mutableListOf<String>()

        if (!author.isNullOrBlank()) {
            queryParts.add("inauthor:${author.trim()}")
        }

        if (!title.isNullOrBlank()) {
            queryParts.add("intitle:${title.trim()}")
        }

        return queryParts.joinToString("+")
    }

    private fun GoogleBook.toLibraryItem(): LibraryItemType.Book {
        val isbn = volumeInfo.industryIdentifiers?.firstOrNull {
            it.type == "ISBN_13" || it.type == "ISBN_10"
        }?.identifier ?: throw IllegalStateException("ISBN is required")

        return LibraryItemType.Book(
            id = isbn.hashCode(),
            title = volumeInfo.title ?: "Без названия",
            isAvailable = true,
            pages = volumeInfo.pages ?: 0,
            author = volumeInfo.authors
        )
    }
}