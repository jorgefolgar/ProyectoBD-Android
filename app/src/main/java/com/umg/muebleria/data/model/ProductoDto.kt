package com.umg.muebleria.data.model

/**
 * DTO de ejemplo para el catálogo de muebles (referencia, nombre, tipo, etc.).
 * Ajustar campos al JSON real que devuelva la API.
 */
data class ProductoDto(
    val referencia: String? = null,
    val nombre: String? = null,
    val tipo: String? = null,
    val material: String? = null,
    val precio: Double? = null,
    val cantidadDisponible: Int? = null,
    val fotoUrl: String? = null
)
