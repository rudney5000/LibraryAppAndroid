package ru.bmstu.libraryapp.data.db.converters

import androidx.room.TypeConverter
import ru.bmstu.libraryapp.domain.entities.DiskType

class DiskTypeConverter {
    @TypeConverter
    fun fromDiskType(diskType: DiskType?): String? {
        return diskType?.name
    }

    @TypeConverter
    fun toDiskType(value: String?): DiskType? {
        return value?.let { DiskType.valueOf(it) }
    }
}