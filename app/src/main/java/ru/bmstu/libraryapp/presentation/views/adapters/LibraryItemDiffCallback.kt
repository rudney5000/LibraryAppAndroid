package ru.bmstu.libraryapp.presentation.views.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import ru.bmstu.domain.models.LibraryItemType

class LibraryItemDiffCallback : DiffUtil.ItemCallback<LibraryItemType>() {

    override fun areItemsTheSame(oldItem: LibraryItemType, newItem: LibraryItemType): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: LibraryItemType, newItem: LibraryItemType): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: LibraryItemType, newItem: LibraryItemType): Any? {
        return when {
            oldItem.isAvailable != newItem.isAvailable ->
                LibraryItemPayload.AvailabilityChanged(newItem.isAvailable)
            else -> null
        }
    }
}