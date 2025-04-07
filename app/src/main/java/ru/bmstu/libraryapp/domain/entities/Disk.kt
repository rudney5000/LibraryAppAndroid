package ru.bmstu.libraryapp.domain.entities

import kotlinx.parcelize.Parcelize

/**
 * Класс, представляющий диск в библиотеке.
 * Наследуется от BaseLibraryItem и добавляет тип диска.
 */
@Parcelize
data class Disk(
    override val id: Int,
    override val title: String,
    override var isAvailable: Boolean,
    val type: DiskType
) : BaseLibraryItem(id, title, isAvailable), ParcelableLibraryItem {

    /**
     * Получение подробной информации о диске.
     * @return Строка в формате "типДиска наименование доступен: Да/Нет"
     */
    override fun getDetailedInfo(): String {
        return "$type $title доступен: ${if (isAvailable) "Да" else "Нет"}"
    }

    /**
     * Диски можно брать домой.
     * @return true, так как диски можно брать домой
     */
    override fun canBeTakenHome(): Boolean = true

    /**
     * Диски нельзя использовать в библиотеке.
     * @return false, так как диски не предназначены для использования в библиотеке
     */
    override fun canBeReadInLibrary(): Boolean = false

    /**
     * Возвращает отображаемое название типа.
     * @return "Диск"
     */
    override fun getDisplayTypeName(): String = "Диск"
}