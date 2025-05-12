package ru.bmstu.libraryapp.presentation.utils

import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Функция-расширение для фильтрации списка по указанному типу.
 * Использует reified для доступа к информации о типе во время выполнения.
 * @return Список элементов указанного типа
 */
inline fun <reified T> List<*>.filterByType(): List<T> {
    return this.filter { it is T }
        .map { it as T }
}

/**
 * Функция-расширение для фильтрации списка по указанному классу.
 * @param kClass Класс для фильтрации
 * @return Список элементов указанного типа
 */
fun <T : Any> List<*>.filterByKClass(kClass: KClass<T>): List<T> {
    return this.filter { kClass.isInstance(it) }
        .map { kClass.cast(it) }
}