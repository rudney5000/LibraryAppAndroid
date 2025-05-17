package ru.bmstu.data.db.converters

import androidx.room.TypeConverter
import ru.bmstu.common.types.DiskType

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