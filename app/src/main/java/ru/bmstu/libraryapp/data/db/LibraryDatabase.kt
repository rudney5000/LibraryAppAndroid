package ru.bmstu.libraryapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.bmstu.libraryapp.data.db.converters.DateConverter
import ru.bmstu.libraryapp.data.db.converters.DiskTypeConverter
import ru.bmstu.libraryapp.data.db.converters.MonthConverter
import ru.bmstu.libraryapp.data.db.entities.LibraryItemEntity

@Database(entities = [LibraryItemEntity::class], version = 1)
@TypeConverters(DateConverter::class, MonthConverter::class, DiskTypeConverter::class)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun libraryItemDao(): LibraryItemDao
}