package com.example.myapplication.ui.demo

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DemoControlCenter {

    private val _cancelRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    val cancelRequests: SharedFlow<Unit> = _cancelRequests

    fun requestCancel() {
        _cancelRequests.tryEmit(Unit)
    }
}