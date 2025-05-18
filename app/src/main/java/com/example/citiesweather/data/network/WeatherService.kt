package com.example.citiesweather.data.network

import com.example.citiesweather.data.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("v1/forecast?current_weather=true&daily=temperature_2m_max,temperature_2m_min&timezone=auto")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): WeatherResponse
}

