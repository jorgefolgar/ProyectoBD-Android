package com.umg.muebleria

import android.app.Application
import com.umg.muebleria.data.local.SessionManager
import com.umg.muebleria.data.remote.ApiClient
import com.umg.muebleria.localization.AppLocale

/**
 * Punto de entrada de la aplicación.
 * Aplica idioma guardado, inicializa SessionManager y ApiClient (token en Retrofit y Glide).
 */
class MuebleriaApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    /**
     * Se incrementa al recibir catálogo/detalle desde la API. Glide usa esto en [com.bumptech.glide.signature.ObjectKey]
     * junto al productId para invalidar caché cuando la foto cambia en el servidor sin cambiar la URL.
     */
    @Volatile
    var productImagesLoadEpoch: Long = System.currentTimeMillis()
        private set

    fun notifyProductImagesMayHaveChanged() {
        productImagesLoadEpoch = System.currentTimeMillis()
    }

    override fun onCreate() {
        AppLocale.applyPersistedLocale(this)
        super.onCreate()
        sessionManager = SessionManager(this)
        ApiClient.init(sessionManager)
    }
}
