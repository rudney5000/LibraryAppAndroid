package ru.bmstu.libraryapp.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.bmstu.libraryapp.data.network.models.GoogleBooksResponse

interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("fields") fields: String = "items(volumeInfo/title,volumeInfo/authors,volumeInfo/pageCount,volumeInfo/industryIdentifiers)"
    ): Response<GoogleBooksResponse>
}