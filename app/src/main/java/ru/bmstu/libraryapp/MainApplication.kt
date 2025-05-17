package ru.bmstu.libraryapp

import android.app.Application
import ru.bmstu.libraryapp.di.AppComponent
import ru.bmstu.libraryapp.di.DaggerAppComponent

class MainApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()
    }

    companion object {
        fun get(context: android.content.Context): MainApplication {
            return context.applicationContext as MainApplication
        }
    }
}