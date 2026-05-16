package com.umg.muebleria.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.remote.ApiClient
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.localization.LanguagePicker
import com.umg.muebleria.ui.MainActivity
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión.
 */
class LoginActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val session = (application as MuebleriaApp).sessionManager
        if (session.isLoggedIn()) {
            lifecycleScope.launch {
                repository.refreshCartFromServer(session)
                goToMain()
            }
            return
        }

        val etLogin = findViewById<TextInputEditText>(R.id.etLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val btnLanguage = findViewById<MaterialButton>(R.id.btnLanguage)
        val progress = findViewById<CircularProgressIndicator>(R.id.progressLogin)

        btnLanguage.setOnClickListener {
            LanguagePicker.show(this)
        }

        btnLogin.setOnClickListener {
            val login = etLogin.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString()?.trim() ?: ""

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.login_required_fields), Toast.LENGTH_SHORT).show()
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
                    ApiClient.init(session)
                    repository.refreshCartFromServer(session)
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_welcome, response.fullName),
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMain()
                }.onFailure { e ->
                    Log.e(TAG, "Login falló → ${BuildConfig.API_BASE_URL}", e)
                    val msg = e.message?.lowercase().orEmpty()
                    val cause = e.cause?.message?.lowercase().orEmpty()
                    when {
                        msg.contains("401") ||
                            msg.contains("unauthorized") ||
                            msg.contains("credenciales") ->
                            Toast.makeText(
                                this@LoginActivity,
                                getString(R.string.login_error_credentials),
                                Toast.LENGTH_SHORT
                            ).show()
                        msg.contains("certificate") ||
                            msg.contains("ssl") ||
                            msg.contains("handshake") ||
                            cause.contains("certificate") ||
                            cause.contains("ssl") ->
                            showConnectionErrorDialog(
                                title = getString(R.string.login_error_ssl_title),
                                message = getString(
                                    R.string.login_error_ssl_message,
                                    BuildConfig.API_BASE_URL
                                ),
                                detail = e.message
                            )
                        else ->
                            showConnectionErrorDialog(
                                title = getString(R.string.login_error_network_title),
                                message = getString(
                                    R.string.login_error_network_message,
                                    BuildConfig.API_BASE_URL
                                ),
                                detail = e.message
                            )
                    }
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

    private fun showConnectionErrorDialog(title: String, message: String, detail: String?) {
        val body = buildString {
            append(message)
            val d = detail?.trim().orEmpty()
            if (d.isNotEmpty()) {
                append("\n\n")
                append(getString(R.string.login_error_detail, d))
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(body)
            .setPositiveButton(R.string.accept, null)
            .show()
    }
}
