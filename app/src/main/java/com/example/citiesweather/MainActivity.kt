package com.example.citiesweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.citiesweather.data.network.ClientService
import com.example.citiesweather.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import com.example.citiesweather.UI.NoInternetActivity
import com.example.citiesweather.ViewModel.WeatherViewModel
import com.example.citiesweather.ViewModel.WeatherViewModelFactory
import com.example.citiesweather.data.models.WeatherInfo
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var localTime: TextView
    private lateinit var repository: WeatherRepository
    private val cities = listOf("Rome", "Milan", "Florence", "Venice", "Pisa")

    // Элементы UI
    private lateinit var citySpinner: Spinner
    private lateinit var cityImage: ImageView
    private lateinit var cityName: TextView
    private lateinit var temperature: TextView
    private lateinit var dailyForecast: TextView
    private lateinit var cityDescription: TextView
    private lateinit var forecastContainer: LinearLayout
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)

        // Инициализация UI
        citySpinner = findViewById(R.id.citySpinner)
        cityImage = findViewById(R.id.cityImage)
        cityName = findViewById(R.id.cityName)
        temperature = findViewById(R.id.temperature)
        localTime = findViewById(R.id.localTime)
        cityDescription = findViewById(R.id.cityDescription)
        forecastContainer = findViewById(R.id.forecastContainer)

        // Инициализация репозитория
        val cityService = ClientService.getCityService()
        val weatherService = ClientService.getWeatherService()
        repository = WeatherRepository(cityService, weatherService)

        val factory = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(this, factory).get(WeatherViewModel::class.java)

        // Настройка Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapter

        // Обработчик выбора города
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCity = cities[position]
                weatherViewModel.loadWeather(selectedCity)

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Загрузка погоды по умолчанию
        weatherViewModel.loadWeather("rome")


        lifecycleScope.launchWhenStarted {
            weatherViewModel.weatherInfo.collect { result ->
                if (result != null) {
                    updateUI(result)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            weatherViewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun updateUI(result: WeatherInfo) {
        cityImage.setImageResource(getCityImageRes(result.city))
        cityName.text = result.city
        temperature.text = "${result.temperature}°C"
        localTime.text = getCurrentTime()
        cityDescription.text = getCityDescription(result.city)

        forecastContainer.removeAllViews()

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = java.text.SimpleDateFormat("EE", Locale("ru"))

        for (i in result.forecastDates.indices) {
            val date = dateFormat.parse(result.forecastDates[i])
            val dayOfWeek = dayFormat.format(date ?: continue).uppercase()

            val itemView = layoutInflater.inflate(R.layout.forecast_item, forecastContainer, false)
            val dayText = itemView.findViewById<TextView>(R.id.dayOfWeek)
            val maxTemp = itemView.findViewById<TextView>(R.id.tempMax)
            val minTemp = itemView.findViewById<TextView>(R.id.tempMin)

            dayText.text = dayOfWeek
            maxTemp.text = "Макс: ${result.dailyMax[i]}°C"
            minTemp.text = "Мин: ${result.dailyMin[i]}°C"

            forecastContainer.addView(itemView)
        }
    }

    private fun loadWeather(city: String) {
        lifecycleScope.launch {
            try {
                val result = repository.getWeatherForCity(city)
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        cityImage.setImageResource(getCityImageRes(city))
                        cityName.text = result.city
                        temperature.text = "${result.temperature}°C"
                        localTime.text = getCurrentTime()
                        cityDescription.text = getCityDescription(city)

                        // Чистим контейнер прогнозов
                        forecastContainer.removeAllViews()

                        // Форматируем дни недели
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dayFormat = java.text.SimpleDateFormat("EE", Locale("ru"))

                        for (i in result.forecastDates.indices) {
                            val date = dateFormat.parse(result.forecastDates[i])
                            val dayOfWeek = dayFormat.format(date ?: continue).uppercase()

                            // Подключаем item layout
                            val itemView = layoutInflater.inflate(R.layout.forecast_item, forecastContainer, false)

                            val dayText = itemView.findViewById<TextView>(R.id.dayOfWeek)
                            val maxTemp = itemView.findViewById<TextView>(R.id.tempMax)
                            val minTemp = itemView.findViewById<TextView>(R.id.tempMin)

                            // Устанавливаем значения
                            dayText.text = dayOfWeek
                            maxTemp.text = "Макс: ${result.dailyMax[i]}°C"
                            minTemp.text = "Мин: ${result.dailyMin[i]}°C"

                            // Добавляем в контейнер
                            forecastContainer.addView(itemView)
                        }

                    } else {
                        cityName.text = "Ошибка загрузки"
                        temperature.text = ""
                        localTime.text = ""
                        cityDescription.text = ""
                        forecastContainer.removeAllViews()
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherError", "Ошибка загрузки данных: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@MainActivity, NoInternetActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }


    private fun getCityDescription(city: String): String {
        return when (city.lowercase()) {
            "rome" -> "Рим – столица Италии, огромный многонациональный город, история которого насчитывает почти три тысячи лет. Его архитектура и произведения искусства оказали огромное влияние на мировую культуру. Развалины античного Форума и Колизея демонстрируют былое величие Римской империи. Ватикан, резиденция руководства Римско-католической церкви, пользуется огромной популярностью у туристов благодаря собору Святого Петра и многочисленным музеям. Среди них – Сикстинская капелла, где можно увидеть знаменитые фрески Микеланджело."
            "florence" -> "Флоренция, столица итальянского региона Тоскана, славится произведениями искусства и архитектурой эпохи Возрождения. Одна из главных достопримечательностей города – кафедральный собор Дуомо. Его покрытый черепицей купол был создан по проекту Брунеллески, а колокольню проектировал Джотто. Среди экспонатов Галереи Академии можно отметить скульптуру \"Давид\" Микеланджело. В галерее Уффици хранятся \"Рождение Венеры\" Боттичелли и \"Благовещение\" Леонардо да Винчи."
            "milan" -> "Милан – крупный город на севере Италии, расположенный в Ломбардии, мировая столица дизайна и моды. Здесь находится фондовая биржа Италии, поэтому город также считается финансовым центром страны. Милан славится роскошными ресторанами и бутиками, а также готическим кафедральным собором Дуомо и церковью Санта-Мария-делле-Грацие, в которой можно увидеть знаменитую фреску \"Тайная вечеря\" работы Леонардо да Винчи."
            "venice" -> "Венеция – столица одноименной области на севере Италии. Город расположен на более чем 100 небольших островах в лагуне Адриатического моря. Здесь совсем нет дорог, движение происходит только по каналам. Самый оживленный – Гранд-канал. Вдоль него расположено множество дворцов в готическом стиле и стиле эпохи Возрождения. Площадь Святого Марка – главная в городе. Здесь находится собор Святого Марка, украшенный мозаикой в византийском стиле, а также колокольная башня (кампанила), с которой открывается вид на красные крыши городских домов."
            "pisa" -> "Пиза – небольшой город в итальянском регионе Тоскана, получивший всемирную известность благодаря Пизанской башне. Она представляет собой колокольню из белого мрамора высотой 56 метров в романском стиле, которая отклонилась от вертикали уже во время строительства, завершившегося в 1372 году. Башня является частью ансамбля площади Чудес, в который также входят мраморный Пизанский собор, баптистерий с уникальной акустикой, в котором ежедневно поют непрофессиональные певцы, и кладбище Кампо-Санто."
            else -> "Информация о городе недоступна."
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
