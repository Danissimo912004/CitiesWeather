package com.example.citiesweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.citiesweather.data.network.ClientService
import com.example.citiesweather.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var repository: WeatherRepository
    private val cities = listOf("Rome", "Milan", "Florence", "Venice", "Pisa")

    // Элементы UI
    private lateinit var citySpinner: Spinner
    private lateinit var cityImage: ImageView
    private lateinit var cityName: TextView
    private lateinit var temperature: TextView
    private lateinit var localTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация UI
        citySpinner = findViewById(R.id.citySpinner)
        cityImage = findViewById(R.id.cityImage)
        cityName = findViewById(R.id.cityName)
        temperature = findViewById(R.id.temperature)
        localTime = findViewById(R.id.localTime)

        // Инициализация репозитория
        val cityService = ClientService.getCityService()
        val weatherService = ClientService.getWeatherService()
        repository = WeatherRepository(cityService, weatherService)

        // Настройка Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapter

        // Обработчик выбора города
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCity = cities[position]
                loadWeather(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Загрузка погоды по умолчанию
        loadWeather("Rome")
    }

    private fun loadWeather(city: String) {
        lifecycleScope.launch {
            val result = repository.getWeatherForCity(city)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    cityImage.setImageResource(getCityImageRes(city))
                    cityName.text = result.city
                    temperature.text = "${result.temperature}°C"
                    localTime.text = getCurrentTime()
                } else {
                    cityName.text = "Ошибка загрузки"
                    temperature.text = ""
                    localTime.text = ""
                }
            }
        }
    }

    private fun getCityImageRes(city: String): Int {
        return when (city.lowercase()) {
            "rome" -> R.drawable.rome
            "florence" -> R.drawable.florence
            "milan" -> R.drawable.milan
            "venice" -> R.drawable.venice
            "pisa" -> R.drawable.piza
            else -> R.drawable.ic_launcher_background
        }
    }

    private fun getCurrentTime(): String {
        val now = Calendar.getInstance()
        val hours = now.get(Calendar.HOUR_OF_DAY)
        val minutes = now.get(Calendar.MINUTE)
        return String.format("Местное время: %02d:%02d", hours, minutes)
    }
}
