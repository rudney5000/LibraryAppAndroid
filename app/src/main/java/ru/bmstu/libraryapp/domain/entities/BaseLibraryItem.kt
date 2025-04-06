package ru.bmstu.libraryapp.domain.entities

/**
 * Абстрактный базовый класс для всех элементов библиотеки.
 * Реализует основные функции интерфейса ru.bmstu.libraryapp.domain.entities.LibraryItem.
 */

 abstract class BaseLibraryItem(
    override val id: Int,
    override val title: String,
    override var isAvailable: Boolean
) : LibraryItem {

    /**
     * Получение краткой информации об элементе библиотеки.
     * @return Строка в формате "наименование доступна: Да/Нет"
     */
    override fun getBriefInfo(): String {
        return "$title доступна: ${if (isAvailable) "Да" else "Нет"}"
    }

    /**
     * Абстрактный метод для получения подробной информации.
     * Должен быть реализован в дочерних классах.
     */
    abstract override fun getDetailedInfo(): String

    /**
     * Изменение доступности элемента.
     * @param newAvailability Новое состояние доступности
     */
    fun changeAvailability(newAvailability: Boolean) {
        isAvailable = newAvailability
    }
    /**
     * Абстрактный метод, возвращающий отображаемое название типа элемента.
     * Должен быть реализован в дочерних классах.
     * @return Строка с названием типа элемента
     */
    abstract override fun getDisplayTypeName(): String

    override fun digitize(): String {
        throw UnsupportedOperationException("Этот тип элемента нельзя оцифровать")
    }
}