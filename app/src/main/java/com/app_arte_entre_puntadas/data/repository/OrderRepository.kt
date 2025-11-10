package com.app_arte_entre_puntadas.data.repository

import com.app_arte_entre_puntadas.data.models.CreateOrderDTO
import com.app_arte_entre_puntadas.data.models.CreateOrderItemDTO
import com.app_arte_entre_puntadas.data.models.Order
import com.app_arte_entre_puntadas.data.models.OrderItem
import com.app_arte_entre_puntadas.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Repositorio para gestionar órdenes de compra
 */
class OrderRepository {
    
    private val supabase = SupabaseClient.getClient()
    
    /**
     * Crea una nueva orden con sus items
     */
    suspend fun createOrder(orderDTO: CreateOrderDTO, items: List<CreateOrderItemDTO>): Result<Order> {
        return try {
            // 1. Crear la orden
            val order = supabase.postgrest["orders"]
                .insert(orderDTO) {
                    select()
                }
                .decodeSingle<Order>()
            
            // 2. Crear los items de la orden
            val orderItems = items.map { item ->
                item.copy(orderId = order.id ?: "")
            }
            
            supabase.postgrest["order_items"]
                .insert(orderItems)
            
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todas las órdenes de un usuario
     */
    suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val orders = supabase.postgrest["orders"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<Order>()
            
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los detalles de una orden específica
     */
    suspend fun getOrderDetails(orderId: String): Result<Order> {
        return try {
            val order = supabase.postgrest["orders"]
                .select {
                    filter {
                        eq("id", orderId)
                    }
                }
                .decodeSingle<Order>()
            
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los items de una orden
     */
    suspend fun getOrderItems(orderId: String): Result<List<OrderItem>> {
        return try {
            val items = supabase.postgrest["order_items"]
                .select(
                    columns = Columns.raw("""
                        *,
                        products:product_id (
                            id,
                            name,
                            image_url
                        )
                    """.trimIndent())
                ) {
                    filter {
                        eq("order_id", orderId)
                    }
                }
                .decodeList<OrderItem>()
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza el estado de una orden
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            supabase.postgrest["orders"]
                .update(mapOf("status" to newStatus)) {
                    filter {
                        eq("id", orderId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancela una orden (solo si está en estado 'pending')
     */
    suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            supabase.postgrest["orders"]
                .update(mapOf("status" to "cancelled")) {
                    filter {
                        eq("id", orderId)
                        eq("status", "pending")
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
