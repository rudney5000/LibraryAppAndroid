package ru.bmstu.libraryapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.data.datasources.InMemoryDataSource
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.databinding.ActivityMainBinding
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.Month
import ru.bmstu.libraryapp.domain.entities.Newspaper
import ru.bmstu.libraryapp.domain.entities.ParcelableLibraryItem
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.views.ativities.LibraryItemDetailActivity
import ru.bmstu.libraryapp.presentation.viewmodels.MainViewModel
import ru.bmstu.libraryapp.presentation.views.adapters.LibraryItemAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LibraryItemAdapter
    private val repository: LibraryRepository = LibraryRepositoryImpl(InMemoryDataSource.getInstance())

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.libraryItems.observe(this) { items ->
            adapter.submitList(items)
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private val editItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { data ->
            val item = data.getParcelableExtra<ParcelableLibraryItem>(LibraryItemDetailActivity.EXTRA_ITEM)
            val isNewItem = data.getBooleanExtra(LibraryItemDetailActivity.EXTRA_IS_NEW_ITEM, false)

            viewModel.loadAllItems()

            if (isNewItem) {
                binding.recyclerView.smoothScrollToPosition(0)
                Toast.makeText(this, "new element added", Toast.LENGTH_SHORT).show()
            } else {
                item?.let {
                    val position = adapter.currentList.indexOfFirst { it.id == item.id }
                    if (position != -1) {
                        binding.recyclerView.smoothScrollToPosition(position)
                        Toast.makeText(this, "element updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = LibraryItemAdapter { item ->
            val intent = LibraryItemDetailActivity.createIntent(
                context = this,
                item = item,
                mode = LibraryItemDetailActivity.Companion.DetailMode.VIEW
            )
            editItemLauncher.launch(intent)
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
        val types = arrayOf(
            getString(R.string.item_type_book),
            getString(R.string.item_type_newspaper),
            getString(R.string.item_type_disk)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_choose_item_type))
            .setItems(types) { _, which ->
                val newItem = when (which) {
                    0 -> Book(0, "", true, 0, "")
                    1 -> Newspaper(0, "", true, 0, Month.JANUARY)
                    2 -> Disk(0, "", true, DiskType.CD)
                    else -> throw IllegalArgumentException("Unknown type")
                }
                val intent = LibraryItemDetailActivity.createIntent(
                    context = this,
                    item = newItem,
                    mode = LibraryItemDetailActivity.Companion.DetailMode.CREATE
                )
                editItemLauncher.launch(intent)
            }
            .show()
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
                    val item = adapter.currentList[position]
                    viewModel.deleteItem(item.id)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}