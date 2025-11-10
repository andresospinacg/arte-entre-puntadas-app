package com.app_arte_entre_puntadas.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app_arte_entre_puntadas.data.models.Category
import com.app_arte_entre_puntadas.data.models.CreateProductDTO
import com.app_arte_entre_puntadas.data.models.Product
import com.app_arte_entre_puntadas.data.models.ProductWithCategory
import com.app_arte_entre_puntadas.data.models.UpdateProductDTO
import com.app_arte_entre_puntadas.data.repository.ProductRepository
import kotlinx.coroutines.launch

/**
 * Estados posibles de la UI
 */
sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<ProductWithCategory>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
    object Empty : ProductUiState()
}

/**
 * ViewModel para manejo de productos
 */
class ProductViewModel : ViewModel() {
    
    private val repository = ProductRepository()
    
    // Estado de la lista de productos
    private val _uiState = MutableLiveData<ProductUiState>()
    val uiState: LiveData<ProductUiState> = _uiState
    
    // Lista de categorías
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    
    // Mensajes de éxito/error para operaciones individuales
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    
    // Estado de carga para operaciones individuales
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadProducts()
        loadCategories()
    }
    
    /**
     * Carga todos los productos
     */
    fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = ProductUiState.Loading
                
                repository.getProductsWithCategory().fold(
                    onSuccess = { products ->
                        if (products.isEmpty()) {
                            _uiState.value = ProductUiState.Empty
                        } else {
                            _uiState.value = ProductUiState.Success(products)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ProductUiState.Error(error.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error al cargar productos: ${e.message}")
            }
        }
    }
    
    /**
     * Carga todas las categorías
     */
    fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().fold(
                onSuccess = { cats ->
                    _categories.value = cats
                },
                onFailure = { /* Silencioso, no es crítico */ }
            )
        }
    }
    
    /**
     * Busca productos por nombre
     */
    fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadProducts()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            
            repository.searchProducts(query).fold(
                onSuccess = { products ->
                    if (products.isEmpty()) {
                        _uiState.value = ProductUiState.Empty
                    } else {
                        // Convertir a ProductWithCategory (sin categoría)
                        val withCategory = products.map { p ->
                            ProductWithCategory(
                                id = p.id ?: "",
                                name = p.name,
                                description = p.description,
                                price = p.price,
                                stock = p.stock,
                                imageUrl = p.imageUrl,
                                isAvailable = p.isAvailable,
                                categoryId = p.categoryId,
                                categoryName = null,
                                createdAt = p.createdAt,
                                updatedAt = p.updatedAt
                            )
                        }
                        _uiState.value = ProductUiState.Success(withCategory)
                    }
                },
                onFailure = { error ->
                    _uiState.value = ProductUiState.Error(error.message ?: "Error en búsqueda")
                }
            )
        }
    }
    
    /**
     * Filtra productos por categoría
     */
    fun filterByCategory(categoryId: Int?) {
        if (categoryId == null) {
            loadProducts()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            
            repository.getProductsByCategory(categoryId).fold(
                onSuccess = { products ->
                    if (products.isEmpty()) {
                        _uiState.value = ProductUiState.Empty
                    } else {
                        val withCategory = products.map { p ->
                            ProductWithCategory(
                                id = p.id ?: "",
                                name = p.name,
                                description = p.description,
                                price = p.price,
                                stock = p.stock,
                                imageUrl = p.imageUrl,
                                isAvailable = p.isAvailable,
                                categoryId = p.categoryId,
                                categoryName = null,
                                createdAt = p.createdAt,
                                updatedAt = p.updatedAt
                            )
                        }
                        _uiState.value = ProductUiState.Success(withCategory)
                    }
                },
                onFailure = { error ->
                    _uiState.value = ProductUiState.Error(error.message ?: "Error al filtrar")
                }
            )
        }
    }
    
    /**
     * Crea un nuevo producto
     */
    fun createProduct(productDTO: CreateProductDTO, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.createProduct(productDTO).fold(
                onSuccess = {
                    _message.value = "Producto creado exitosamente"
                    _isLoading.value = false
                    loadProducts() // Recargar lista
                    onSuccess()
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Actualiza un producto existente
     */
    fun updateProduct(id: String, productDTO: UpdateProductDTO, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.updateProduct(id, productDTO).fold(
                onSuccess = {
                    _message.value = "Producto actualizado exitosamente"
                    _isLoading.value = false
                    loadProducts() // Recargar lista
                    onSuccess()
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Elimina un producto
     */
    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.deleteProduct(id).fold(
                onSuccess = {
                    _message.value = "Producto eliminado exitosamente"
                    _isLoading.value = false
                    loadProducts() // Recargar lista
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Cambia la disponibilidad de un producto
     */
    fun toggleAvailability(id: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.toggleAvailability(id, isAvailable).fold(
                onSuccess = {
                    _message.value = if (isAvailable) "Producto activado" else "Producto desactivado"
                    loadProducts()
                },
                onFailure = { error ->
                    _message.value = error.message
                }
            )
        }
    }
    
    /**
     * Obtiene un producto por ID
     */
    fun getProductById(id: String, onSuccess: (ProductWithCategory) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Obtener de la lista actual si está disponible
                val currentProducts = (uiState.value as? ProductUiState.Success)?.products
                val product = currentProducts?.find { it.id == id }
                
                if (product != null) {
                    _isLoading.value = false
                    onSuccess(product)
                } else {
                    // Si no está en la lista, obtener todos los productos
                    repository.getProductsWithCategory().fold(
                        onSuccess = { products ->
                            val foundProduct = products.find { it.id == id }
                            _isLoading.value = false
                            if (foundProduct != null) {
                                onSuccess(foundProduct)
                            } else {
                                _message.value = "Producto no encontrado"
                            }
                        },
                        onFailure = { error ->
                            _message.value = error.message
                            _isLoading.value = false
                        }
                    )
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar producto: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sube una imagen de producto
     * @param imageFile archivo de imagen
     * @param onSuccess callback con la URL de la imagen
     */
    fun uploadImage(imageFile: java.io.File, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.uploadProductImage(imageFile).fold(
                onSuccess = { imageUrl ->
                    _isLoading.value = false
                    onSuccess(imageUrl)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _message.value = error.message
                }
            )
        }
    }
    
    /**
     * Limpia el mensaje
     */
    fun clearMessage() {
        _message.value = null
    }
}
