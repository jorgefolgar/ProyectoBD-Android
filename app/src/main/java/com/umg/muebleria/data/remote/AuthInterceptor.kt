package com.umg.muebleria.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que agrega el token de autenticación a cada request HTTP.
 * El token se obtiene de [tokenProvider] que lee de SharedPreferences.
 */
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
