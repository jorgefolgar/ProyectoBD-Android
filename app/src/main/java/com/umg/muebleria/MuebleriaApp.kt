package com.umg.muebleria

import android.app.Application
import com.umg.muebleria.data.local.SessionManager
import com.umg.muebleria.data.remote.ApiClient

/**
 * Punto de entrada de la aplicación.
 * Inicializa SessionManager y ApiClient para que el token se inyecte automáticamente.
 */
class MuebleriaApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        ApiClient.init(sessionManager)
    }
}
