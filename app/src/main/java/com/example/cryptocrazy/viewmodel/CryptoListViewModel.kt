package com.example.cryptocrazy.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptocrazy.model.CryptoListItem
import com.example.cryptocrazy.repository.CryptoRepository
import com.example.cryptocrazy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoListViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {
    var cryptoList = mutableStateOf<List<CryptoListItem>>(listOf())
    var errorMessage = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    private var initialCryptoList = listOf<CryptoListItem>()
    private var isSearchStaring = true

    init {
        loadCryptos()
    }

    fun searchCryptoList(query: String) {
        val listToSearch = if (isSearchStaring) {
            cryptoList.value
        } else {
            initialCryptoList
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()) {
                cryptoList.value = initialCryptoList
                isSearchStaring = true
                return@launch
            }

            val results = listToSearch.filter {
                it.currency.contains(query.trim(), ignoreCase = true)
            }

            if (isSearchStaring) {
                initialCryptoList = cryptoList.value
                isSearchStaring = false
            }

            cryptoList.value = results
        }
    }

    fun loadCryptos() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.getCryptoList()) {
                is Resource.Success -> {
                    val cryptoItems = result.data!!.mapIndexed { _, cryptoListItem ->
                        CryptoListItem(cryptoListItem.currency, cryptoListItem.price)
                    }

                    errorMessage.value = ""
                    isLoading.value = false
                    cryptoList.value += cryptoItems
                }
                is Resource.Error -> {
                    errorMessage.value = result.message!!
                    isLoading.value = false
                }
                else -> Unit
            }
        }
    }
}