package ru.bmstu.libraryapp.presentation.views.adapters

import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.databinding.ItemLibraryBinding
import ru.bmstu.libraryapp.domain.entities.LibraryItemType

class LibraryItemViewHolder(
    private val binding: ItemLibraryBinding,
    private val onItemClick: (LibraryItemType) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LibraryItemType) {
        binding.titleText.text = item.title
        binding.idText.text = "ID: ${item.id}"
        updateAvailability(item)
        binding.root.setOnClickListener { onItemClick(item) }
    }

    fun updateAvailability(item: LibraryItemType) {
        binding.root.alpha = if (item.isAvailable) 1.0f else 0.5f
    }
}