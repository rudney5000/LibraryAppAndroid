package ru.bmstu.libraryapp.presentation.views.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import ru.bmstu.libraryapp.domain.entities.ParcelableLibraryItem

class LibraryItemDiffCallback: DiffUtil.ItemCallback<ParcelableLibraryItem>() {

    override fun areItemsTheSame(oldItem: ParcelableLibraryItem, newItem: ParcelableLibraryItem): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ParcelableLibraryItem, newItem: ParcelableLibraryItem) =
        oldItem == newItem

    override fun getChangePayload(oldItem: ParcelableLibraryItem, newItem: ParcelableLibraryItem): Any? {
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