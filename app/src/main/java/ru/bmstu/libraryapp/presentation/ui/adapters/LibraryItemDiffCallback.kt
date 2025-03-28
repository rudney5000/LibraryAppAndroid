package ru.bmstu.libraryapp.presentation.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.bmstu.libraryapp.domain.entities.LibraryItem

class LibraryItemDiffCallback: DiffUtil.ItemCallback<LibraryItem>() {

    override fun areItemsTheSame(oldItem: LibraryItem, newItem: LibraryItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: LibraryItem, newItem: LibraryItem) =
        oldItem == newItem
}