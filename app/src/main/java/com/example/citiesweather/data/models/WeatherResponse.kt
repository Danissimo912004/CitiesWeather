package com.example.citiesweather.data.models

data class WeatherResponse(
    val current_weather: CurrentWeather?,
    val daily: DailyForecast?
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int
)

data class DailyForecast(
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val time: List<String>
)
