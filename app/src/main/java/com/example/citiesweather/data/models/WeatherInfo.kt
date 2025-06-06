package com.example.citiesweather.data.models

data class WeatherInfo(
    val city: String,
    val temperature: Double,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val dailyMax: List<Double>,
    val dailyMin: List<Double>,
    val forecastDates: List<String>
)


