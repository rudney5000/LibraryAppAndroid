package ru.bmstu.libraryapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Month
import ru.bmstu.libraryapp.domain.entities.Newspaper
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.ui.adapters.LibraryItemAdapter
import ru.bmstu.libraryapp.presentation.ui.adapters.LibraryItemDetailActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LibraryItemAdapter
    private val items = mutableListOf<LibraryItem>()
    private val repository: LibraryRepository = LibraryRepositoryImpl(InMemoryDataSource())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        loadLibraryItems()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = LibraryItemAdapter() { item ->
            val intent = Intent(this, LibraryItemDetailActivity::class.java).apply {
                putExtra("item_id", item.id)
                putExtra("item_title", item.title)
                putExtra("item_is_available", item.isAvailable)
                putExtra("item_type", when(item) {
                    is Book -> "book"
                    is Newspaper -> "newspaper"
                    is Disk -> "disk"
                    else -> ""
                })
                when(item) {
                    is Book -> {
                        putExtra("item_author", item.author)
                        putExtra("item_pages", item.pages)
                    }
                    is Newspaper -> {
                        putExtra("item_issue_number", item.issueNumber)
                        putExtra("item_month", item.month.ordinal)
                    }
                    is Disk -> {
                        putExtra("item_disk_type", item.type.name)
                    }
                    else -> {}
                }
            }
            startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFab() {
        binding.addFab.setOnClickListener {
            showItemTypeDialog()
        }
    }

    private fun showItemTypeDialog() {
        val types = arrayOf("Book", "Newspaper", "Disk")
        AlertDialog.Builder(this)
            .setTitle("Choose item type")
            .setItems(types) { _, which ->
                val itemType = when (which) {
                    0 -> "book"
                    1 -> "newspaper"
                    2 -> "disk"
                    else -> "book"
                }
                val intent = Intent(this, LibraryItemDetailActivity::class.java).apply {
                    putExtra("item_type", itemType)
                }
                startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
            }
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val id = data.getIntExtra("item_id", -1)
            val title = data.getStringExtra("item_title") ?: return
            val isAvailable = data.getBooleanExtra("item_is_available", false)
            val type = data.getStringExtra("item_type") ?: return

            val newItem = when (type) {
                "book" -> Book(
                    id = id,
                    title = title,
                    isAvailable = isAvailable,
                    author = data.getStringExtra("item_author") ?: "",
                    pages = data.getIntExtra("item_pages", 0)
                )
                "newspaper" -> Newspaper(
                    id = id,
                    title = title,
                    isAvailable = isAvailable,
                    issueNumber = data.getIntExtra("item_issue_number", 0),
                    month = Month.entries[data.getIntExtra("item_month", 0)]
                )
                "disk" -> Disk(
                    id = id,
                    title = title,
                    isAvailable = isAvailable,
                    type = DiskType.valueOf(data.getStringExtra("item_disk_type") ?: DiskType.entries[0].name)
                )
                else -> null
            }

            when (requestCode) {
                ADD_ITEM_REQUEST_CODE -> {
                    newItem?.let {
                        items.add(it)
                        adapter.notifyItemInserted(items.size - 1)
                    }
                }
                EDIT_ITEM_REQUEST_CODE -> {
                    newItem?.let { updated ->
                        val index = items.indexOfFirst { it.id == updated.id }
                        if (index != -1) {
                            items[index] = updated
                            adapter.notifyItemChanged(index)
                        }
                    }
                }
            }
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

    companion object {
        private const val ADD_ITEM_REQUEST_CODE = 1
        private const val EDIT_ITEM_REQUEST_CODE = 2
    }
}