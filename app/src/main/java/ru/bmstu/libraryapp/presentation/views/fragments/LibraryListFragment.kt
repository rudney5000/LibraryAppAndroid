package ru.bmstu.libraryapp.presentation.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.databinding.FragmentLibraryListBinding
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.Month
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.MainViewModel
import ru.bmstu.libraryapp.presentation.views.adapters.LibraryItemAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.bmstu.libraryapp.data.datasources.InMemoryDataSource
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.domain.entities.DetailMode
import ru.bmstu.libraryapp.domain.entities.LibraryItemType

class LibraryListFragment : BaseFragment() {
    private var _binding: FragmentLibraryListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: LibraryItemAdapter
    private val repository: LibraryRepository by lazy {
        Log.d("LibraryListFragment", "Initializing repository")
        LibraryRepositoryImpl(InMemoryDataSource.getInstance())
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("shouldRefresh")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh) {
                    viewModel.refreshItems()
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("shouldRefresh")
                }
            }

        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        observeViewModel()
    }

    override fun handleBackPressed(): Boolean {
        return false
    }


    private fun observeViewModel() {
        viewModel.libraryItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun setupRecyclerView() {
        adapter = LibraryItemAdapter { item ->
            val action = LibraryListFragmentDirections
                .actionListToDetail(item, DetailMode.CREATE)
            findNavController().navigate(action)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LibraryListFragment.adapter
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
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_choose_item_type))
            .setItems(types) { _, which ->
                val newItem = when (which) {
                    0 -> LibraryItemType.Book(0, "", true, 0, "")
                    1 -> LibraryItemType.Newspaper(0, "", true, 0, Month.JANUARY)
                    2 -> LibraryItemType.Disk(0, "", true, DiskType.CD)
                    else -> throw IllegalArgumentException("Unknown type")
                }

                navigateToDetail(newItem, DetailMode.CREATE)
            }
            .show()
    }

    private fun navigateToDetail(item: LibraryItemType, mode: DetailMode = DetailMode.VIEW) {
        val action = LibraryListFragmentDirections.actionListToDetail(item, mode)
        findNavController().navigate(action)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}