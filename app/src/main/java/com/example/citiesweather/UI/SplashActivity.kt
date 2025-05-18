package com.example.citiesweather.UI

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.citiesweather.MainActivity
import com.example.citiesweather.R

class SplashActivity : AppCompatActivity() {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isNetworkAvailable()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, NoInternetActivity::class.java))
            }
            finish()
        }, 2000)
    }




}
