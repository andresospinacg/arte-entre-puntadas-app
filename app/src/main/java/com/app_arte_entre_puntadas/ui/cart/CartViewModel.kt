package com.app_arte_entre_puntadas.ui.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app_arte_entre_puntadas.data.local.SessionManager
import com.app_arte_entre_puntadas.data.models.AddToCartDTO
import com.app_arte_entre_puntadas.data.models.CartItemDetail
import com.app_arte_entre_puntadas.data.repository.CartRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para el carrito de compras
 * Maneja el estado del carrito y las operaciones CRUD
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = CartRepository()
    private val sessionManager = SessionManager(application)
    
    // LiveData para los items del carrito
    private val _cartItems = MutableLiveData<List<CartItemDetail>>()
    val cartItems: LiveData<List<CartItemDetail>> = _cartItems
    
    // LiveData para el total del carrito
    private val _cartTotal = MutableLiveData<Double>()
    val cartTotal: LiveData<Double> = _cartTotal
    
    // LiveData para la cantidad de items
    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount
    
    // LiveData para mensajes de error
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    // LiveData para indicar carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData para indicar éxito en operaciones
    private val _operationSuccess = MutableLiveData<String>()
    val operationSuccess: LiveData<String> = _operationSuccess
    
    init {
        loadCartItems()
    }
    
    /**
     * Carga los items del carrito con sus detalles
     */
    fun loadCartItems() {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.getCartItemsWithDetails(userId)
                result.onSuccess { items ->
                    _cartItems.value = items
                    calculateTotal(items)
                    _cartItemCount.value = items.sumOf { it.quantity }
                }.onFailure { e ->
                    _error.value = "Error al cargar el carrito: ${e.message}"
                    _cartItems.value = emptyList()
                    _cartTotal.value = 0.0
                    _cartItemCount.value = 0
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar el carrito: ${e.message}"
                _cartItems.value = emptyList()
                _cartTotal.value = 0.0
                _cartItemCount.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Agrega un producto al carrito
     */
    fun addToCart(productId: String, quantity: Int) {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val dto = AddToCartDTO(
                    userId = userId,
                    productId = productId,
                    quantity = quantity
                )
                repository.addToCart(dto)
                _operationSuccess.value = "Producto agregado al carrito"
                loadCartItems() // Recargar el carrito
            } catch (e: Exception) {
                _error.value = "Error al agregar al carrito: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualiza la cantidad de un item en el carrito
     */
    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItemId)
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateCartItemQuantity(cartItemId, newQuantity)
                loadCartItems() // Recargar el carrito
            } catch (e: Exception) {
                _error.value = "Error al actualizar cantidad: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Elimina un item del carrito
     */
    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.removeFromCart(cartItemId)
                _operationSuccess.value = "Producto eliminado del carrito"
                loadCartItems() // Recargar el carrito
            } catch (e: Exception) {
                _error.value = "Error al eliminar del carrito: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Vacía completamente el carrito
     */
    fun clearCart() {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.clearCart(userId)
                _operationSuccess.value = "Carrito vaciado"
                loadCartItems() // Recargar el carrito
            } catch (e: Exception) {
                _error.value = "Error al vaciar el carrito: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Calcula el total del carrito
     */
    private fun calculateTotal(items: List<CartItemDetail>) {
        val total = items.sumOf { it.subtotal }
        _cartTotal.value = total
    }
    
    /**
     * Obtiene el conteo de items en el carrito
     */
    fun refreshCartCount() {
        val userId = sessionManager.getUserId()
        if (userId == null) return
        
        viewModelScope.launch {
            try {
                val result = repository.getCartItemCount(userId)
                result.onSuccess { count ->
                    _cartItemCount.value = count
                }
            } catch (e: Exception) {
                // Silenciosamente fallar, no es crítico
            }
        }
    }
}
