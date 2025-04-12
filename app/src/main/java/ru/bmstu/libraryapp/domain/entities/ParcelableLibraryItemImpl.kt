package ru.bmstu.libraryapp.domain.entities

import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableLibraryItemImpl(
    override val id: Int,
    override val title: String,
    override var isAvailable: Boolean,
    private val briefInfo: String,
    private val detailedInfo: String,
    private val canTakeHome: Boolean,
    private val canReadInLibrary: Boolean,
    private val typeName: String,
    override val isDigitizable: Boolean,
    private val digitalContent: String?,
) : ParcelableLibraryItem {

    override fun getBriefInfo(): String = briefInfo
    override fun getDetailedInfo(): String = detailedInfo
    override fun canBeTakenHome(): Boolean = canTakeHome
    override fun canBeReadInLibrary(): Boolean = canReadInLibrary
    override fun getDisplayTypeName(): String = typeName

    override fun digitize(): String {
        return digitalContent ?: throw UnsupportedOperationException("Этот тип элемента нельзя оцифровать")
    }
}