package ru.bmstu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.bmstu.data.db.converters.DateConverter
import ru.bmstu.data.db.converters.DiskTypeConverter
import ru.bmstu.data.db.converters.MonthConverter
import ru.bmstu.data.db.entities.LibraryItemEntity

@Database(entities = [LibraryItemEntity::class], version = 1)
@TypeConverters(DateConverter::class, MonthConverter::class, DiskTypeConverter::class)
abstract class LibraryDatabase : RoomDatabase() {
//    abstract val DiskTypeConverter: Any

    abstract fun libraryItemDao(): LibraryItemDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getInstance(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): LibraryDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LibraryDatabase::class.java,
                "library_database"
            ).build()
        }
    }
}