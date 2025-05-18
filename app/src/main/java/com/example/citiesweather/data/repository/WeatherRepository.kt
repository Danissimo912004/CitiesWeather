package com.example.citiesweather.data.repository
import com.example.citiesweather.data.models.WeatherInfo
import com.example.citiesweather.data.network.CityService
import com.example.citiesweather.data.network.WeatherService
import com.example.citiesweather.data.models.CityResponse


class WeatherRepository(
    private val cityApi: CityService,
    private val weatherApi: WeatherService
) {
    suspend fun getWeatherForCity(cityName: String): WeatherInfo? {
        return try {
            val cities = cityApi.getCity(name = cityName)
            val city = cities.firstOrNull() ?: return null

            val weather = weatherApi.getWeather(
                lat = city.latitude,
                lon = city.longitude
            )

            val current = weather.current_weather
            val daily = weather.daily

            if (current != null && daily != null) {
                WeatherInfo(
                    city = city.name,
                    temperature = current.temperature,
                    description = "Code: ${current.weathercode}",
                    latitude = city.latitude,
                    longitude = city.longitude,
                    dailyMax = daily.temperature_2m_max,
                    dailyMin = daily.temperature_2m_min,
                    forecastDates = daily.time
                )
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}

