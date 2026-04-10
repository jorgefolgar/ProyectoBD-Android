package com.umg.muebleria.data.repository

import com.umg.muebleria.data.model.ProductoDto
import com.umg.muebleria.data.remote.ApiClient

/**
 * Capa de repositorio: la UI/ViewModel llama aquí; aquí se orquestan llamadas a la API.
 * No debe contener SQL ni credenciales de Oracle (eso es responsabilidad del servidor).
 */
class MuebleriaRepository(
    private val api = ApiClient.api
) {

    suspend fun obtenerProductos(): Result<List<ProductoDto>> = runCatching {
        val response = api.listarProductos()
        if (response.isSuccessful) {
            response.body().orEmpty()
        } else {
            error("Error HTTP ${response.code()}")
        }
    }
}
