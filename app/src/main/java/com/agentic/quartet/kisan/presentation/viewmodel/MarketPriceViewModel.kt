package com.agentic.quartet.kisan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentic.quartet.kisan.domain.model.MarketPrice
import com.agentic.quartet.kisan.domain.usecase.GetMarketPriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketPriceViewModel @Inject constructor(
    private val getMarketPriceUseCase: GetMarketPriceUseCase
) : ViewModel() {

    private val _marketPrice = MutableStateFlow<MarketPrice?>(null)
    val marketPrice = _marketPrice.asStateFlow()

    fun fetchMarketPrice(commodity: String) {
        viewModelScope.launch {
            val price = getMarketPriceUseCase(commodity)
            _marketPrice.value = price
        }
    }
}