package ru.bmstu.libraryapp.presentation.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.databinding.ItemLibraryBinding
import ru.bmstu.libraryapp.domain.entities.*

class LibraryItemViewHolder(
    private val binding: ItemLibraryBinding,
    private val onItemClick: (LibraryItem) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var currentItem: LibraryItem? = null

    init {
        itemView.setOnClickListener {
            currentItem?.let { item -> onItemClick(item) }
        }
    }

    fun bind(item: LibraryItem) {
        currentItem = item
        binding.apply {
            titleText.text = item.title
            idText.text = itemView.context.getString(R.string.item_id_format, item.id)

            iconView.setImageResource(when(item) {
                is Book -> R.drawable.ic_book_24
                is Newspaper -> R.drawable.ic_newspaper_24
                is Disk -> R.drawable.ic_disk_24
                is BaseLibraryItem -> R.drawable.ic_book_24
            })

            updateAvailability(item)
        }
    }

    fun updateAvailability(item: LibraryItem) {
        currentItem = item
        val alpha = if (item.isAvailable) 1f else 0.3f
        binding.apply {
            titleText.alpha = alpha
            idText.alpha = alpha
            cardView.elevation = itemView.resources.getDimension(
                if (item.isAvailable) R.dimen.card_elevation_normal else R.dimen.card_elevation_disabled
            )
        }
    }
}