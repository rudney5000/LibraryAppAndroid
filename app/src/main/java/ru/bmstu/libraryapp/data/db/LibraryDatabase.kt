package ru.bmstu.libraryapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getInstance(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}