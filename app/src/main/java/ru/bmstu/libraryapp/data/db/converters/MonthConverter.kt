package ru.bmstu.libraryapp.data.db.converters

import androidx.room.TypeConverter
import ru.bmstu.libraryapp.domain.entities.Month

class MonthConverter {
    @TypeConverter
    fun fromMonth(month: Month?): String? {
        return month?.name
    }

    @TypeConverter
    fun toMonth(value: String?): Month? {
        return value?.let { Month.valueOf(it) }
    }
}