package ru.bmstu.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.bmstu.data.network.models.GoogleBooksResponse

private const val KEY = ""

interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String? = KEY,
        @Query("startIndex") startIndex: Int = 0,
        @Query("maxResults") maxResults: Int = 20,
        @Query("fields") fields: String = "items(volumeInfo(title,authors,pageCount,industryIdentifiers))"
    ): Response<GoogleBooksResponse>
}