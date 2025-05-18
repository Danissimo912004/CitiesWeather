package com.example.citiesweather.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citiesweather.data.models.WeatherInfo
import com.example.citiesweather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadWeather(city: String) {
        viewModelScope.launch {
            try {
                val result = repository.getWeatherForCity(city)
                if (result != null) {
                    _weatherInfo.value = result
                } else {
                    _error.value = "Ошибка загрузки данных"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }
}