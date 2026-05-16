package com.umg.muebleria.data.repository

import com.umg.muebleria.data.local.SessionManager
import com.umg.muebleria.data.model.*
import com.umg.muebleria.data.remote.ApiClient
import com.umg.muebleria.data.remote.MuebleriaApi
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/**
 * Capa de repositorio: solo flujos de cliente (auth, cuenta, catálogo, checkout).
 * Las cancelaciones de corrutinas (p. ej. al cambiar de pestaña o idioma) no se convierten en error.
 */
class MuebleriaRepository(
    private val api: MuebleriaApi = ApiClient.api
) {
    private suspend fun <T> safeCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Respuesta vacía del servidor"))
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                Result.failure(Exception(errorBody))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(login: String, password: String): Result<LoginResponse> =
        safeCall { api.login(LoginRequest(login, password)) }

    suspend fun register(request: RegisterRequest): Result<ApiMessage> =
        safeCall { api.register(request) }

    suspend fun getProfile(): Result<PerfilDto> =
        safeCall { api.getProfile() }

    suspend fun updateProfile(request: ProfileUpdateRequest): Result<ApiMessage> =
        safeCall { api.updateProfile(request) }

    suspend fun changePassword(request: ChangePasswordRequest): Result<ApiMessage> =
        safeCall { api.changePassword(request) }

    suspend fun listProfessions(): Result<List<ProfesionDto>> =
        safeCall { api.listProfessions() }

    suspend fun listCatalog(
        reference: String? = null, name: String? = null,
        typeFilter: String? = null, material: String? = null
    ): Result<List<ProductoDto>> =
        safeCall { api.listCatalog(reference, name, typeFilter, material) }

    suspend fun getCatalogDetail(id: Int): Result<ProductoDetalleDto> =
        safeCall { api.getCatalogDetail(id) }

    suspend fun listPaymentMethods(): Result<List<MetodoPagoDto>> =
        safeCall { api.listPaymentMethods() }

    /** Descarga carrito servidor → cache local (mismo usuario desde web u otro dispositivo). */
    suspend fun refreshCartFromServer(session: SessionManager) {
        if (!session.isLoggedIn()) return
        safeCall { api.getCart() }.onSuccess { list ->
            session.saveCart(list.map { it.toCarritoItem() })
        }
    }

    suspend fun cartAdd(productId: Int, quantity: Int = 1): Result<List<CartLineDto>> =
        safeCall { api.cartAdd(CartAddBody(productId, quantity)) }

    suspend fun cartDecrement(productId: Int): Result<List<CartLineDto>> =
        safeCall { api.cartDecrement(CartProductIdBody(productId)) }

    suspend fun cartRemoveLine(productId: Int): Result<List<CartLineDto>> =
        safeCall { api.cartRemoveLine(productId) }

    suspend fun checkout(request: CheckoutRequest): Result<CheckoutResponse> =
        safeCall { api.checkout(request) }
}
