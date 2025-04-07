package ru.bmstu.libraryapp.presentation.views.ativities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.data.datasources.InMemoryDataSource
import ru.bmstu.libraryapp.data.repositories.LibraryRepositoryImpl
import ru.bmstu.libraryapp.databinding.ActivityLibraryItemDetailBinding
import ru.bmstu.libraryapp.databinding.ItemDetailFieldBinding
import ru.bmstu.libraryapp.domain.entities.*
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.utils.getParcelableExtraCompat
import ru.bmstu.libraryapp.presentation.viewmodels.LibraryItemDetailViewModel

class LibraryItemDetailActivity: ComponentActivity() {
    private lateinit var binding: ActivityLibraryItemDetailBinding
    private val repository: LibraryRepository = LibraryRepositoryImpl(InMemoryDataSource.getInstance())
    private lateinit var currentMode: DetailMode

    private val viewModel: LibraryItemDetailViewModel by viewModels {
        val item = intent.getParcelableExtraCompat<ParcelableLibraryItem>(EXTRA_ITEM)
        val mode = DetailMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: DetailMode.VIEW.name)
        LibraryItemDetailViewModel.Factory(repository, item, mode)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentMode = DetailMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: DetailMode.VIEW.name)
        setupObservers()
        setupButtons()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupObservers() {
        viewModel.item.observe(this) { item ->
            item?.let { setupViews(it) }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                viewModel.item.value?.let { item ->
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(EXTRA_ITEM, item as ParcelableLibraryItem)
                        putExtra(EXTRA_IS_NEW_ITEM, currentMode == DetailMode.CREATE)
                    })
                }
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            val currentItem = viewModel.item.value ?: return@setOnClickListener

            val updatedItem = when (currentItem) {
                is Book -> currentItem.copy(
                    title = binding.titleInput.text.toString(),
                    isAvailable = binding.availabilitySwitch.isChecked,
                    author = getFieldValue("author") ?: currentItem.author,
                    pages = getFieldValue("pages")?.toIntOrNull() ?: currentItem.pages
                )
                is Newspaper -> currentItem.copy(
                    title = binding.titleInput.text.toString(),
                    isAvailable = binding.availabilitySwitch.isChecked,
                    issueNumber = getFieldValue("issue_number")?.toIntOrNull() ?: currentItem.issueNumber,
                    month = Month.valueOf(getFieldValue("month") ?: currentItem.month.name)
                )
                is Disk -> currentItem.copy(
                    title = binding.titleInput.text.toString(),
                    isAvailable = binding.availabilitySwitch.isChecked,
                    type = DiskType.valueOf(getFieldValue("disk_type") ?: currentItem.type.name)
                )
                else -> null
            }

            updatedItem?.let { viewModel.saveItem(it) }
        }

        binding.availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (currentMode == DetailMode.VIEW) {
                viewModel.updateAvailability(isChecked)
            }
        }
    }

    private fun setupViews(item: LibraryItem) {
        binding.apply {
            titleInput.setText(item.title)
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

            when (currentMode) {
                DetailMode.VIEW -> {
                    titleInput.isEnabled = false
                    availabilitySwitch.isEnabled = true
                    saveButton.visibility = View.GONE
                }
                DetailMode.CREATE, DetailMode.EDIT -> {
                    titleInput.isEnabled = true
                    availabilitySwitch.isEnabled = true
                    saveButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getFieldValue(fieldId: String): String? {
        return binding.specificFieldsContainer.findViewWithTag<TextInputEditText>(fieldId)?.text?.toString()
    }

    private fun addBookFields(book: Book) {
        addField(
            getString(R.string.tag_author),
            getString(R.string.field_author),
            book.author,
            currentMode != DetailMode.VIEW
        )
        addField(
            getString(R.string.tag_pages),
            getString(R.string.field_pages),
            book.pages.toString(),
            currentMode != DetailMode.VIEW
        )
    }

    private fun addNewspaperFields(newspaper: Newspaper) {
        addField(
            getString(R.string.tag_issue_number),
            getString(R.string.field_issue_number),
            newspaper.issueNumber.toString(),
            currentMode != DetailMode.VIEW
        )
        addField(
            getString(R.string.tag_month),
            getString(R.string.field_month),
            newspaper.month.toString(),
            currentMode != DetailMode.VIEW
        )
    }

    private fun addDiskFields(disk: Disk) {
        addField(
            getString(R.string.tag_disk_type),
            getString(R.string.field_disk_type),
            disk.type.toString(),
            currentMode != DetailMode.VIEW
        )
    }

    private fun addField(tag: String, label: String, value: String, isEnabled: Boolean) {
        val fieldBinding = ItemDetailFieldBinding.inflate(layoutInflater)
        fieldBinding.apply {
            textInputLayout.hint = label
            textInputLayout.isEnabled = isEnabled
            textInputEditText.setText(value)
            textInputEditText.isEnabled = isEnabled
            textInputEditText.tag = tag
        }
        binding.specificFieldsContainer.addView(fieldBinding.root)
    }

    companion object {
        const val EXTRA_ITEM = "extra_item"
        const val EXTRA_IS_NEW_ITEM = "extra_is_new_item"
        private const val EXTRA_MODE = "extra_mode"

        enum class DetailMode {
            VIEW, CREATE, EDIT
        }

        fun createIntent(
            context: Context,
            item: LibraryItem?,
            mode: DetailMode
        ): Intent {
            return Intent(context, LibraryItemDetailActivity::class.java).apply {
                putExtra(EXTRA_ITEM, item as? ParcelableLibraryItem)
                putExtra(EXTRA_MODE, mode.name)
                putExtra(EXTRA_IS_NEW_ITEM, mode == DetailMode.CREATE)
            }
        }
    }
}