package ru.bmstu.libraryapp.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Newspaper
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem

class LibraryItemAdapter (
    private val onItemClick: (LibraryItem) -> Unit
): ListAdapter<LibraryItem, LibraryItemAdapter.ViewHolder>(LibraryItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_library, parent, false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val icon: ImageView = itemView.findViewById(R.id.iconView)
        private val title: TextView = itemView.findViewById(R.id.titleText)
        private val id: TextView = itemView.findViewById(R.id.idText)

        fun bind(item: LibraryItem) {
            title.text = item.title
            id.text = "ID: ${item.id}"

            icon.setImageResource(when(item) {
                is Book -> R.drawable.baseline_book_24
                is Newspaper -> R.drawable.baseline_newspaper_24
                is Disk -> R.drawable.baseline_disk_24
                is BaseLibraryItem ->  R.drawable.baseline_book_24
            })

            val alpha = if (item.isAvailable) 1f else 0.3f
            title.alpha = alpha
            id.alpha = alpha
            cardView.elevation = if (item.isAvailable) 10f else 1f

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}