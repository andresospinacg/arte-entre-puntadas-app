package com.app_arte_entre_puntadas.data.repository

import com.app_arte_entre_puntadas.data.models.AddToCartDTO
import com.app_arte_entre_puntadas.data.models.CartItem
import com.app_arte_entre_puntadas.data.models.CartItemDetail
import com.app_arte_entre_puntadas.data.models.UpdateCartItemDTO
import com.app_arte_entre_puntadas.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para operaciones del carrito de compras
 */
class CartRepository {
    
    private val client = SupabaseClient.getClient()
    
    /**
     * Obtiene los items del carrito de un usuario
     */
    suspend fun getCartItems(userId: String): Result<List<CartItem>> = withContext(Dispatchers.IO) {
        try {
            val items = client.from("cart_items")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<CartItem>()
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener items del carrito: ${e.message}"))
        }
    }
    
    /**
     * Obtiene los items del carrito con detalles del producto
     */
    suspend fun getCartItemsWithDetails(userId: String): Result<List<CartItemDetail>> = withContext(Dispatchers.IO) {
        try {
            // Primero obtenemos los items del carrito
            val cartItems = client.from("cart_items")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<CartItem>()
            
            // Si no hay items, retornar lista vacía
            if (cartItems.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            
            // Obtener los IDs únicos de productos
            val productIds = cartItems.map { it.productId }.distinct()
            
            // Obtener los detalles de los productos
            val products = client.from("products")
                .select {
                    filter {
                        isIn("id", productIds)
                    }
                }.decodeList<com.app_arte_entre_puntadas.data.models.Product>()
            
            // Crear un mapa de productos por ID para acceso rápido
            val productMap = products.associateBy { it.id ?: "" }
            
            // Combinar los items del carrito con los detalles del producto
            val cartItemDetails = cartItems.mapNotNull { cartItem ->
                val product = productMap[cartItem.productId]
                product?.let {
                    CartItemDetail(
                        id = cartItem.id ?: "",
                        userId = cartItem.userId,
                        productId = cartItem.productId,
                        quantity = cartItem.quantity,
                        productName = product.name,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        subtotal = product.price * cartItem.quantity,
                        stock = product.stock,
                        isAvailable = product.isAvailable,
                        createdAt = cartItem.createdAt
                    )
                }
            }
            
            Result.success(cartItemDetails)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener detalles del carrito: ${e.message}"))
        }
    }
    
    /**
     * Agrega un producto al carrito
     */
    suspend fun addToCart(addToCartDTO: AddToCartDTO): Result<CartItem> = withContext(Dispatchers.IO) {
        try {
            val item = client.from("cart_items")
                .insert(addToCartDTO) {
                    select()
                }.decodeSingle<CartItem>()
            
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(Exception("Error al agregar al carrito: ${e.message}"))
        }
    }
    
    /**
     * Actualiza la cantidad de un item en el carrito
     */
    suspend fun updateCartItemQuantity(
        cartItemId: String,
        quantity: Int
    ): Result<CartItem> = withContext(Dispatchers.IO) {
        try {
            val item = client.from("cart_items")
                .update(UpdateCartItemDTO(quantity)) {
                    filter {
                        eq("id", cartItemId)
                    }
                    select()
                }.decodeSingle<CartItem>()
            
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar cantidad: ${e.message}"))
        }
    }
    
    /**
     * Elimina un item del carrito
     */
    suspend fun removeFromCart(cartItemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("cart_items")
                .delete {
                    filter {
                        eq("id", cartItemId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar del carrito: ${e.message}"))
        }
    }
    
    /**
     * Limpia todo el carrito de un usuario
     */
    suspend fun clearCart(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("cart_items")
                .delete {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al limpiar carrito: ${e.message}"))
        }
    }
    
    /**
     * Cuenta el número de items en el carrito
     */
    suspend fun getCartItemCount(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val items = getCartItems(userId).getOrThrow()
            Result.success(items.sumOf { it.quantity })
        } catch (e: Exception) {
            Result.failure(Exception("Error al contar items: ${e.message}"))
        }
    }
}
