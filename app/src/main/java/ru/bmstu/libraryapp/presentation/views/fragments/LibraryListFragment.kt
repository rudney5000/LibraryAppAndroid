package ru.bmstu.libraryapp.presentation.views.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toolbar
import androidx.core.widget.addTextChangedListener
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
import ru.bmstu.libraryapp.domain.entities.LibraryMode
import ru.bmstu.libraryapp.domain.repositories.GoogleBooksRepository

class LibraryListFragment : BaseFragment() {
    private var _binding: FragmentLibraryListBinding? = null
    private val binding get() = _binding!!
    private var lastCreatedItemId: Int? = null
    private var lastDeletedItem: LibraryItemType? = null
    private var isLoadUpInProgress = false
    private var isLoadDownInProgress = false
    private var currentMode: LibraryMode = LibraryMode.Local
    private lateinit var adapter: LibraryItemAdapter
    private val repository: LibraryRepository by lazy {
        LibraryRepositoryImpl(
            RoomDataSource(LibraryDatabase.getInstance(requireContext())),
            LibraryPreferences(requireContext())
        )
    }
    private val googleBooksRepository by lazy {
        GoogleBooksRepositoryImpl(
            googleBooksService = NetworkModule.googleBooksService,
            context = requireContext()
        )
    }
    private val preferences by lazy { LibraryPreferences(requireContext()) }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(repository, googleBooksRepository, preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.library_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        val titleMenuItem = menu.findItem(R.id.sort_by_title)
        val dateMenuItem = menu.findItem(R.id.sort_by_date)

        when (preferences.sortOrder) {
            "title" -> titleMenuItem.isChecked = true
            "createdAt" -> dateMenuItem.isChecked = true
        }

        super.onPrepareOptionsMenu(menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_library -> {
                item.isChecked = true
                viewModel.switchToLibrary()
                true
            }
            R.id.action_google_books -> {
                item.isChecked = true
                viewModel.switchToGoogleBooks()
                showGoogleBooksSearchUI()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                    viewModel.mode.collect { mode ->
                        currentMode = mode
                        updateUIForCurrentMode(mode)
                    }
                }
                launch {
                    viewModel.items.collect { items ->
                        adapter.submitList(items) {
                            lastCreatedItemId?.let { scrollToItem(it) }
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
                    viewModel.loadingMore.collect { isLoadingMore ->
                        if (!isLoadingMore) {
                            isLoadUpInProgress = false
                            isLoadDownInProgress = false
                        }
                    }
                }

                launch {
                    viewModel.error.collect { errorMessage ->
                        errorMessage?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                                .setAction("Try again") {
                                    viewModel.refreshItems()
                                }
                                .setAction("Retry") { viewModel.refreshItems() }
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUIForCurrentMode(mode: LibraryMode) {
        when (mode) {
            is LibraryMode.Local -> {
                binding.addFab.show()
                setupOptionsMenuForLocal()
            }
            is LibraryMode.GoogleBooks -> {
                binding.addFab.hide()
                setupOptionsMenuForGoogleBooks()
            }
        }
    }

    private fun setupOptionsMenuForLocal() {
        binding.toolbar.menu.clear()
        requireActivity().menuInflater.inflate(R.menu.library_list_menu, binding.toolbar.menu)

        val titleItem = binding.toolbar.menu.findItem(R.id.sort_by_title)
        val dateItem = binding.toolbar.menu.findItem(R.id.sort_by_date)

        when (viewModel.sortOrder.value) {
            "title" -> titleItem?.isChecked = true
            "createdAt" -> dateItem?.isChecked = true
        }
    }

//    private fun setupOptionsMenuForGoogleBooks() {
//        binding.toolbar.menu.clear()
//
//        // Можно инфлировать другое меню, например google_books_menu.xml
//        // Или просто добавить нужные пункты вручную
//        val menu = binding.toolbar.menu
//
//        menu.add(Menu.NONE, R.id.action_search, Menu.NONE, R.string.search)
//            .setIcon(R.drawable.ic_search)
//            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
//
//        // Если нужно, можно добавить кнопку фильтрации или другие элементы
//    }

    private fun setupOptionsMenuForGoogleBooks() {
        binding.toolbar.menu.clear()
        requireActivity().menuInflater.inflate(R.menu.google_books_menu, binding.toolbar.menu)
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

                    if (!isLoadUpInProgress && firstVisibleItem <= 2) {
                        if (viewModel.currentPageNumber > 0) {
                            isLoadUpInProgress = true
                            viewModel.loadMoreItems(forward = false)
                        }
                    }

                    if (!isLoadDownInProgress && totalItemCount <= lastVisibleItem + 2) {
                        isLoadDownInProgress = true
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
                if (position == RecyclerView.NO_POSITION) return

                val item = adapter.currentList[position]

                if (viewModel.isInGoogleBooksMode) {
                    viewModel.saveGoogleBook(item as LibraryItemType.Book)
                    Snackbar.make(binding.root, "Книга добавлена в библиотеку", Snackbar.LENGTH_SHORT).show()
                } else {
                    lastDeletedItem = item
                    viewModel.deleteItem(item.id)

                    val detailContainer = activity?.findViewById<View>(R.id.detail_container)
                    if (detailContainer != null) {
                        val currentDetailFragment = parentFragmentManager.findFragmentById(R.id.detail_container) as? LibraryItemDetailFragment
                        currentDetailFragment?.let {
                            parentFragmentManager.beginTransaction().remove(it).commit()
                        }
                    }

                    Snackbar.make(binding.root, R.string.item_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            lastDeletedItem?.let { restoredItem ->
                                viewModel.restoreItem(restoredItem)
                                lastDeletedItem = null
                                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    openDetailFragment(restoredItem, DetailMode.VIEW)
                                }
                            }
                        }
                        .show()
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
                query?.let { viewModel.searchBooks(
                    title = it,
                    author = it
                ) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let { viewModel.searchBooks(title = it, author = null) }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean = false
//        })
    }
    private fun showGoogleBooksSearchUI() {
        binding.toolbar.removeAllViews()
        val searchForm = layoutInflater.inflate(R.layout.view_google_books_search, null)
        binding.toolbar.addView(searchForm)

        val etAuthor = searchForm.findViewById<EditText>(R.id.etAuthor)
        val etTitle = searchForm.findViewById<EditText>(R.id.etTitle)
        val btnSearch = searchForm.findViewById<Button>(R.id.btnSearch)

        fun updateButtonState() {
            val authorText = etAuthor.text.toString().trim()
            val titleText = etTitle.text.toString().trim()
            val isAuthorValid = authorText.length >= 3
            val isTitleValid = titleText.length >= 3
            btnSearch.isEnabled = isAuthorValid || isTitleValid
        }

        etAuthor.addTextChangedListener { updateButtonState() }
        etTitle.addTextChangedListener { updateButtonState() }

        btnSearch.setOnClickListener {
            val author = etAuthor.text.toString().takeIf { it.isNotBlank() }
            val title = etTitle.text.toString().takeIf { it.isNotBlank() }

            Log.d("LibraryListFragment", "Начинаем поиск: author=$author, title=$title")

            if (author == null && title == null) {
                Snackbar.make(binding.root, "Введите автора или заголовок", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.searchBooks(author, title)
        }
    }

    override fun onDestroyView() {
        binding.loadingState.stopShimmer()
        super.onDestroyView()
        _binding = null
    }
}