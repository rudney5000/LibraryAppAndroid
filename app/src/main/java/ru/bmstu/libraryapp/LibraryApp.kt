package ru.bmstu.libraryapp

import android.app.Application
import ru.bmstu.libraryapp.data.datasources.InMemoryDataSource
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository


class LibraryApp : Application() {
    val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(InMemoryDataSource.getInstance())
    }
}