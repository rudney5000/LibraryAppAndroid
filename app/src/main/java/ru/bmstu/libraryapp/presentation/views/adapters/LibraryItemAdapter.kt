package ru.bmstu.libraryapp.presentation.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.bmstu.libraryapp.databinding.ItemLibraryBinding
import ru.bmstu.libraryapp.domain.entities.ParcelableLibraryItem

class LibraryItemAdapter(
    private val onItemClick: (ParcelableLibraryItem) -> Unit
) : ListAdapter<ParcelableLibraryItem, LibraryItemViewHolder>(LibraryItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryItemViewHolder {
        val binding = ItemLibraryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LibraryItemViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: LibraryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: LibraryItemViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        val payload = payloads[0] as? LibraryItemPayload
        when (payload) {
            is LibraryItemPayload.AvailabilityChanged -> {
                holder.updateAvailability(getItem(position))
            }
            null -> super.onBindViewHolder(holder, position, payloads)
        }
    }
}