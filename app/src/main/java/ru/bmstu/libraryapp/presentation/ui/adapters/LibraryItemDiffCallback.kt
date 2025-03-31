package ru.bmstu.libraryapp.presentation.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.bmstu.libraryapp.domain.entities.LibraryItem

class LibraryItemDiffCallback: DiffUtil.ItemCallback<LibraryItem>() {

    override fun areItemsTheSame(oldItem: LibraryItem, newItem: LibraryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LibraryItem, newItem: LibraryItem) =
        oldItem == newItem

    override fun getChangePayload(oldItem: LibraryItem, newItem: LibraryItem): Any? {
        return when {
            oldItem.isAvailable != newItem.isAvailable ->
                LibraryItemPayload.AvailabilityChanged(newItem.isAvailable)
            else -> null
        }
    }
}

sealed class LibraryItemPayload {
    data class AvailabilityChanged(val newAvailability: Boolean) : LibraryItemPayload()
}