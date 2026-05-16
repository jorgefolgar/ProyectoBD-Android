package com.umg.muebleria.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umg.muebleria.data.model.CarritoItem
import com.umg.muebleria.data.model.LoginResponse

/**
 * Gestión de sesión local: token, datos de usuario y carrito.
 * Usa SharedPreferences para persistencia entre reinicios de la app.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("muebleria_session", Context.MODE_PRIVATE)
    private val cartPrefs: SharedPreferences =
        context.getSharedPreferences("muebleria_cart", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_LOGIN = "user_login"
        private const val KEY_USER_ROLE = "user_role_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_full_name"
        private const val KEY_CART = "cart_items"
    }

    // ────── Auth ──────

    fun saveLogin(response: LoginResponse) {
        prefs.edit()
            .putString(KEY_TOKEN, response.token)
            .putInt(KEY_USER_ID, response.userId)
            .putString(KEY_USER_LOGIN, response.login)
            .putInt(KEY_USER_ROLE, response.roleId)
            .putString(KEY_USER_TYPE, response.userType)
            .putString(KEY_USER_EMAIL, response.email)
            .putString(KEY_USER_NAME, response.fullName)
            .commit()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)
    fun getUserLogin(): String = prefs.getString(KEY_USER_LOGIN, "") ?: ""
    fun getUserRoleId(): Int = prefs.getInt(KEY_USER_ROLE, 0)
    fun getUserType(): String = prefs.getString(KEY_USER_TYPE, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserFullName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    /** Actualiza el nombre mostrado en la UI tras cambios en el perfil (p. ej. desde la API). */
    fun updateUserFullName(fullName: String) {
        prefs.edit().putString(KEY_USER_NAME, fullName.trim()).apply()
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun isAdmin(): Boolean {
        val roleId = getUserRoleId()
        val userType = getUserType().trim().uppercase()
        return roleId == 1 || userType == "A"
    }

    fun logout() {
        // Borrar caché local del carrito: la verdad está en servidor; evita mezclar cuentas en el mismo dispositivo.
        clearCart()
        // commit() evita que otra pantalla lea token aún presente antes de que apply termine en memoria/disco.
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_LOGIN)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_TYPE)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .commit()
    }

    // ────── Carrito (client-side) ──────

    fun getCart(): MutableList<CarritoItem> {
        val json = cartPrefs.getString(KEY_CART, null)
            ?: prefs.getString(KEY_CART, null) // migración desde prefs de sesión
            ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<CarritoItem>>() {}.type
            val cart = gson.fromJson<MutableList<CarritoItem>>(json, type) ?: mutableListOf()
            if (cart.isNotEmpty() && !cartPrefs.contains(KEY_CART)) {
                saveCart(cart)
                prefs.edit().remove(KEY_CART).apply()
            }
            cart
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveCart(cart: List<CarritoItem>) {
        cartPrefs.edit().putString(KEY_CART, gson.toJson(cart)).commit()
    }

    fun clearCart() {
        cartPrefs.edit().remove(KEY_CART).commit()
        prefs.edit().remove(KEY_CART).apply()
    }

    fun addToCart(item: CarritoItem) {
        val cart = getCart()
        val existing = cart.find { it.productId == item.productId }
        if (existing != null) {
            existing.quantity += item.quantity
            existing.recalculate()
        } else {
            item.recalculate()
            cart.add(item)
        }
        saveCart(cart)
    }

    fun removeFromCart(productId: Int) {
        val cart = getCart()
        cart.removeAll { it.productId == productId }
        saveCart(cart)
    }

    fun decrementInCart(productId: Int) {
        val cart = getCart()
        val item = cart.find { it.productId == productId }
        if (item != null) {
            item.quantity -= 1
            if (item.quantity <= 0) {
                cart.remove(item)
            } else {
                item.recalculate()
            }
        }
        saveCart(cart)
    }

    fun getCartTotal(): Double = getCart().sumOf { it.unitPrice * it.quantity }
    fun getCartItemCount(): Int = getCart().sumOf { it.quantity }
}
