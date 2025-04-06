package ru.bmstu.libraryapp.presentation.utils
/**
 * Функция-расширение для фильтрации списка по указанному типу.
 * Использует reified для доступа к информации о типе во время выполнения.
 * @return Список элементов указанного типа
 */
inline fun <reified T> List<*>.filterByType(): List<T> {
    return this.filter { it is T }
        .map { it as T }
}