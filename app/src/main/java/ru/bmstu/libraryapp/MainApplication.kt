package ru.bmstu.libraryapp

import android.app.Application
import android.content.Context
import ru.bmstu.libraryapp.di.AppComponent
import ru.bmstu.libraryapp.di.DaggerAppComponent

class MainApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()
    }

    companion object {
        lateinit var INSTANCE: MainApplication
            private set

        fun get(context: Context): MainApplication {
            return context.applicationContext as MainApplication
        }
    }
}