package com.umg.muebleria.data.remote

import com.umg.muebleria.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Contrato HTTP para la app cliente con el backend VB.NET.
 * Las operaciones de administración no se exponen en esta app.
 */
interface MuebleriaApi {

    @POST("api/AuthApi/Login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

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

    @GET("api/catalog")
    suspend fun listCatalog(
        @Query("reference") reference: String? = null,
        @Query("name") name: String? = null,
        @Query("typeFilter") typeFilter: String? = null,
        @Query("material") material: String? = null
    ): Response<List<ProductoDto>>

    @GET("api/catalog/{id}")
    suspend fun getCatalogDetail(@Path("id") id: Int): Response<ProductoDetalleDto>

    @GET("api/cart/payment-methods")
    suspend fun listPaymentMethods(): Response<List<MetodoPagoDto>>

    /** Carrito persistente del usuario autenticado (Oracle). */
    @GET("api/cart")
    suspend fun getCart(): Response<List<CartLineDto>>

    @POST("api/cart/add")
    suspend fun cartAdd(@Body body: CartAddBody): Response<List<CartLineDto>>

    @POST("api/cart/decrement")
    suspend fun cartDecrement(@Body body: CartProductIdBody): Response<List<CartLineDto>>

    @DELETE("api/cart/line/{productId}")
    suspend fun cartRemoveLine(@Path("productId") productId: Int): Response<List<CartLineDto>>

    @POST("api/cart/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Response<CheckoutResponse>
}
