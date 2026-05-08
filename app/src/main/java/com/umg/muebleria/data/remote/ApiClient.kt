package com.umg.muebleria.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Configuración de Retrofit con autenticación por token.
 * En debug, confía en el certificado self-signed de IIS Express.
 */
object ApiClient {

    private var sessionManager: SessionManager? = null
    private var _retrofit: Retrofit? = null
    private var _api: MuebleriaApi? = null

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
        _retrofit = null
        _api = null
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun buildOkHttp(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { sessionManager?.getToken() })
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // En debug: confiar en certificado self-signed de IIS Express.
        if (BuildConfig.DEBUG) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            
            // Solo forzamos Host localhost cuando la base apunta al alias del emulador.
            // En teléfono físico (IP LAN), dejar el Host original evita 400 Invalid Hostname.
            if (BuildConfig.API_BASE_URL.contains("10.0.2.2")) {
                builder.addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Host", "localhost")
                        .build()
                    chain.proceed(request)
                }
            }
        }

        return builder.build()
    }

    val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                val gson = GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .create()
                _retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(buildOkHttp())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return _retrofit!!
        }

    val api: MuebleriaApi
        get() {
            if (_api == null) {
                _api = retrofit.create(MuebleriaApi::class.java)
            }
            return _api!!
        }
}
