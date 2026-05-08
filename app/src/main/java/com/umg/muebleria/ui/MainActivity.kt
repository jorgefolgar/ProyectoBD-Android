package com.umg.muebleria.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.ui.admin.AdminMoreFragment
import com.umg.muebleria.ui.admin.clients.ClientsAdminFragment
import com.umg.muebleria.ui.admin.products.ProductsAdminFragment
import com.umg.muebleria.ui.auth.LoginActivity
import com.umg.muebleria.ui.cart.CartFragment
import com.umg.muebleria.ui.catalog.CatalogFragment
import com.umg.muebleria.ui.profile.ProfileFragment

/**
 * Actividad principal con navegación inferior.
 * Menú dinámico según rol: cliente ve catálogo/carrito/perfil, admin ve todo.
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
            goToLogin()
            return
        }

        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = session.getUserFullName()

        bottomNav = findViewById(R.id.bottomNav)

        // Configurar menú según rol
        if (session.isAdmin()) {
            bottomNav.inflateMenu(R.menu.bottom_nav_admin)
        } else {
            bottomNav.inflateMenu(R.menu.bottom_nav_client)
        }

        bottomNav.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            when (item.itemId) {
                R.id.nav_catalog -> loadFragment(CatalogFragment())
                R.id.nav_cart -> loadFragment(CartFragment())
                R.id.nav_clients -> loadFragment(ClientsAdminFragment())
                R.id.nav_products -> loadFragment(ProductsAdminFragment())
                R.id.nav_admin_more -> loadFragment(AdminMoreFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
                else -> false
            }
        }

        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_admin_more) {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                loadFragment(AdminMoreFragment())
            }
        }

        // Cargar catálogo por defecto inmediatamente (sin depender del callback del menú)
        if (savedInstanceState == null) {
            loadFragment(CatalogFragment())
            bottomNav.selectedItemId = R.id.nav_catalog
        }

        // La flecha de "Atrás" solo debe aparecer cuando navegamos desde el menú "Más"
        // (cuando hay backStack en el FragmentManager).
        supportFragmentManager.addOnBackStackChangedListener {
            val canNavigateUp = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.setDisplayHomeAsUpEnabled(canNavigateUp)
            supportActionBar?.setHomeButtonEnabled(canNavigateUp)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.logout_confirm_title)
                    .setMessage(R.string.logout_confirm_message)
                    .setPositiveButton(R.string.accept) { _, _ ->
                        (application as MuebleriaApp).sessionManager.logout()
                        goToLogin()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Al presionar la flecha, retrocedemos al fragment anterior del back stack.
        return if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            true
        } else {
            finish()
            true
        }
    }
}
