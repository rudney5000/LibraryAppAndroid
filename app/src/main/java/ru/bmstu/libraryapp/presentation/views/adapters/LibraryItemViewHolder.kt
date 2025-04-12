package ru.bmstu.libraryapp.presentation.views.adapters

import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.databinding.ItemLibraryBinding
import ru.bmstu.libraryapp.domain.entities.ParcelableLibraryItem

class LibraryItemViewHolder(
    private val binding: ItemLibraryBinding,
    private val onItemClick: (ParcelableLibraryItem) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ParcelableLibraryItem) {
        binding.titleText.text = item.title
        binding.idText.text = "ID: ${item.id}"
        updateAvailability(item)
        binding.root.setOnClickListener { onItemClick(item) }
    }

    fun updateAvailability(item: ParcelableLibraryItem) {
        binding.root.alpha = if (item.isAvailable) 1.0f else 0.5f
    }
}