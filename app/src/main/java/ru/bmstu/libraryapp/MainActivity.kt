package ru.bmstu.libraryapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.bmstu.libraryapp.databinding.ActivityMainBinding
import ru.bmstu.libraryapp.presentation.views.fragments.LibraryItemDetailFragment
import ru.bmstu.libraryapp.presentation.views.fragments.LibraryListFragment

class MainActivity : AppCompatActivity(), LibraryItemDetailFragment.OnItemSavedListener {
    private lateinit var binding: ActivityMainBinding

    private val isDualPane: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    if (isDualPane) R.id.fragment_container else R.id.fragment_container,
                    LibraryListFragment(),
                    "LibraryListFragment"
                )
                .commit()
        }
    }

    override fun onItemSaved() {
        val listFragment = supportFragmentManager.findFragmentByTag("LibraryListFragment") as? LibraryListFragment
        listFragment?.refreshList()

        if(!isDualPane) {
            supportFragmentManager.popBackStack()
        }
    }
}