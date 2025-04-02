package ru.bmstu.libraryapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.data.datasources.InMemoryDataSource
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.databinding.ActivityMainBinding
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Newspaper
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.ui.adapters.LibraryItemAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LibraryItemAdapter
    private val repository: LibraryRepository = LibraryRepositoryImpl(InMemoryDataSource())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeToDelete()
        loadLibraryItems()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = LibraryItemAdapter { item ->
            Toast.makeText(this, getString(R.string.item_clicked_format, item.id), Toast.LENGTH_SHORT).show()
            val updatedList = adapter.currentList.toMutableList()
            val position = updatedList.indexOfFirst { it.id == item.id }
            if (position != -1) {
                val updatedItem = when (item) {
                    is Book -> item.copy(isAvailable = !item.isAvailable)
                    is Newspaper -> item.copy(isAvailable = !item.isAvailable)
                    is Disk -> item.copy(isAvailable = !item.isAvailable)
                    else -> item
                }

                repository.updateItemAvailability(updatedItem, updatedItem.isAvailable)

                updatedList[position] = updatedItem
                adapter.submitList(updatedList)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentList = adapter.currentList.toMutableList()
                    currentList.removeAt(position)
                    adapter.submitList(currentList)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun loadLibraryItems() {
        val allItems = mutableListOf<BaseLibraryItem>().apply {
            addAll(repository.getAllBooks())
            addAll(repository.getAllNewspapers())
            addAll(repository.getAllDisks())
        }
        adapter.submitList(allItems as List<LibraryItem>?)
    }
}