package ru.bmstu.libraryapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.bmstu.data.repositories.impl.GoogleBooksRepositoryImpl
import ru.bmstu.data.network.NetworkModule
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.repositories.LibraryRepository
import ru.bmstu.domain.usecases.*
import ru.bmstu.libraryapp.presentation.viewmodels.ViewModelFactory
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun provideViewModelFactory(
        getAllBooksUseCase: GetAllBooksUseCase,
        getAllNewspapersUseCase: GetAllNewspapersUseCase,
        getAllDisksUseCase: GetAllDisksUseCase,
        deleteItemUseCase: DeleteItemUseCase,
        addBookUseCase: AddBookUseCase,
        addNewspaperUseCase: AddNewspaperUseCase,
        addDiskUseCase: AddDiskUseCase,
        updateBookUseCase: UpdateBookUseCase,
        updateNewspaperUseCase: UpdateNewspaperUseCase,
        updateDiskUseCase: UpdateDiskUseCase,
        searchBooksUseCase: SearchBooksUseCase,
        googleBooksRepository: GoogleBooksRepository,
        context: Context
    ): ViewModelFactory {
        return ViewModelFactory(
            getAllBooksUseCase,
            getAllNewspapersUseCase,
            getAllDisksUseCase,
            deleteItemUseCase,
            addBookUseCase,
            addNewspaperUseCase,
            addDiskUseCase,
            updateBookUseCase,
            updateNewspaperUseCase,
            updateDiskUseCase,
            searchBooksUseCase,
            googleBooksRepository,
            context
        )
    }
}