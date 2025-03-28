package ru.bmstu.libraryapp.domain.entities
import java.util.Locale

/**
 * Перечисление месяцев на русском языке.
 * Используется для указания месяца выпуска газет.
 */
enum class Month {
    JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE,
    JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;

    fun getDisplayName(locale: Locale = Locale.getDefault()): String {
        return when(locale.language) {
            "en" -> {
                when(this) {
                    JANUARY -> "January"
                    FEBRUARY -> "February"
                    MARCH -> "March"
                    APRIL -> "April"
                    MAY -> "May"
                    JUNE -> "June"
                    JULY -> "July"
                    AUGUST -> "August"
                    SEPTEMBER -> "September"
                    OCTOBER -> "October"
                    NOVEMBER -> "November"
                    DECEMBER -> "December"
                }
            }
            else -> {
                when(this) {
                    JANUARY -> "Январь"
                    FEBRUARY -> "Февраль"
                    MARCH -> "Март"
                    APRIL -> "Апрель"
                    MAY -> "Май"
                    JUNE -> "Июнь"
                    JULY -> "Июль"
                    AUGUST -> "Август"
                    SEPTEMBER -> "Сентябрь"
                    OCTOBER -> "Октябрь"
                    NOVEMBER -> "Ноябрь"
                    DECEMBER -> "Декабрь"
                }
            }
        }
    }

}