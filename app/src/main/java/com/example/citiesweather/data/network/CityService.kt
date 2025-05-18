package com.example.citiesweather.data.network


import com.example.citiesweather.data.models.CityResponse
import retrofit2.http.GET
import retrofit2.http.Query



    interface CityService {
        @GET("v1/city?")
        suspend fun getCity(
            @Query("name") name: String
        ): List<CityResponse>
    }

