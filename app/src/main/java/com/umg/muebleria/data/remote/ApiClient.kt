package com.umg.muebleria.data.remote

import com.umg.muebleria.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuración de Retrofit apuntando a [BuildConfig.API_BASE_URL].
 * El backend (VB.NET / Web API) es quien debe llamar a Oracle mediante procedimientos/paquetes PL/SQL.
 */
object ApiClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: MuebleriaApi by lazy { retrofit.create(MuebleriaApi::class.java) }
}
