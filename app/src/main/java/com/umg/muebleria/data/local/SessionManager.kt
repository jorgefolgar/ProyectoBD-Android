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

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun isAdmin(): Boolean {
        val roleId = getUserRoleId()
        val userType = getUserType().trim().uppercase()
        return roleId == 1 || userType == "A"
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    // ────── Carrito (client-side) ──────

    fun getCart(): MutableList<CarritoItem> {
        val json = prefs.getString(KEY_CART, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<CarritoItem>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveCart(cart: List<CarritoItem>) {
        prefs.edit().putString(KEY_CART, gson.toJson(cart)).apply()
    }

    fun clearCart() {
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
