package com.guiagym.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.local.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val tokenDataStore: TokenDataStore) : ViewModel() {

    // null = comprobando, true = logueado, false = no logueado
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            val token = tokenDataStore.token.first()
            _isLoggedIn.value = !token.isNullOrBlank()
        }
    }

    class Factory(private val tokenDataStore: TokenDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(tokenDataStore) as T
    }
}
