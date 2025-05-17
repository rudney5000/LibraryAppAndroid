package ru.bmstu.libraryapp.di

import dagger.Module
import dagger.Provides
import ru.bmstu.data.datasources.InMemoryDataSource
import ru.bmstu.data.repositories.impl.LibraryRepositoryImpl
import ru.bmstu.domain.repositories.LibraryRepository
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideInMemoryDataSource(): InMemoryDataSource {
        return InMemoryDataSource.getInstance()
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(dataSource: InMemoryDataSource): LibraryRepository {
        return LibraryRepositoryImpl(dataSource)
    }
}