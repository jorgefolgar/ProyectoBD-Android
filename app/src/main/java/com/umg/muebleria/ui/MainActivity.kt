package com.umg.muebleria.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.databinding.ActivityMainBinding

/**
 * Actividad principal (portal / arranque).
 * Pantallas de catálogo, carrito y checkout se añadirán según el enunciado.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textApiBase.text = getString(
            com.umg.muebleria.R.string.api_base_label,
            BuildConfig.API_BASE_URL
        )
    }
}
