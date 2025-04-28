package ru.bmstu.libraryapp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun ioThread(block: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        block()
    }
}