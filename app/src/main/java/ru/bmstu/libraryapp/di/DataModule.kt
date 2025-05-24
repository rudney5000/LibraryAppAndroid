package ru.bmstu.libraryapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.bmstu.data.datasources.InMemoryDataSource
import ru.bmstu.data.datasources.LocalDataSource
import ru.bmstu.data.datasources.RoomDataSource
import ru.bmstu.data.db.LibraryDatabase
import ru.bmstu.data.network.NetworkModule
import ru.bmstu.data.repositories.impl.GoogleBooksRepositoryImpl
import ru.bmstu.data.repositories.impl.LibraryRepositoryImpl
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.repositories.LibraryRepository
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideLibraryDatabase(context: Context): LibraryDatabase {
        return LibraryDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRoomDataSource(database: LibraryDatabase): RoomDataSource {
        return RoomDataSource(database)
    }

    @Provides
    @Singleton
    fun provideLocalDataSource(roomDataSource: RoomDataSource): LocalDataSource {
        return roomDataSource
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(dataSource: LocalDataSource): LibraryRepository {
        return LibraryRepositoryImpl(dataSource)
    }

    @Provides
    @Singleton
    fun provideGoogleBooksRepository(context: Context): GoogleBooksRepository {
        return GoogleBooksRepositoryImpl(NetworkModule.googleBooksService, context)
    }
}