package ru.bmstu.libraryapp.domain.entities

/**
 * Класс, представляющий книгу в библиотеке.
 * Наследуется от BaseLibraryItem и добавляет специфичные для книги свойства.
 */
data class Book(
    override val id: Int,
    override val title: String,
    override var isAvailable: Boolean,
    val pages: Int,
    val author: String
) : BaseLibraryItem(id, title, isAvailable) {

    /**
     * Получение подробной информации о книге.
     * @return Строка в формате "книга: наименование (количествоСтраниц стр.) автора: автор с id: id доступна: Да/Нет"
     */
    override fun getDetailedInfo(): String {
        return "книга: $title ($pages стр.) автора: $author с id: $id доступна: ${if (isAvailable) "Да" else "Нет"}"
    }

    /**
     * Книги можно брать домой.
     * @return true, так как книги можно брать домой
     */
    override fun canBeTakenHome(): Boolean = true

    /**
     * Книги можно читать в библиотеке.
     * @return true, так как книги можно читать в библиотеке
     */
    override fun canBeReadInLibrary(): Boolean = true

    /**
     * Возвращает отображаемое название типа.
     * @return "Книга"
     */
    override fun getDisplayTypeName(): String = "книга"
}