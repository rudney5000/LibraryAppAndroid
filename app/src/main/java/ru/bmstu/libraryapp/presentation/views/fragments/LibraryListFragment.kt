package ru.bmstu.libraryapp.presentation.views.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.databinding.FragmentLibraryListBinding
import ru.bmstu.libraryapp.presentation.viewmodels.MainViewModel
import ru.bmstu.libraryapp.presentation.views.adapters.LibraryItemAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.bmstu.common.types.DetailMode
import ru.bmstu.common.types.DiskType
import ru.bmstu.common.types.LibraryMode
import ru.bmstu.common.types.Month
import ru.bmstu.data.datasources.InMemoryDataSource
import ru.bmstu.data.filters.LibraryFilter
import ru.bmstu.data.filters.SortBy
import ru.bmstu.data.network.NetworkModule.googleBooksService
import ru.bmstu.data.repositories.impl.GoogleBooksRepositoryImpl
import ru.bmstu.data.repositories.impl.LibraryRepositoryImpl
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.ViewModelFactory
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState
import ru.bmstu.libraryapp.presentation.viewmodels.SearchViewModel

class LibraryListFragment : BaseFragment() {
    private var _binding: FragmentLibraryListBinding? = null
    private val binding get() = _binding!!
    private var lastCreatedItemId: Int? = null
    private var lastDeletedItem: LibraryItemType? = null
    private lateinit var adapter: LibraryItemAdapter

    private val repository: LibraryRepository by lazy {
        LibraryRepositoryImpl(InMemoryDataSource.getInstance())
    }

    private val googleBooksRepository: GoogleBooksRepository by lazy {
        GoogleBooksRepositoryImpl(googleBooksService, requireContext())
    }

    val viewModel: MainViewModel by viewModels {
        ViewModelFactory.create(repository, googleBooksRepository, requireContext())
    }

    private val searchViewModel: SearchViewModel by viewModels {
        ViewModelFactory.create(repository, googleBooksRepository, requireContext())
    }

    private var isSearchMode = false
    private var libraryMode = LibraryMode.LOCAL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        setupSearchView()
        setupToolbar()
        observeViewModel()
        observeSearchViewModel()
    }

    override fun handleBackPressed(): Boolean {
        return false
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { uiState ->
                    if (!isSearchMode) {
                        when (uiState) {
                            is MainViewState.Loading -> {
                                binding.loadingState.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            }
                            is MainViewState.Success -> {
                                binding.loadingState.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                adapter.submitList(uiState.data)
                            }
                            is MainViewState.Error -> {
                                binding.loadingState.visibility = View.GONE
                                Snackbar.make(binding.root, uiState.message, Snackbar.LENGTH_LONG)
                                    .setAction("Try again") {
                                        viewModel.refreshItems()
                                    }
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = LibraryItemAdapter { item ->
            openDetailFragment(item, DetailMode.VIEW)
        }.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LibraryListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun updateSortOption(sortBy: SortBy) {
        val currentFilter = LibraryFilter(sortBy = sortBy)
        viewModel.updateFilter(currentFilter.copy(sortBy = sortBy))
    }

    private fun setupFab() {
        binding.addFab.setOnClickListener {
            showItemTypeDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        lastCreatedItemId?.let { scrollToItem(it) }
    }

    fun refreshList() {
        viewModel.refreshItems()
    }
    fun scrollToItem(itemId: Int) {
        lastCreatedItemId = itemId

        val position = adapter.currentList.indexOfFirst {
            it.id == itemId
        }

        if (position >= 0) {
            binding.recyclerView.post {
                binding.recyclerView.smoothScrollToPosition(position)
            }
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
        openDetailFragment(item, mode)
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
                    lastDeletedItem = item
                    viewModel.deleteItem(item.id)

                    val detailContainer = activity?.findViewById<View>(R.id.detail_container)

                    if (detailContainer != null) {
                        val currentDetailFragment = parentFragmentManager
                            .findFragmentById(R.id.detail_container) as? LibraryItemDetailFragment
                        if (currentDetailFragment?.getCurrentItemId() == item.id) {
                            parentFragmentManager.beginTransaction()
                                .remove(currentDetailFragment)
                                .commit()
                        }
                    }
                    Snackbar.make(
                        binding.root,
                        R.string.item_deleted,
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.undo) {
                        lastDeletedItem?.let { restoredItem ->
                            viewModel.restoreItem(restoredItem)
                            lastDeletedItem = null
                            if (detailContainer != null && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                openDetailFragment(restoredItem, DetailMode.VIEW)
                            }
                        }
                    }.show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun openDetailFragment(item: LibraryItemType, mode: DetailMode) {
        val detailFragment = LibraryItemDetailFragment.newInstance(item, mode)

        val detailContainer = activity?.findViewById<View>(R.id.detail_container)

        if (detailContainer != null) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.detail_container, detailFragment)
                .commit()
        } else {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    override fun onDestroyView() {
        binding.loadingState.stopShimmer()
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.library_list_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_library -> {
                    setLibraryMode(LibraryMode.LOCAL)
                    true
                }
                R.id.action_google_books -> {
                    setLibraryMode(LibraryMode.GOOGLE_BOOKS)
                    true
                }
                R.id.sort_by_title -> {
                    menuItem.isChecked = true
                    updateSortOption(SortBy.TITLE)
                    true
                }
                R.id.sort_by_date -> {
                    menuItem.isChecked = true
                    updateSortOption(SortBy.DATE)
                    true
                }
                R.id.sort_by_author -> {
                    menuItem.isChecked = true
                    updateSortOption(SortBy.AUTHOR)
                    true
                }
                else -> false
            }
        }
    }

    private fun setLibraryMode(mode: LibraryMode) {
        libraryMode = mode
        searchViewModel.setLibraryMode(mode)

        viewModel.setLibraryMode(mode)

        when (mode) {
            LibraryMode.LOCAL -> {
                binding.addFab.visibility = View.VISIBLE
                isSearchMode = false
            }
            LibraryMode.GOOGLE_BOOKS -> {
                binding.addFab.visibility = View.GONE
                isSearchMode = false
            }
        }

        binding.toolbar.menu.findItem(R.id.action_library)?.isChecked = mode == LibraryMode.LOCAL
        binding.toolbar.menu.findItem(R.id.action_google_books)?.isChecked = mode == LibraryMode.GOOGLE_BOOKS
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        isSearchMode = true
                        searchViewModel.searchBooks(it)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    isSearchMode = false
                    viewModel.refreshItems()
                }
                return true
            }
        })

            binding.searchView.setOnCloseListener {
                isSearchMode = false
                viewModel.refreshItems()
                false
            }
    }

    private fun observeSearchViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchResults.collect { books ->
                    if (isSearchMode) {
                        adapter.submitList(books)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.state.collect { state ->
                    if (isSearchMode) {
                        when (state) {
                            is MainViewState.Loading -> {
                                binding.loadingState.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            }
                            is MainViewState.Success -> {
                                binding.loadingState.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                            }
                            is MainViewState.Error -> {
                                binding.loadingState.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
