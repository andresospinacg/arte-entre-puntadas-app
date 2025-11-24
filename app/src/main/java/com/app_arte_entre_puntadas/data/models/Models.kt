package com.app_arte_entre_puntadas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de perfil de usuario
 */
@Serializable
data class Profile(
    @SerialName("id")
    val id: String,
    
    @SerialName("full_name")
    val fullName: String,
    
    @SerialName("phone")
    val phone: String? = null,
    
    @SerialName("role")
    val role: String = "client", // client o admin
    
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun isAdmin(): Boolean = role == "admin"
    fun isClient(): Boolean = role == "client"
}

/**
 * Modelo de categoría de productos
 */
@Serializable
data class Category(
    @SerialName("id")
    val id: Int? = null,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Modelo de producto
 */
@Serializable
data class Product(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("category_id")
    val categoryId: Int? = null,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("price")
    val price: Double,
    
    @SerialName("stock")
    val stock: Int = 0,
    
    @SerialName("image_url")
    val imageUrl: String? = null,
    
    @SerialName("is_available")
    val isAvailable: Boolean = true,
    
    @SerialName("created_by")
    val createdBy: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun isInStock(): Boolean = stock > 0
    fun getFormattedPrice(): String = "$${String.format("%,.0f", price)}"
}

/**
 * Modelo de producto con categoría (vista)
 */
@Serializable
data class ProductWithCategory(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("price")
    val price: Double,
    
    @SerialName("stock")
    val stock: Int,
    
    @SerialName("image_url")
    val imageUrl: String? = null,
    
    @SerialName("is_available")
    val isAvailable: Boolean,
    
    @SerialName("category_id")
    val categoryId: Int? = null,
    
    @SerialName("category_name")
    val categoryName: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun getFormattedPrice(): String = "$${String.format("%,.0f", price)}"
}

/**
 * Modelo de imagen de producto
 */
@Serializable
data class ProductImage(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("product_id")
    val productId: String,
    
    @SerialName("image_url")
    val imageUrl: String,
    
    @SerialName("is_primary")
    val isPrimary: Boolean = false,
    
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Modelo de item del carrito
 */
@Serializable
data class CartItem(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("product_id")
    val productId: String,
    
    @SerialName("quantity")
    val quantity: Int = 1,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Modelo de item del carrito con detalles (vista)
 */
@Serializable
data class CartItemDetail(
    @SerialName("id")
    val id: String,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("product_id")
    val productId: String,
    
    @SerialName("quantity")
    val quantity: Int,
    
    @SerialName("product_name")
    val productName: String,
    
    @SerialName("price")
    val price: Double,
    
    @SerialName("image_url")
    val imageUrl: String? = null,
    
    @SerialName("subtotal")
    val subtotal: Double,
    
    @SerialName("stock")
    val stock: Int,
    
    @SerialName("is_available")
    val isAvailable: Boolean,
    
    @SerialName("created_at")
    val createdAt: String? = null
) {
    fun getFormattedSubtotal(): String = "$${String.format("%,.0f", subtotal)}"
}

/**
 * Estado del pedido
 */
enum class OrderStatus {
    @SerialName("pending")
    PENDING,
    
    @SerialName("confirmed")
    CONFIRMED,
    
    @SerialName("processing")
    PROCESSING,
    
    @SerialName("shipped")
    SHIPPED,
    
    @SerialName("delivered")
    DELIVERED,
    
    @SerialName("cancelled")
    CANCELLED;
    
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "Pendiente"
            CONFIRMED -> "Confirmado"
            PROCESSING -> "En Proceso"
            SHIPPED -> "Enviado"
            DELIVERED -> "Entregado"
            CANCELLED -> "Cancelado"
        }
    }
}

/**
 * Modelo de pedido
 */
@Serializable
data class Order(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("total")
    val total: Double,
    
    @SerialName("status")
    val status: String = "pending",
    
    @SerialName("shipping_address")
    val shippingAddress: String,
    
    @SerialName("notes")
    val notes: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun getFormattedTotal(): String = "$${String.format("%,.0f", total)}"
    
    fun getStatusDisplay(): String {
        return when (status) {
            "pending" -> "Pendiente"
            "confirmed" -> "Confirmado"
            "processing" -> "En Proceso"
            "shipped" -> "Enviado"
            "delivered" -> "Entregado"
            "cancelled" -> "Cancelado"
            else -> "Desconocido"
        }
    }
}

/**
 * Modelo de item de pedido
 */
@Serializable
data class OrderItem(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("order_id")
    val orderId: String,
    
    @SerialName("product_id")
    val productId: String,
    
    @SerialName("quantity")
    val quantity: Int,
    
    @SerialName("unit_price")
    val unitPrice: Double,
    
    @SerialName("subtotal")
    val subtotal: Double,
    
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * DTO para crear un nuevo producto
 */
@Serializable
data class CreateProductDTO(
    @SerialName("category_id")
    val categoryId: Int?,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String?,
    
    @SerialName("price")
    val price: Double,
    
    @SerialName("stock")
    val stock: Int,
    
    @SerialName("image_url")
    val imageUrl: String?,
    
    @SerialName("is_available")
    val isAvailable: Boolean = true
)

/**
 * DTO para actualizar un producto
 */
@Serializable
data class UpdateProductDTO(
    @SerialName("category_id")
    val categoryId: Int? = null,
    
    @SerialName("name")
    val name: String? = null,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("price")
    val price: Double? = null,
    
    @SerialName("stock")
    val stock: Int? = null,
    
    @SerialName("image_url")
    val imageUrl: String? = null,
    
    @SerialName("is_available")
    val isAvailable: Boolean? = null
)

/**
 * DTO para registro de usuario
 */
data class RegisterDTO(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String?
)

/**
 * DTO para login
 */
data class LoginDTO(
    val email: String,
    val password: String
)

/**
 * DTO para agregar item al carrito
 */
@Serializable
data class AddToCartDTO(
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("product_id")
    val productId: String,
    
    @SerialName("quantity")
    val quantity: Int = 1
)

/**
 * DTO para actualizar cantidad en carrito
 */
@Serializable
data class UpdateCartItemDTO(
    @SerialName("quantity")
    val quantity: Int
)

/**
 * DTO para crear orden
 */
@Serializable
data class CreateOrderDTO(
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("total")
    val total: Double,
    
    @SerialName("shipping_address")
    val shippingAddress: String,
    
    @SerialName("notes")
    val notes: String? = null
)

/**
 * DTO para crear item de orden
 */
@Serializable
data class CreateOrderItemDTO(
    @SerialName("order_id")
    val orderId: String,
    
    @SerialName("product_id")
    val productId: String,
    
    @SerialName("quantity")
    val quantity: Int,
    
    @SerialName("unit_price")
    val unitPrice: Double,
    
    @SerialName("subtotal")
    val subtotal: Double
)

/**
 * Modelo extendido de usuario con geolocalización y datos de tienda
 */
@Serializable
data class User(
    @SerialName("id")
    val id: String,
    
    @SerialName("full_name")
    val fullName: String,
    
    @SerialName("phone")
    val phone: String? = null,
    
    @SerialName("role")
    val role: String = "client", // client o admin
    
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    
    // Datos de geolocalización personal
    @SerialName("latitude")
    val latitude: Double? = null,
    
    @SerialName("longitude")
    val longitude: Double? = null,
    
    @SerialName("address")
    val address: String? = null,
    
    @SerialName("city")
    val city: String? = null,
    
    @SerialName("country")
    val country: String? = null,
    
    // Datos de tienda física (para artesanos)
    @SerialName("store_name")
    val storeName: String? = null,
    
    @SerialName("store_description")
    val storeDescription: String? = null,
    
    @SerialName("store_latitude")
    val storeLatitude: Double? = null,
    
    @SerialName("store_longitude")
    val storeLongitude: Double? = null,
    
    @SerialName("store_address")
    val storeAddress: String? = null,
    
    @SerialName("store_phone")
    val storePhone: String? = null,
    
    @SerialName("store_image_url")
    val storeImageUrl: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun isAdmin(): Boolean = role == "admin"
    fun isClient(): Boolean = role == "client"
    fun hasLocation(): Boolean = latitude != null && longitude != null
    fun hasStore(): Boolean = !storeName.isNullOrEmpty()
    fun hasStoreLocation(): Boolean = storeLatitude != null && storeLongitude != null
    
    // Email se obtiene de auth.users, no de profiles
    fun getEmail(): String = "" // Se debe obtener del contexto de autenticación
}

/**
 * DTO para crear usuario con geolocalización
 */
@Serializable
data class CreateUserDTO(
    @SerialName("full_name")
    val fullName: String,
    
    @SerialName("phone")
    val phone: String? = null,
    
    @SerialName("role")
    val role: String = "client",
    
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerialName("latitude")
    val latitude: Double? = null,
    
    @SerialName("longitude")
    val longitude: Double? = null,
    
    @SerialName("address")
    val address: String? = null,
    
    @SerialName("city")
    val city: String? = null,
    
    @SerialName("country")
    val country: String? = null,
    
    @SerialName("store_name")
    val storeName: String? = null,
    
    @SerialName("store_description")
    val storeDescription: String? = null,
    
    @SerialName("store_latitude")
    val storeLatitude: Double? = null,
    
    @SerialName("store_longitude")
    val storeLongitude: Double? = null,
    
    @SerialName("store_address")
    val storeAddress: String? = null,
    
    @SerialName("store_phone")
    val storePhone: String? = null,
    
    @SerialName("store_image_url")
    val storeImageUrl: String? = null,
    
    // Campos para crear usuario en auth (no se envían a profiles)
    @kotlinx.serialization.Transient
    val email: String? = null,
    
    @kotlinx.serialization.Transient
    val password: String? = null
)

/**
 * DTO para actualizar usuario
 */
@Serializable
data class UpdateUserDTO(
    @SerialName("full_name")
    val fullName: String? = null,
    
    @SerialName("phone")
    val phone: String? = null,
    
    @SerialName("role")
    val role: String? = null,
    
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerialName("latitude")
    val latitude: Double? = null,
    
    @SerialName("longitude")
    val longitude: Double? = null,
    
    @SerialName("address")
    val address: String? = null,
    
    @SerialName("city")
    val city: String? = null,
    
    @SerialName("country")
    val country: String? = null,
    
    @SerialName("store_name")
    val storeName: String? = null,
    
    @SerialName("store_description")
    val storeDescription: String? = null,
    
    @SerialName("store_latitude")
    val storeLatitude: Double? = null,
    
    @SerialName("store_longitude")
    val storeLongitude: Double? = null,
    
    @SerialName("store_address")
    val storeAddress: String? = null,
    
    @SerialName("store_phone")
    val storePhone: String? = null,
    
    @SerialName("store_image_url")
    val storeImageUrl: String? = null
)

/**
 * Clase de datos para ubicación
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null
)
