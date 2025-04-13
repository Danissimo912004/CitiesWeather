package com.example.citiesweather.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClientService {

    // API Ninjas
    private val cityClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-Api-Key", "1yRKQkHTlWjQZn9R1NagyA==QQlAeuUBxtuvaEkz")
                .build()
            chain.proceed(request)
        })
        .build()

    fun getCityService(): CityService {
        return Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/")
            .client(cityClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CityService::class.java)
    }

    fun getWeatherService(): WeatherService {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}
