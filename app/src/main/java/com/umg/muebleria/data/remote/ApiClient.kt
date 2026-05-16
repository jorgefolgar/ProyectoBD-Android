package com.umg.muebleria.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Configuración de Retrofit con autenticación por token.
 * El mismo [okHttpClient] se usa con Glide (vía AppGlideModule) para fotos con Bearer y SSL dev.
 */
object ApiClient {

    private var sessionManager: SessionManager? = null
    private var _http: OkHttpClient? = null
    private var _retrofit: Retrofit? = null
    private var _api: MuebleriaApi? = null

    private val apiHost: String? by lazy {
        runCatching { URI(BuildConfig.API_BASE_URL).host?.lowercase() }.getOrNull()
    }

    /** Backend en LAN (emulador 10.0.2.2, servidor 192.168.x.x, etc.). */
    private fun isLanBackendHost(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        if (host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2") return true
        if (host.startsWith("192.168.")) return true
        if (host.startsWith("10.")) return true
        val parts = host.split(".")
        if (parts.size == 4 && parts[0] == "172") {
            val second = parts[1].toIntOrNull() ?: return false
            if (second in 16..31) return true
        }
        return false
    }

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
        _http = null
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

        // En LAN, IIS a veces redirige HTTP→HTTPS con certificado que no valida la IP; confiar host privado.
        val relaxSslForLan = BuildConfig.DEBUG || isLanBackendHost(apiHost)
        if (relaxSslForLan) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        }

        // Solo emulador: el host del PC es 10.0.2.2 pero IIS puede esperar Host: localhost
        if (apiHost == "10.0.2.2") {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Host", "localhost")
                    .build()
                chain.proceed(request)
            }
        }

        return builder.build()
    }

    fun okHttpClient(): OkHttpClient {
        if (_http == null) {
            _http = buildOkHttp()
        }
        return _http!!
    }

    val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                val gson = GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .create()
                _retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(okHttpClient())
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
