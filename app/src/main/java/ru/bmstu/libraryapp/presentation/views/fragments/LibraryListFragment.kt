package ru.bmstu.libraryapp.presentation.views.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.bmstu.libraryapp.data.datasources.RoomDataSource
import ru.bmstu.libraryapp.data.db.LibraryDatabase
import ru.bmstu.libraryapp.data.network.GoogleBooksApiService
import ru.bmstu.libraryapp.data.network.NetworkModule
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.data.repositories.GoogleBooksRepositoryImpl
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.domain.entities.DetailMode
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.GoogleBooksRepository

class LibraryListFragment : BaseFragment() {
    private var _binding: FragmentLibraryListBinding? = null
    private val binding get() = _binding!!
    private var lastCreatedItemId: Int? = null
    private var lastDeletedItem: LibraryItemType? = null
    
    private lateinit var adapter: LibraryItemAdapter
    private val repository: LibraryRepository by lazy {
        LibraryRepositoryImpl(
            RoomDataSource(LibraryDatabase.getInstance(requireContext())),
            LibraryPreferences(requireContext())
            )
    }
    private val googleBooksRepository: GoogleBooksRepository by lazy {
        GoogleBooksRepositoryImpl(NetworkModule.googleBooksService)
    }
    private val preferences by lazy { LibraryPreferences(requireContext()) }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(repository, googleBooksRepository, preferences)
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

        setupSearchView()
        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        observeViewModel()
    }

    override fun handleBackPressed(): Boolean {
        return false
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collect { items ->
                        adapter.submitList(items) {
                            lastCreatedItemId?.let { scrollToItem(it) }
                            updateEmptyState(items.isEmpty())
                        }
                    }
                }

                launch {
                    viewModel.loading.collect { isLoading ->
                        binding.loadingState.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.error.collect { errorMessage ->
                        errorMessage?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                                .setAction("Retry") { viewModel.refreshItems() }
                                .show()
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (!viewModel.loading.value && !viewModel.loadingMore.value &&
                        firstVisibleItem <= 2) {
                        viewModel.loadMoreItems(forward = false)
                    }

                    if (!viewModel.loading.value && !viewModel.loadingMore.value &&
                        totalItemCount <= lastVisibleItem + 2) {
                        viewModel.loadMoreItems(forward = true)
                    }
                }
            })
        }
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
        val position = (binding.recyclerView.adapter as? LibraryItemAdapter)
            ?.currentList
            ?.indexOfFirst { it.id == itemId }
            ?: -1

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

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty && !viewModel.loading.value) {
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSearchView() {
        val searchView = SearchView(requireContext()).apply {
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            queryHint = getString(R.string.search_hint)
        }

        binding.toolbar.addView(searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchBooks(title = it, author = null) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    override fun onDestroyView() {
        binding.loadingState.stopShimmer()
        super.onDestroyView()
        _binding = null
    }
}