package ru.bmstu.libraryapp.presentation.ui.adapters

import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.bmstu.libraryapp.R
import ru.bmstu.libraryapp.databinding.ActivityLibraryItemDetailBinding
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Month
import ru.bmstu.libraryapp.domain.entities.Newspaper

class LibraryItemDetailActivity: ComponentActivity() {
    private lateinit var binding: ActivityLibraryItemDetailBinding
    private var currentItem: LibraryItem? = null
    private val isEditMode: Boolean
        get() = currentItem == null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemId = intent.getIntExtra("item_id", -1)
        val itemTitle = intent.getStringExtra("item_title")
        val itemIsAvailable = intent.getBooleanExtra("item_is_available", false)
        val itemType = intent.getStringExtra("item_type")

        currentItem = when (itemType) {
            "book" -> Book(
                id = itemId,
                title = itemTitle ?: "",
                isAvailable = itemIsAvailable,
                author = intent.getStringExtra("item_author") ?: "",
                pages = intent.getIntExtra("item_pages", 0)
            )
            "newspaper" -> Newspaper(
                id = itemId,
                title = itemTitle ?: "",
                isAvailable = itemIsAvailable,
                issueNumber = intent.getIntExtra("item_issue_number", 0),
                month = Month.entries[intent.getIntExtra("item_month", 0)]
            )
            "disk" -> Disk(
                id = itemId,
                title = itemTitle ?: "",
                isAvailable = itemIsAvailable,
                type = DiskType.valueOf(intent.getStringExtra("item_disk_type") ?: DiskType.entries[0].name)
            )
            else -> null
        }
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            currentItem?.let { item ->
                titleInput.setText(item.title)
                availabilitySwitch.isChecked = item.isAvailable

                iconView.setImageResource(when(item) {
                    is Book -> R.drawable.ic_book_24
                    is Newspaper -> R.drawable.ic_newspaper_24
                    is Disk -> R.drawable.ic_disk_24
                    else -> R.drawable.ic_book_24
                })

                when (item) {
                    is Book -> addBookFields(item)
                    is Newspaper -> addNewspaperFields(item)
                    is Disk -> addDiskFields(item)
                    else -> {}
                }
            }

            if (!isEditMode) {
                titleInput.isEnabled = false
                availabilitySwitch.isEnabled = false
                saveButton.isEnabled = false
            }

            saveButton.setOnClickListener {
                val updatedItem = when (val item = currentItem) {
                    is Book -> item.copy(
                        title = titleInput.text.toString(),
                        isAvailable = availabilitySwitch.isChecked
                    )
                    is Newspaper -> item.copy(
                        title = titleInput.text.toString(),
                        isAvailable = availabilitySwitch.isChecked
                    )
                    is Disk -> item.copy(
                        title = titleInput.text.toString(),
                        isAvailable = availabilitySwitch.isChecked
                    )
                    else -> null
                }

                updatedItem?.let {
                    val intent = Intent().apply {
                        putExtra("item_id", it.id)
                        putExtra("item_title", it.title)
                        putExtra("item_is_available", it.isAvailable)
                        putExtra("item_type", when(it) {
                            is Book -> "book"
                            is Newspaper -> "newspaper"
                            is Disk -> "disk"
                            else -> ""
                        })
                        when(it) {
                            is Book -> {
                                putExtra("item_author", it.author)
                                putExtra("item_pages", it.pages)
                            }
                            is Newspaper -> {
                                putExtra("item_issue_number", it.issueNumber)
                                putExtra("item_month", it.month)
                            }
                            is Disk -> {
                                putExtra("item_disk_type", it.type)
                            }
                        }
                    }
                    setResult(RESULT_OK, intent)
                }
                finish()
            }
        }
    }

    private fun addBookFields(book: Book) {
        addField("Pages", book.pages.toString())
        addField("Author", book.author)
    }

    private fun addNewspaperFields(newspaper: Newspaper) {
        addField("Issue Number", newspaper.issueNumber.toString())
        addField("Month", newspaper.month.toString())
    }

    private fun addDiskFields(disk: Disk) {
        addField("Type", disk.type.toString())
    }

    private fun addField(label: String, value: String) {
        val layout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8.dp)
            }
            hint = label
            isEnabled = isEditMode
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val editText = TextInputEditText(this).apply {
            setText(value)
            isEnabled = isEditMode
        }

        layout.addView(editText)
        binding.specificFieldsContainer.addView(layout)
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

