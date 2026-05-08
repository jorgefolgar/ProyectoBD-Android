package com.umg.muebleria.data.repository

import com.umg.muebleria.data.model.*
import com.umg.muebleria.data.remote.ApiClient
import com.umg.muebleria.data.remote.MuebleriaApi
import retrofit2.Response

/**
 * Capa de repositorio: la UI/ViewModel llama aquí; aquí se orquestan llamadas a la API.
 * No debe contener SQL ni credenciales de Oracle (eso es responsabilidad del servidor).
 */
class MuebleriaRepository(
    private val api: MuebleriaApi = ApiClient.api
) {
    private suspend fun <T> safeCall(call: suspend () -> Response<T>): Result<T> = runCatching {
        val response = call()
        if (response.isSuccessful) {
            response.body() ?: error("Respuesta vacía del servidor")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
            error(errorBody)
        }
    }

    // ────── Auth ──────
    suspend fun login(login: String, password: String): Result<LoginResponse> =
        safeCall { api.login(LoginRequest(login, password)) }

    suspend fun register(request: RegisterRequest): Result<ApiMessage> =
        safeCall { api.register(request) }

    // ────── Account ──────
    suspend fun getProfile(): Result<PerfilDto> =
        safeCall { api.getProfile() }

    suspend fun updateProfile(request: ProfileUpdateRequest): Result<ApiMessage> =
        safeCall { api.updateProfile(request) }

    suspend fun changePassword(request: ChangePasswordRequest): Result<ApiMessage> =
        safeCall { api.changePassword(request) }

    suspend fun listProfessions(): Result<List<ProfesionDto>> =
        safeCall { api.listProfessions() }

    // ────── Catálogo ──────
    suspend fun listCatalog(
        reference: String? = null, name: String? = null,
        typeFilter: String? = null, material: String? = null
    ): Result<List<ProductoDto>> =
        safeCall { api.listCatalog(reference, name, typeFilter, material) }

    suspend fun getCatalogDetail(id: Int): Result<ProductoDetalleDto> =
        safeCall { api.getCatalogDetail(id) }

    // ────── Checkout ──────
    suspend fun listPaymentMethods(): Result<List<MetodoPagoDto>> =
        safeCall { api.listPaymentMethods() }

    suspend fun checkout(request: CheckoutRequest): Result<CheckoutResponse> =
        safeCall { api.checkout(request) }

    // ────── Admin: Clientes ──────
    suspend fun listClients(document: String? = null, name: String? = null, email: String? = null): Result<List<ClienteDto>> =
        safeCall { api.listClients(document, name, email) }

    suspend fun getClient(id: Int): Result<ClienteDto> =
        safeCall { api.getClient(id) }

    suspend fun createClient(client: ClienteDto): Result<ApiMessage> =
        safeCall { api.createClient(client) }

    suspend fun updateClient(id: Int, client: ClienteDto): Result<ApiMessage> =
        safeCall { api.updateClient(id, client) }

    suspend fun deleteClient(id: Int): Result<ApiMessage> =
        safeCall { api.deleteClient(id) }

    // ────── Admin: Productos ──────
    suspend fun listProductsAdmin(reference: String? = null, name: String? = null, typeFilter: String? = null): Result<List<ProductoAdminDto>> =
        safeCall { api.listProductsAdmin(reference, name, typeFilter) }

    suspend fun getProductAdmin(id: Int): Result<ProductoAdminDto> =
        safeCall { api.getProductAdmin(id) }

    suspend fun createProduct(product: ProductoAdminDto): Result<ApiMessage> =
        safeCall { api.createProduct(product) }

    suspend fun updateProduct(id: Int, product: ProductoAdminDto): Result<ApiMessage> =
        safeCall { api.updateProduct(id, product) }

    suspend fun deleteProduct(id: Int): Result<ApiMessage> =
        safeCall { api.deleteProduct(id) }

    // ────── Admin: Precios ──────
    suspend fun listPrices(productId: Int? = null): Result<List<PrecioDto>> =
        safeCall { api.listPrices(productId) }

    suspend fun getPrice(id: Int): Result<PrecioDto> =
        safeCall { api.getPrice(id) }

    suspend fun createPrice(price: PrecioDto): Result<ApiMessage> =
        safeCall { api.createPrice(price) }

    suspend fun updatePrice(id: Int, price: PrecioDto): Result<ApiMessage> =
        safeCall { api.updatePrice(id, price) }

    suspend fun deletePrice(id: Int): Result<ApiMessage> =
        safeCall { api.deletePrice(id) }

    // ────── Admin: Reportes ──────
    suspend fun reportSalesByType(startDate: String? = null, endDate: String? = null, city: String? = null): Result<ReporteResponse> =
        safeCall { api.reportSalesByType(startDate, endDate, city) }

    suspend fun reportTopProduct(startDate: String? = null, endDate: String? = null, city: String? = null, furnitureType: String? = null): Result<ReporteResponse> =
        safeCall { api.reportTopProduct(startDate, endDate, city, furnitureType) }

    suspend fun reportPurchasesByClient(userId: Int): Result<ReporteResponse> =
        safeCall { api.reportPurchasesByClient(userId) }

    suspend fun reportCashClosures(): Result<ReporteResponse> =
        safeCall { api.reportCashClosures() }

    suspend fun reportMarketingLtv(startDate: String? = null, endDate: String? = null): Result<ReporteResponse> =
        safeCall { api.reportMarketingLtv(startDate, endDate) }

    suspend fun reportMarketingBase(startDate: String? = null, endDate: String? = null): Result<ReporteResponse> =
        safeCall { api.reportMarketingBase(startDate, endDate) }

    suspend fun reportMarketingActivity(startDate: String? = null, endDate: String? = null): Result<ReporteResponse> =
        safeCall { api.reportMarketingActivity(startDate, endDate) }

    suspend fun reportMarketingRetention(startDate: String? = null, endDate: String? = null): Result<ReporteResponse> =
        safeCall { api.reportMarketingRetention(startDate, endDate) }

    suspend fun reportMarketingCohort(startDate: String? = null, endDate: String? = null): Result<ReporteResponse> =
        safeCall { api.reportMarketingCohort(startDate, endDate) }

    suspend fun reportMarketingRemarketing(startDate: String? = null, endDate: String? = null): Result<ReporteResponse> =
        safeCall { api.reportMarketingRemarketing(startDate, endDate) }
}
