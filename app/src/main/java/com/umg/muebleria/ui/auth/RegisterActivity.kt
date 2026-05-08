package com.umg.muebleria.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.umg.muebleria.R
import com.umg.muebleria.data.model.RegisterRequest
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Pantalla de registro de nuevo cliente.
 * Delega a la API que ejecuta PKG_MUE_USUARIO.PR_USUARIO_INSERTAR.
 */
class RegisterActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setSupportActionBar(findViewById(R.id.toolbarRegister))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Registro de cuenta"

        val etLogin = findViewById<TextInputEditText>(R.id.etRegLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etFirstName = findViewById<TextInputEditText>(R.id.etRegFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etRegLastName)
        val etDocNumber = findViewById<TextInputEditText>(R.id.etRegDocNumber)
        val etPhone = findViewById<TextInputEditText>(R.id.etRegPhone)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etCity = findViewById<TextInputEditText>(R.id.etRegCity)
        val etDepartment = findViewById<TextInputEditText>(R.id.etRegDepartment)
        val etMunicipality = findViewById<TextInputEditText>(R.id.etRegMunicipality)
        val btnRegister = findViewById<MaterialButton>(R.id.btnDoRegister)
        val progress = findViewById<CircularProgressIndicator>(R.id.progressRegister)

        btnRegister.setOnClickListener {
            val login = etLogin.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString()?.trim() ?: ""
            val firstName = etFirstName.text?.toString()?.trim() ?: ""
            val lastName = etLastName.text?.toString()?.trim() ?: ""

            if (login.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                login = login,
                password = password,
                firstName = firstName,
                lastName = lastName,
                documentNumber = etDocNumber.text?.toString()?.trim()?.toLongOrNull(),
                homePhone = etPhone.text?.toString()?.trim()?.toLongOrNull(),
                email = etEmail.text?.toString()?.trim() ?: "",
                city = etCity.text?.toString()?.trim() ?: "",
                department = etDepartment.text?.toString()?.trim() ?: "",
                municipality = etMunicipality.text?.toString()?.trim() ?: ""
            )

            progress.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            lifecycleScope.launch {
                val result = repository.register(request)
                progress.visibility = View.GONE
                btnRegister.isEnabled = true

                result.onSuccess {
                    Toast.makeText(this@RegisterActivity, "Cuenta creada. Inicia sesión.", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@RegisterActivity, e.message ?: "Error al registrar", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
