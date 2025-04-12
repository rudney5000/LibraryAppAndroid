package ru.bmstu.libraryapp

import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ru.bmstu.libraryapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController

            if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                onBackPressedDispatcher.addCallback(this) {
                    if (!handleBackPressed()) {
                        finish()
                    }
                }
            }
            Log.d("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            throw e
        }
    }

    private fun handleBackPressed(): Boolean {
        val detailContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.detail_container)
        if (detailContainer != null) {
            val detailFragment = supportFragmentManager.findFragmentById(R.id.detail_container)
            if (detailFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(detailFragment)
                    .commit()
                return true
            }
        }
        return false
    }
}