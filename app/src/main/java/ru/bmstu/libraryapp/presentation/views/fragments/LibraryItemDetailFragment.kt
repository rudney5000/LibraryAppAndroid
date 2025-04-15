package ru.bmstu.libraryapp.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.data.datasources.InMemoryDataSource
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.databinding.ActivityLibraryItemDetailBinding
import ru.bmstu.libraryapp.databinding.ItemDetailFieldBinding
import ru.bmstu.libraryapp.domain.entities.*
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.LibraryItemDetailViewModel

class LibraryItemDetailFragment : BaseFragment() {
    private var _binding: ActivityLibraryItemDetailBinding? = null

    private val repository: LibraryRepository by lazy {
        LibraryRepositoryImpl(InMemoryDataSource.getInstance())
    }
    private val binding get() = _binding!!
    
    private var item: LibraryItemType? = null
    private var mode: DetailMode = DetailMode.VIEW

    private val viewModel: LibraryItemDetailViewModel by viewModels {
        LibraryItemDetailViewModel.Factory(
            repository = repository,
            initialItem = item,
            mode = mode
        )
    }
    
    private val specificFields = mutableMapOf<String, EditText>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLibraryItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
        setupViews()
        observeViewModel()
    }

    override fun handleBackPressed(): Boolean {
        if (hasUnsavedChanges()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.unsaved_changes_title)
                .setMessage(R.string.unsaved_changes_message)
                .setPositiveButton(R.string.discard) { _, _ ->
                    navigateBack()
                }
                .setNegativeButton(R.string.stay, null)
                .setNeutralButton(R.string.save) { _, _ ->
                    saveItem()
                }
                .show()
            return true
        }
        return false
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentItem = item ?: return false

        if (binding.titleInput.text.toString() != currentItem.title) return true
        if (binding.availabilitySwitch.isChecked != currentItem.isAvailable) return true

        when (currentItem) {
            is LibraryItemType.Book -> {
                if (specificFields[getString(R.string.tag_author)]?.text.toString() != currentItem.author) return true
                if (specificFields[getString(R.string.tag_pages)]?.text.toString().toIntOrNull() != currentItem.pages) return true
            }
            is LibraryItemType.Newspaper -> {
                if (specificFields[getString(R.string.tag_issue_number)]?.text.toString().toIntOrNull() != currentItem.issueNumber) return true
                if (specificFields[getString(R.string.tag_month)]?.text.toString() != currentItem.month.name) return true
            }
            is LibraryItemType.Disk -> {
                if (specificFields[getString(R.string.tag_disk_type)]?.text.toString() != currentItem.type.name) return true
            }
        }
        return false
    }

    private fun setupViews() {
        binding.apply {
            val currentItem = item ?: return

            titleInput.setText(currentItem.title)
            idInput.setText(currentItem.id.toString())
            availabilitySwitch.isChecked = currentItem.isAvailable

            iconView.setImageResource(when(currentItem) {
                is LibraryItemType.Book -> R.drawable.ic_book_24
                is LibraryItemType.Newspaper -> R.drawable.ic_newspaper_24
                is LibraryItemType.Disk -> R.drawable.ic_disk_24
            })

            specificFieldsContainer.removeAllViews()

            when (currentItem) {
                is LibraryItemType.Book -> addBookFields(currentItem)
                is LibraryItemType.Newspaper -> addNewspaperFields(currentItem)
                is LibraryItemType.Disk -> addDiskFields(currentItem)
            }
            when (mode) {
                DetailMode.VIEW -> {
                    titleInput.isEnabled = false
                    availabilitySwitch.isEnabled = false
                    saveButton.visibility = View.GONE
                    specificFields.values.forEach { it.isEnabled = false }
                }
                DetailMode.CREATE, DetailMode.EDIT -> {
                    titleInput.isEnabled = true
                    availabilitySwitch.isEnabled = true
                    saveButton.visibility = View.VISIBLE
                }
            }

            saveButton.setOnClickListener {
                saveItem()
            }
        }
    }

    private fun addBookFields(book: LibraryItemType.Book) {
        addSpecificField(getString(R.string.field_author), getString(R.string.tag_author), book.author)
        addSpecificField(getString(R.string.field_pages), getString(R.string.tag_pages), book.pages.toString())
    }

    private fun addNewspaperFields(newspaper: LibraryItemType.Newspaper) {
        addSpecificField(getString(R.string.field_issue_number), getString(R.string.tag_issue_number), newspaper.issueNumber.toString())
        addSpecificField(getString(R.string.field_month), getString(R.string.tag_month), newspaper.month.name)
    }

    private fun addDiskFields(disk: LibraryItemType.Disk) {
        addSpecificField(getString(R.string.field_disk_type), getString(R.string.tag_disk_type), disk.type.name)
    }

    private fun addSpecificField(hint: String, tag: String, value: String) {
        val fieldBinding = ItemDetailFieldBinding.inflate(
            layoutInflater,
            binding.specificFieldsContainer,
            true
        )
        
        fieldBinding.textInputLayout.hint = hint
        fieldBinding.textInputEditText.setText(value)
        specificFields[tag] = fieldBinding.textInputEditText
    }

    private fun saveItem() {
        val updatedItem = when (val originalItem = item) {
            is LibraryItemType.Book -> createUpdatedBook(originalItem)
            is LibraryItemType.Newspaper -> createUpdatedNewspaper(originalItem)
            is LibraryItemType.Disk -> createUpdatedDisk(originalItem)
            null -> throw IllegalArgumentException("Item cannot be null")
        }

        viewModel.saveItem(updatedItem)
        val libraryListFragment = parentFragmentManager
            .fragments
            .firstOrNull { it is LibraryListFragment } as? LibraryListFragment

        libraryListFragment?.refreshList()
        navigateBack()
    }

    private fun createUpdatedBook(originalItem: LibraryItemType.Book): LibraryItemType.Book {
        return LibraryItemType.Book(
            id = originalItem.id,
            title = binding.titleInput.text.toString(),
            isAvailable = binding.availabilitySwitch.isChecked,
            pages = specificFields[getString(R.string.tag_pages)]?.text.toString().toIntOrNull() ?: 0,
            author = specificFields[getString(R.string.tag_author)]?.text.toString()
        )
    }

    private fun createUpdatedNewspaper(originalItem: LibraryItemType.Newspaper): LibraryItemType.Newspaper {
        return LibraryItemType.Newspaper(
            id = originalItem.id,
            title = binding.titleInput.text.toString(),
            isAvailable = binding.availabilitySwitch.isChecked,
            issueNumber = specificFields[getString(R.string.tag_issue_number)]?.text.toString().toIntOrNull() ?: 0,
            month = Month.valueOf(specificFields[getString(R.string.tag_month)]?.text.toString())
        )
    }

    private fun createUpdatedDisk(originalItem: LibraryItemType.Disk): LibraryItemType.Disk {
        return LibraryItemType.Disk(
            id = originalItem.id,
            title = binding.titleInput.text.toString(),
            isAvailable = binding.availabilitySwitch.isChecked,
            type = DiskType.valueOf(specificFields[getString(R.string.tag_disk_type)]?.text.toString())
        )
    }

    private fun navigateBack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable(ARG_ITEM)
            mode = DetailMode.valueOf(it.getString(ARG_MODE, DetailMode.VIEW.name))
        }
    }

    private fun observeViewModel() {
        viewModel.item.observe(viewLifecycleOwner) { item ->
            item?.let { setupViews() }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                (activity as? OnItemSavedListener)?.onItemSaved()
                navigateBack()
            }
        }
    }

    companion object {
        private const val ARG_ITEM = "arg_item"
        private const val ARG_MODE = "arg_mode"

        fun newInstance(item: LibraryItemType, mode: DetailMode): LibraryItemDetailFragment {
            return LibraryItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM, item)
                    putString(ARG_MODE, mode.name)
                }
            }
        }
    }

    interface OnItemSavedListener {
        fun onItemSaved()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}