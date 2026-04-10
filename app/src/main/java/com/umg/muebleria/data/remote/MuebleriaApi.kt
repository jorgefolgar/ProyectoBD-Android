package com.umg.muebleria.data.remote

import com.umg.muebleria.data.model.ProductoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Contrato HTTP con el backend. Los paths reales deben coincidir con la API que expongáis
 * (ASP.NET Web API u otro), que a su vez ejecuta los procedimientos en Oracle.
 *
 * Ejemplos alineados con el enunciado (catálogo, carrito, orden): definir cuando exista el backend.
 */
interface MuebleriaApi {

    @GET("productos")
    suspend fun listarProductos(): Response<List<ProductoDto>>

    @GET("productos/{id}")
    suspend fun obtenerProducto(@Path("id") id: String): Response<ProductoDto>
}
