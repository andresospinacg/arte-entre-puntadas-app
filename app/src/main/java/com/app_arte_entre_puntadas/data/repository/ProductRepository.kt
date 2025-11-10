package com.app_arte_entre_puntadas.data.repository

import com.app_arte_entre_puntadas.data.models.Category
import com.app_arte_entre_puntadas.data.models.CreateProductDTO
import com.app_arte_entre_puntadas.data.models.Product
import com.app_arte_entre_puntadas.data.models.ProductWithCategory
import com.app_arte_entre_puntadas.data.models.UpdateProductDTO
import com.app_arte_entre_puntadas.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Repositorio de productos
 * Maneja todas las operaciones CRUD de productos
 */
class ProductRepository {
    
    private val client = SupabaseClient.getClient()
    
    /**
     * Obtiene todos los productos
     */
    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val products = client.from("products")
                .select()
                .decodeList<Product>()
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener productos: ${e.message}"))
        }
    }
    
    /**
     * Obtiene productos con información de categoría
     */
    suspend fun getProductsWithCategory(): Result<List<ProductWithCategory>> = withContext(Dispatchers.IO) {
        try {
            val products = client.from("products_with_category")
                .select()
                .decodeList<ProductWithCategory>()
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener productos: ${e.message}"))
        }
    }
    
    /**
     * Obtiene un producto por ID
     */
    suspend fun getProductById(id: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val product = client.from("products")
                .select {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingle<Product>()
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener producto: ${e.message}"))
        }
    }
    
    /**
     * Obtiene productos por categoría
     */
    suspend fun getProductsByCategory(categoryId: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val products = client.from("products")
                .select {
                    filter {
                        eq("category_id", categoryId)
                    }
                }.decodeList<Product>()
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener productos por categoría: ${e.message}"))
        }
    }
    
    /**
     * Busca productos por nombre
     */
    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val products = client.from("products")
                .select {
                    filter {
                        ilike("name", "%$query%")
                    }
                }.decodeList<Product>()
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(Exception("Error al buscar productos: ${e.message}"))
        }
    }
    
    /**
     * Crea un nuevo producto
     */
    suspend fun createProduct(productDTO: CreateProductDTO): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val product = client.from("products")
                .insert(productDTO) {
                    select()
                }.decodeSingle<Product>()
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear producto: ${e.message}"))
        }
    }
    
    /**
     * Actualiza un producto existente
     */
    suspend fun updateProduct(id: String, productDTO: UpdateProductDTO): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val product = client.from("products")
                .update(productDTO) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<Product>()
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar producto: ${e.message}"))
        }
    }
    
    /**
     * Elimina un producto
     */
    suspend fun deleteProduct(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("products")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar producto: ${e.message}"))
        }
    }
    
    /**
     * Obtiene todas las categorías
     */
    suspend fun getAllCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val categories = client.from("categories")
                .select()
                .decodeList<Category>()
            
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener categorías: ${e.message}"))
        }
    }
    
    /**
     * Actualiza el stock de un producto
     */
    suspend fun updateStock(id: String, newStock: Int): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val product = client.from("products")
                .update(mapOf("stock" to newStock)) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<Product>()
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar stock: ${e.message}"))
        }
    }
    
    /**
     * Cambia la disponibilidad de un producto
     */
    suspend fun toggleAvailability(id: String, isAvailable: Boolean): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val product = client.from("products")
                .update(mapOf("is_available" to isAvailable)) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<Product>()
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Error al cambiar disponibilidad: ${e.message}"))
        }
    }
    
    /**
     * Sube una imagen al Storage de Supabase
     * @param imageFile archivo de imagen a subir
     * @return URL pública de la imagen subida
     */
    suspend fun uploadProductImage(imageFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bucket = client.storage.from("product-images")
            
            // Generar nombre único para la imagen
            val fileName = "${UUID.randomUUID()}_${imageFile.name}"
            
            // Subir archivo
            bucket.upload(fileName, imageFile.readBytes())
            
            // Obtener URL pública
            val publicUrl = bucket.publicUrl(fileName)
            
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Error al subir imagen: ${e.message}"))
        }
    }
    
    /**
     * Elimina una imagen del Storage
     * @param imageUrl URL de la imagen a eliminar
     */
    suspend fun deleteProductImage(imageUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Extraer el nombre del archivo de la URL
            val fileName = imageUrl.substringAfterLast("/")
            
            val bucket = client.storage.from("product-images")
            bucket.delete(fileName)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar imagen: ${e.message}"))
        }
    }
}
