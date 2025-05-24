package ru.bmstu.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.bmstu.common.types.DigitizableItem
import ru.bmstu.common.types.DiskType
import ru.bmstu.common.types.Month
import java.util.Locale

@Parcelize
sealed class LibraryItemType(
    override val id: Int,
    override val title: String,
    override var isAvailable: Boolean
) : BaseLibraryItem(id, title, isAvailable), Parcelable {
    @Parcelize
    data class Book(
        override val id: Int,
        override val title: String,
        override var isAvailable: Boolean,
        val pages: Int,
        val author: String
    ) : LibraryItemType(id, title, isAvailable), DigitizableItem {
        override fun getDetailedInfo(): String =
            "книга: $title ($pages стр.) автора: $author с id: $id доступна: ${if (isAvailable) "Да" else "Нет"}"
        override fun canBeTakenHome(): Boolean = true
        override fun canBeReadInLibrary(): Boolean = true
        override fun getDisplayTypeName(): String = "книга"
        override fun digitize(): String =
            "Оцифрованная книга: $title (автор: $author, $pages страниц)"
    }

    @Parcelize
    data class Disk(
        override val id: Int,
        override val title: String,
        override var isAvailable: Boolean,
        val type: DiskType
    ) : LibraryItemType(id, title, isAvailable), DigitizableItem {
        override fun getDetailedInfo(): String =
            "$type $title доступен: ${if (isAvailable) "Да" else "Нет"}"
        override fun canBeTakenHome(): Boolean = true
        override fun canBeReadInLibrary(): Boolean = false
        override fun getDisplayTypeName(): String = "Диск"
        override fun digitize(): String = "Оцифрованный $type: $title"
    }

    @Parcelize
    data class Newspaper(
        override val id: Int,
        override val title: String,
        override var isAvailable: Boolean,
        val issueNumber: Int,
        val month: Month
    ) : LibraryItemType(id, title, isAvailable) {
        override fun getDetailedInfo(): String =
            "выпуск: $issueNumber за ${month.getDisplayName(Locale("ru"))} газеты $title с id: $id доступен: ${if (isAvailable) "Да" else "Нет"}"
        override fun canBeTakenHome(): Boolean = false
        override fun canBeReadInLibrary(): Boolean = true
        override fun getDisplayTypeName(): String = "Газета"
    }
}