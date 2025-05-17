package ru.bmstu.libraryapp.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.bmstu.libraryapp.presentation.views.fragments.LibraryItemDetailFragment
import ru.bmstu.libraryapp.presentation.views.fragments.LibraryListFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class, DomainModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(fragment: LibraryItemDetailFragment)
    fun inject(fragment: LibraryListFragment)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}