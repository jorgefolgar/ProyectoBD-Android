package com.umg.muebleria.data.model

import com.google.gson.annotations.SerializedName

/** Respuesta del login con token de autenticación */
data class LoginResponse(
    val userId: Int = 0,
    val login: String = "",
    val roleId: Int = 0,
    val userType: String = "",
    val email: String = "",
    val fullName: String = "",
    val token: String = ""
)

/** Request de login */
data class LoginRequest(
    val login: String,
    val password: String
)

/** Request de registro de cliente */
data class RegisterRequest(
    val login: String = "",
    val password: String = "",
    val documentTypeId: Int = 1,
    val documentNumber: Long? = null,
    val firstName: String = "",
    val middleName: String? = null,
    val lastName: String = "",
    val secondLastName: String? = null,
    val homePhone: Long? = null,
    val mobilePhone: Long? = null,
    val municipality: String = "",
    val department: String = "",
    val city: String = "",
    val country: String = "Guatemala",
    val email: String = "",
    @SerializedName("nit") val nit: String? = null,
    val professionId: Int? = null
)

/** Producto del catálogo (resumen) */
data class ProductoDto(
    @SerializedName(value = "productId", alternate = ["ProductId"]) val productId: Int = 0,
    @SerializedName(value = "reference", alternate = ["Reference"]) val reference: String? = null,
    @SerializedName(value = "name", alternate = ["Name"]) val name: String? = null,
    @SerializedName(value = "type", alternate = ["Type"]) val type: String? = null,
    @SerializedName(value = "material", alternate = ["Material"]) val material: String? = null,
    @SerializedName(value = "unitPrice", alternate = ["UnitPrice"]) val unitPrice: Double = 0.0,
    @SerializedName(value = "stock", alternate = ["Stock"]) val stock: Int = 0,
    @SerializedName(value = "color", alternate = ["Color"]) val color: String? = null,
    @SerializedName(value = "hasPhoto", alternate = ["HasPhoto"]) val hasPhoto: Boolean = false
)

/** Producto detalle completo */
data class ProductoDetalleDto(
    @SerializedName(value = "productId", alternate = ["ProductId"]) val productId: Int = 0,
    @SerializedName(value = "reference", alternate = ["Reference"]) val reference: String? = null,
    @SerializedName(value = "name", alternate = ["Name"]) val name: String? = null,
    @SerializedName(value = "type", alternate = ["Type"]) val type: String? = null,
    @SerializedName(value = "material", alternate = ["Material"]) val material: String? = null,
    @SerializedName(value = "description", alternate = ["Description"]) val description: String? = null,
    @SerializedName(value = "unitPrice", alternate = ["UnitPrice"]) val unitPrice: Double = 0.0,
    @SerializedName(value = "stock", alternate = ["Stock"]) val stock: Int = 0,
    @SerializedName(value = "color", alternate = ["Color"]) val color: String? = null,
    @SerializedName(value = "heightCm", alternate = ["HeightCm"]) val heightCm: Double? = null,
    @SerializedName(value = "widthCm", alternate = ["WidthCm"]) val widthCm: Double? = null,
    @SerializedName(value = "depthCm", alternate = ["DepthCm"]) val depthCm: Double? = null,
    @SerializedName(value = "weightGrams", alternate = ["WeightGrams"]) val weightGrams: Double? = null,
    @SerializedName(value = "hasPhoto", alternate = ["HasPhoto"]) val hasPhoto: Boolean = false,
    @SerializedName(value = "hasCurrentPrice", alternate = ["HasCurrentPrice"]) val hasCurrentPrice: Boolean = false,
    @SerializedName(value = "isAvailable", alternate = ["IsAvailable"]) val isAvailable: Boolean = false
)

/** Item del carrito (client-side cache; la fuente de verdad usa la API cuando hay sesión). */
data class CarritoItem(
    val productId: Int,
    val reference: String,
    val name: String,
    var unitPrice: Double,
    var quantity: Int,
    var lineTotal: Double = 0.0
) {
    fun recalculate() { lineTotal = unitPrice * quantity }
}

/** Respuesta GET/POST cart del servidor (camelCase/PascalCase). */
data class CartLineDto(
    @SerializedName(value = "productId", alternate = ["ProductId"]) val productId: Int = 0,
    @SerializedName(value = "reference", alternate = ["Reference"]) val reference: String? = null,
    @SerializedName(value = "name", alternate = ["Name"]) val name: String? = null,
    @SerializedName(value = "unitPrice", alternate = ["UnitPrice"]) val unitPrice: Double = 0.0,
    @SerializedName(value = "quantity", alternate = ["Quantity"]) val quantity: Int = 0,
    @SerializedName(value = "lineTotal", alternate = ["LineTotal"]) val lineTotal: Double? = null
)

fun CartLineDto.toCarritoItem(): CarritoItem {
    val item = CarritoItem(
        productId = productId,
        reference = reference.orEmpty(),
        name = name.orEmpty(),
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal ?: unitPrice * quantity
    )
    item.recalculate()
    return item
}

/** Body POST api/cart/add (cantidad sumada al total existente). */
data class CartAddBody(
    val productId: Int,
    val quantity: Int = 1
)

data class CartProductIdBody(val productId: Int)

/** Request para checkout */
data class CheckoutRequest(
    val paymentMethodId: Int,
    val cardHolderName: String? = null,
    val cardNumber: String? = null,
    val cardExpiry: String? = null,
    val cardCvv: String? = null,
    val items: List<CheckoutItemRequest>
)

data class CheckoutItemRequest(
    val productId: Int,
    val quantity: Int
)

/** Método de pago */
data class MetodoPagoDto(
    val paymentMethodId: Int = 0,
    val paymentMethodName: String = "",
    val notes: String? = null
)

/** Respuesta de checkout */
data class CheckoutResponse(
    val orderId: Int = 0,
    val total: Double = 0.0,
    /** Gson puede dejar null si el JSON omite el campo o envía null. */
    val message: String? = null
)

/** Perfil de usuario */
data class PerfilDto(
    val userId: Int = 0,
    val roleId: Int = 0,
    val login: String = "",
    val firstName: String = "",
    val middleName: String? = null,
    val lastName: String = "",
    val secondLastName: String? = null,
    val homePhone: Int? = null,
    val mobilePhone: Int? = null,
    val zone: Int? = null,
    val municipality: String? = null,
    val department: String? = null,
    val city: String? = null,
    val country: String? = null,
    val email: String = "",
    val userType: String? = null
)

/** Request de actualización de perfil */
data class ProfileUpdateRequest(
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val secondLastName: String? = null,
    val homePhone: Int? = null,
    val mobilePhone: Int? = null,
    val zone: Int? = null,
    val municipality: String? = null,
    val department: String? = null,
    val city: String? = null,
    val country: String? = null,
    val email: String
)

/** Request de cambio de contraseña */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

/** Profesión para dropdown */
data class ProfesionDto(
    val professionId: Int = 0,
    val name: String = ""
)

/** Respuesta genérica con mensaje */
data class ApiMessage(
    val message: String = "",
    val error: String? = null
)
