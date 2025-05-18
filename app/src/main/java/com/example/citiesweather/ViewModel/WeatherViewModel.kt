package com.example.citiesweather.ViewModel

import android.content.Intent
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.citiesweather.R
import com.example.citiesweather.UI.NoInternetActivity
import com.example.citiesweather.data.models.WeatherInfo
import com.example.citiesweather.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadWeather(city: String) {
        viewModelScope.launch {
            try {
                val result = repository.getWeatherForCity(city)
                _weatherInfo.value = result
                if (result == null) {
                    _error.value = "Ошибка загрузки данных"
                }
            } catch (e: Exception) {
                Log.e("WeatherError", "Ошибка загрузки данных: ${e.localizedMessage}")
                _error.value = "Ошибка подключения"
            }
        }
    }
}
