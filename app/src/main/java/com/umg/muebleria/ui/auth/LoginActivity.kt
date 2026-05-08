package com.umg.muebleria.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.remote.ApiClient
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión.
 * Al autenticar, genera token y navega a MainActivity.
 */
class LoginActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val session = (application as MuebleriaApp).sessionManager
        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        val etLogin = findViewById<TextInputEditText>(R.id.etLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val progress = findViewById<CircularProgressIndicator>(R.id.progressLogin)

        btnLogin.setOnClickListener {
            val login = etLogin.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString()?.trim() ?: ""

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                val result = repository.login(login, password)
                progress.visibility = View.GONE
                btnLogin.isEnabled = true

                result.onSuccess { response ->
                    session.saveLogin(response)
                    ApiClient.init(session) // Reinicializar con nuevo token
                    Toast.makeText(this@LoginActivity, "Bienvenido, ${response.fullName}", Toast.LENGTH_SHORT).show()
                    goToMain()
                }.onFailure { e ->
                    val msg = e.message?.lowercase().orEmpty()
                    val uiMsg = if (
                        msg.contains("401") ||
                        msg.contains("unauthorized") ||
                        msg.contains("credenciales")
                    ) {
                        "Credenciales inválidas"
                    } else {
                        "No se pudo conectar al servidor. Verifica IP/puerto y que el backend esté corriendo."
                    }
                    Toast.makeText(this@LoginActivity, uiMsg, Toast.LENGTH_LONG).show()
                }
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
