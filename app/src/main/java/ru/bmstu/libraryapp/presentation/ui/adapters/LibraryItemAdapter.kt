package ru.bmstu.libraryapp.presentation.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.bmstu.libraryapp.databinding.ItemLibraryBinding
import ru.bmstu.libraryapp.domain.entities.LibraryItem

class LibraryItemAdapter(
    private val onItemClick: (LibraryItem) -> Unit
) : ListAdapter<LibraryItem, LibraryItemViewHolder>(LibraryItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryItemViewHolder {
        val binding = ItemLibraryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LibraryItemViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: LibraryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: LibraryItemViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        val bundle = payloads[0] as Bundle
        if (bundle.getBoolean("availability_changed", false)) {
            holder.updateAvailability(getItem(position).isAvailable)
        }
    }
}