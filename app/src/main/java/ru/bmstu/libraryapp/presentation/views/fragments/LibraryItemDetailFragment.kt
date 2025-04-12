package ru.bmstu.libraryapp.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.bmstu.libraryapp.LibraryApp
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.databinding.ActivityLibraryItemDetailBinding
import ru.bmstu.libraryapp.databinding.ItemDetailFieldBinding
import ru.bmstu.libraryapp.domain.entities.*
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.LibraryItemDetailViewModel

class LibraryItemDetailFragment : Fragment() {
    private var _binding: ActivityLibraryItemDetailBinding? = null
    private val repository: LibraryRepository by lazy {
        (requireActivity().application as LibraryApp).libraryRepository
    }
    private val binding get() = _binding!!
    
    private val args: LibraryItemDetailFragmentArgs by navArgs()

    private val viewModel: LibraryItemDetailViewModel by viewModels {
        LibraryItemDetailViewModel.Factory(
            repository = repository,
            initialItem = args.item,
            mode = DetailMode.valueOf(args.mode)
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
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            val item = args.item ?: return

            titleInput.setText(item.title)
            idInput.setText(item.id.toString())
            availabilitySwitch.isChecked = item.isAvailable

            iconView.setImageResource(when(item) {
                is Book -> R.drawable.ic_book_24
                is Newspaper -> R.drawable.ic_newspaper_24
                is Disk -> R.drawable.ic_disk_24
                else -> R.drawable.ic_book_24
            })

            specificFieldsContainer.removeAllViews()

            when (item) {
                is Book -> addBookFields(item)
                is Newspaper -> addNewspaperFields(item)
                is Disk -> addDiskFields(item)
                else -> {}
            }

            when (DetailMode.valueOf(args.mode)) {
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

    private fun addBookFields(book: Book) {
        addSpecificField(getString(R.string.field_author), getString(R.string.tag_author), book.author)
        addSpecificField(getString(R.string.field_pages), getString(R.string.tag_pages), book.pages.toString())
    }

    private fun addNewspaperFields(newspaper: Newspaper) {
        addSpecificField(getString(R.string.field_issue_number), getString(R.string.tag_issue_number), newspaper.issueNumber.toString())
        addSpecificField(getString(R.string.field_month), getString(R.string.tag_month), newspaper.month.name)
    }

    private fun addDiskFields(disk: Disk) {
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
        val updatedItem = when (val originalItem = args.item) {
            is Book -> createUpdatedBook(originalItem)
            is Newspaper -> createUpdatedNewspaper(originalItem)
            is Disk -> createUpdatedDisk(originalItem)
            else -> throw IllegalArgumentException("Unknown item type")
        }

        viewModel.saveItem(updatedItem)
        findNavController().navigateUp()
    }

    private fun createUpdatedBook(originalItem: Book): Book {
        return Book(
            id = originalItem.id,
            title = binding.titleInput.text.toString(),
            isAvailable = binding.availabilitySwitch.isChecked,
            pages = specificFields[getString(R.string.tag_pages)]?.text.toString().toIntOrNull() ?: 0,
            author = specificFields[getString(R.string.tag_author)]?.text.toString()
        )
    }

    private fun createUpdatedNewspaper(originalItem: Newspaper): Newspaper {
        return Newspaper(
            id = originalItem.id,
            title = binding.titleInput.text.toString(),
            isAvailable = binding.availabilitySwitch.isChecked,
            issueNumber = specificFields[getString(R.string.tag_issue_number)]?.text.toString().toIntOrNull() ?: 0,
            month = Month.valueOf(specificFields[getString(R.string.tag_month)]?.text.toString())
        )
    }

    private fun createUpdatedDisk(originalItem: Disk): Disk {
        return Disk(
            id = originalItem.id,
            title = binding.titleInput.text.toString(),
            isAvailable = binding.availabilitySwitch.isChecked,
            type = DiskType.valueOf(specificFields[getString(R.string.tag_disk_type)]?.text.toString())
        )
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
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "shouldRefresh",
                    true
                )
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}