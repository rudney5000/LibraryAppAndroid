package ru.bmstu.libraryapp.presentation.utils

import ru.bmstu.libraryapp.domain.entities.DigitizableItem
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.ParcelableLibraryItemImpl

fun LibraryItem.toParcelable(): ParcelableLibraryItemImpl {
    val canDigitize = this is DigitizableItem
    val digitalContent = if (canDigitize) (this as DigitizableItem).digitize() else null

    return ParcelableLibraryItemImpl(
        id = id,
        title = title,
        isAvailable = isAvailable,
        briefInfo = getBriefInfo(),
        detailedInfo = getDetailedInfo(),
        canTakeHome = canBeTakenHome(),
        canReadInLibrary = canBeReadInLibrary(),
        typeName = getDisplayTypeName(),
        isDigitizable = canDigitize,
        digitalContent = digitalContent
    )
}