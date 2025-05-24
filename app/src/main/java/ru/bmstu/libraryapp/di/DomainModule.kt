package ru.bmstu.libraryapp.di

import dagger.Module
import dagger.Provides
import ru.bmstu.domain.repositories.LibraryRepository
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.usecases.*
import ru.bmstu.data.repositories.impl.GoogleBooksRepositoryImpl
import ru.bmstu.data.network.NetworkModule
import android.content.Context

@Module
class DomainModule {

    @Provides
    fun provideGetAllBooksUseCase(repository: LibraryRepository): GetAllBooksUseCase {
        return GetAllBooksUseCase(repository)
    }

    @Provides
    fun provideGetAllNewspapersUseCase(repository: LibraryRepository): GetAllNewspapersUseCase {
        return GetAllNewspapersUseCase(repository)
    }

    @Provides
    fun provideGetAllDisksUseCase(repository: LibraryRepository): GetAllDisksUseCase {
        return GetAllDisksUseCase(repository)
    }

    @Provides
    fun provideDeleteItemUseCase(repository: LibraryRepository): DeleteItemUseCase {
        return DeleteItemUseCase(repository)
    }

    @Provides
    fun provideAddBookUseCase(repository: LibraryRepository): AddBookUseCase {
        return AddBookUseCase(repository)
    }

    @Provides
    fun provideAddNewspaperUseCase(repository: LibraryRepository): AddNewspaperUseCase {
        return AddNewspaperUseCase(repository)
    }

    @Provides
    fun provideAddDiskUseCase(repository: LibraryRepository): AddDiskUseCase {
        return AddDiskUseCase(repository)
    }

    @Provides
    fun provideUpdateBookUseCase(repository: LibraryRepository): UpdateBookUseCase {
        return UpdateBookUseCase(repository)
    }

    @Provides
    fun provideUpdateNewspaperUseCase(repository: LibraryRepository): UpdateNewspaperUseCase {
        return UpdateNewspaperUseCase(repository)
    }

    @Provides
    fun provideUpdateDiskUseCase(repository: LibraryRepository): UpdateDiskUseCase {
        return UpdateDiskUseCase(repository)
    }

    @Provides
    fun provideSearchBooksUseCase(
        libraryRepository: LibraryRepository,
        googleBooksRepository: GoogleBooksRepository
    ): SearchBooksUseCase {
        return SearchBooksUseCase(libraryRepository, googleBooksRepository)
    }
}