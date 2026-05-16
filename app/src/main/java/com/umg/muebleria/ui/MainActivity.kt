package com.umg.muebleria.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.remote.ApiClient
import com.umg.muebleria.localization.LanguagePicker
import com.umg.muebleria.ui.auth.LoginActivity
import com.umg.muebleria.ui.cart.CartFragment
import com.umg.muebleria.ui.catalog.CatalogFragment
import com.umg.muebleria.ui.profile.ProfileFragment

/**
 * Actividad principal con navegación inferior (catálogo, carrito, perfil).
 * Administradores entran con la misma cuenta; aquí solo existe la vista de cliente.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbarMain))
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val session = (application as MuebleriaApp).sessionManager
        if (!session.isLoggedIn()) {
            // No usar CLEAR_TASK aquí: puede fallar si aún estamos construyendo esta actividad.
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = session.getUserFullName()

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.inflateMenu(R.menu.bottom_nav_client)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> loadFragment(CatalogFragment())
                R.id.nav_cart -> loadFragment(CartFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(CatalogFragment())
            bottomNav.selectedItemId = R.id.nav_catalog
        }
    }

    override fun onResume() {
        super.onResume()
        val session = (application as MuebleriaApp).sessionManager
        if (session.isLoggedIn()) {
            supportActionBar?.subtitle = session.getUserFullName()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                LanguagePicker.show(this)
                true
            }
            R.id.action_logout -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.logout_confirm_title)
                    .setMessage(R.string.logout_confirm_message)
                    .setPositiveButton(R.string.accept) { _, _ ->
                        // Post: no navegar en el mismo tick que cierra el diálogo (evita conflictos con la ventana).
                        (findViewById<View>(R.id.toolbarMain) ?: window.decorView).post {
                            navigateToLoginReplacingTask()
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        // commit() es asíncrono: al abrir Login con CLEAR_TASK puede ejecutarse después de
        // onSaveInstanceState → IllegalStateException. commitAllowingStateLoss evita ese crash al cerrar sesión.
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()
        return true
    }

    /**
     * Cierra sesión y deja solo el login en la pila.
     * [Intent.FLAG_ACTIVITY_CLEAR_TASK] requiere [Intent.FLAG_ACTIVITY_NEW_TASK] (documentación Android).
     */
    private fun navigateToLoginReplacingTask() {
        if (isFinishing || isDestroyed) return
        val app = application as MuebleriaApp
        app.sessionManager.logout()
        ApiClient.init(app.sessionManager)
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }
}
