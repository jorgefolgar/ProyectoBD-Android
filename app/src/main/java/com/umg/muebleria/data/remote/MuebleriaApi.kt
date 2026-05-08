package com.umg.muebleria.data.remote

import com.umg.muebleria.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Contrato HTTP completo con el backend VB.NET.
 * Cada endpoint delega a stored procedures Oracle vía los Services del backend.
 * Android NUNCA accede directamente a Oracle.
 */
interface MuebleriaApi {

    // ────── Auth ──────
    @POST("api/AuthApi/Login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ────── Account ──────
    @POST("api/account/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiMessage>

    @GET("api/account/profile")
    suspend fun getProfile(): Response<PerfilDto>

    @PUT("api/account/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<ApiMessage>

    @POST("api/account/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiMessage>

    @GET("api/account/professions")
    suspend fun listProfessions(): Response<List<ProfesionDto>>

    // ────── Catálogo ──────
    @GET("api/catalog")
    suspend fun listCatalog(
        @Query("reference") reference: String? = null,
        @Query("name") name: String? = null,
        @Query("typeFilter") typeFilter: String? = null,
        @Query("material") material: String? = null
    ): Response<List<ProductoDto>>

    @GET("api/catalog/{id}")
    suspend fun getCatalogDetail(@Path("id") id: Int): Response<ProductoDetalleDto>

    // Imagen: se carga directamente con Glide usando la URL api/catalog/{id}/image

    // ────── Carrito / Checkout ──────
    @GET("api/cart/payment-methods")
    suspend fun listPaymentMethods(): Response<List<MetodoPagoDto>>

    @POST("api/cart/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Response<CheckoutResponse>

    // ────── Admin: Clientes ──────
    @GET("api/clients")
    suspend fun listClients(
        @Query("document") document: String? = null,
        @Query("name") name: String? = null,
        @Query("email") email: String? = null
    ): Response<List<ClienteDto>>

    @GET("api/clients/{id}")
    suspend fun getClient(@Path("id") id: Int): Response<ClienteDto>

    @POST("api/clients")
    suspend fun createClient(@Body client: ClienteDto): Response<ApiMessage>

    @PUT("api/clients/{id}")
    suspend fun updateClient(@Path("id") id: Int, @Body client: ClienteDto): Response<ApiMessage>

    @DELETE("api/clients/{id}")
    suspend fun deleteClient(@Path("id") id: Int): Response<ApiMessage>

    // ────── Admin: Productos ──────
    @GET("api/products-admin")
    suspend fun listProductsAdmin(
        @Query("reference") reference: String? = null,
        @Query("name") name: String? = null,
        @Query("typeFilter") typeFilter: String? = null
    ): Response<List<ProductoAdminDto>>

    @GET("api/products-admin/{id}")
    suspend fun getProductAdmin(@Path("id") id: Int): Response<ProductoAdminDto>

    @POST("api/products-admin")
    suspend fun createProduct(@Body product: ProductoAdminDto): Response<ApiMessage>

    @PUT("api/products-admin/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: ProductoAdminDto): Response<ApiMessage>

    @DELETE("api/products-admin/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<ApiMessage>

    // ────── Admin: Precios ──────
    @GET("api/prices")
    suspend fun listPrices(@Query("productId") productId: Int? = null): Response<List<PrecioDto>>

    @GET("api/prices/{id}")
    suspend fun getPrice(@Path("id") id: Int): Response<PrecioDto>

    @POST("api/prices")
    suspend fun createPrice(@Body price: PrecioDto): Response<ApiMessage>

    @PUT("api/prices/{id}")
    suspend fun updatePrice(@Path("id") id: Int, @Body price: PrecioDto): Response<ApiMessage>

    @DELETE("api/prices/{id}")
    suspend fun deletePrice(@Path("id") id: Int): Response<ApiMessage>

    // ────── Admin: Reportes ──────
    @GET("api/reports/sales-by-type")
    suspend fun reportSalesByType(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("city") city: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/top-product")
    suspend fun reportTopProduct(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("city") city: String? = null,
        @Query("furnitureType") furnitureType: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/purchases-by-client")
    suspend fun reportPurchasesByClient(@Query("userId") userId: Int): Response<ReporteResponse>

    @GET("api/reports/cash-closures")
    suspend fun reportCashClosures(): Response<ReporteResponse>

    @GET("api/reports/marketing-ltv")
    suspend fun reportMarketingLtv(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/marketing-base")
    suspend fun reportMarketingBase(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/marketing-activity")
    suspend fun reportMarketingActivity(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/marketing-retention")
    suspend fun reportMarketingRetention(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/marketing-cohort")
    suspend fun reportMarketingCohort(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReporteResponse>

    @GET("api/reports/marketing-remarketing")
    suspend fun reportMarketingRemarketing(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReporteResponse>
}
