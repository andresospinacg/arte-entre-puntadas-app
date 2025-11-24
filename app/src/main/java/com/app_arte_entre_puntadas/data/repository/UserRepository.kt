package com.app_arte_entre_puntadas.data.repository

import com.app_arte_entre_puntadas.data.models.CreateUserDTO
import com.app_arte_entre_puntadas.data.models.UpdateUserDTO
import com.app_arte_entre_puntadas.data.models.User
import com.app_arte_entre_puntadas.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Repositorio para gestión de usuarios
 * Maneja operaciones CRUD con geolocalización y almacenamiento de imágenes
 */
class UserRepository {
    
    private val client = SupabaseClient.getClient()
    
    /**
     * Obtiene todos los usuarios
     */
    suspend fun getAllUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val users = client.from("profiles")
                .select()
                .decodeList<User>()
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuarios: ${e.message}"))
        }
    }
    
    /**
     * Obtiene un usuario por ID
     */
    suspend fun getUserById(id: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = client.from("profiles")
                .select {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingle<User>()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuario: ${e.message}"))
        }
    }
    
    /**
     * Busca usuarios por nombre
     */
    suspend fun searchUsers(query: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val users = client.from("profiles")
                .select {
                    filter {
                        ilike("full_name", "%$query%")
                    }
                }.decodeList<User>()
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Error al buscar usuarios: ${e.message}"))
        }
    }
    
    /**
     * Filtra usuarios por rol
     */
    suspend fun getUsersByRole(role: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val users = client.from("profiles")
                .select {
                    filter {
                        eq("role", role)
                    }
                }.decodeList<User>()
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Error al filtrar usuarios: ${e.message}"))
        }
    }
    
    /**
     * Obtiene usuarios que tienen tienda física
     */
    suspend fun getUsersWithStore(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val users = client.from("profiles")
                .select()
                .decodeList<User>()
                .filter { it.storeName != null }
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuarios con tienda: ${e.message}"))
        }
    }
    
    /**
     * Obtiene usuarios cercanos a una ubicación
     * @param latitude latitud de referencia
     * @param longitude longitud de referencia
     * @param radiusKm radio de búsqueda en kilómetros
     */
    suspend fun getUsersNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            // Obtener todos los usuarios
            val allUsers = client.from("profiles")
                .select()
                .decodeList<User>()
            
            // Filtrar por usuarios con ubicación y distancia
            val nearbyUsers = allUsers.filter { user ->
                user.latitude?.let { userLat ->
                    user.longitude?.let { userLon ->
                        calculateDistance(latitude, longitude, userLat, userLon) <= radiusKm
                    } ?: false
                } ?: false
            }
            
            Result.success(nearbyUsers)
        } catch (e: Exception) {
            Result.failure(Exception("Error al buscar usuarios cercanos: ${e.message}"))
        }
    }
    
    /**
     * Obtiene tiendas cercanas a una ubicación
     */
    suspend fun getStoresNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            // Obtener todos los usuarios
            val allUsers = client.from("profiles")
                .select()
                .decodeList<User>()
            
            // Filtrar por usuarios con tienda, ubicación de tienda y distancia
            val nearbyStores = allUsers.filter { user ->
                !user.storeName.isNullOrEmpty() &&
                user.storeLatitude?.let { storeLat ->
                    user.storeLongitude?.let { storeLon ->
                        calculateDistance(latitude, longitude, storeLat, storeLon) <= radiusKm
                    } ?: false
                } ?: false
            }
            
            Result.success(nearbyStores)
        } catch (e: Exception) {
            Result.failure(Exception("Error al buscar tiendas cercanas: ${e.message}"))
        }
    }
    
    /**
     * Crea un nuevo usuario (solo para admins)
     * Primero lo crea en auth.users y luego crea su perfil en profiles
     */
    suspend fun createUser(userDTO: CreateUserDTO): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Verificar que email y password existan
            if (userDTO.email.isNullOrBlank() || userDTO.password.isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("Email y contraseña son requeridos para crear un usuario")
                )
            }
            
            // 1. Crear usuario en auth.users
            client.auth.signUpWith(Email) {
                email = userDTO.email
                password = userDTO.password
            }
            
            // 2. Obtener el usuario recién creado
            val authUser = client.auth.currentUserOrNull()
                ?: throw Exception("No se pudo obtener el usuario después de crearlo")
            
            val userId = authUser.id
            
            // 3. Crear perfil en profiles con el ID del usuario recién creado
            val profileData = mapOf(
                "id" to userId,
                "full_name" to userDTO.fullName,
                "phone" to userDTO.phone,
                "role" to userDTO.role,
                "avatar_url" to userDTO.avatarUrl,
                "latitude" to userDTO.latitude,
                "longitude" to userDTO.longitude,
                "address" to userDTO.address,
                "city" to userDTO.city,
                "country" to userDTO.country,
                "store_name" to userDTO.storeName,
                "store_description" to userDTO.storeDescription,
                "store_latitude" to userDTO.storeLatitude,
                "store_longitude" to userDTO.storeLongitude,
                "store_address" to userDTO.storeAddress,
                "store_phone" to userDTO.storePhone,
                "store_image_url" to userDTO.storeImageUrl
            )
            
            val user = client.from("profiles")
                .insert(profileData) {
                    select()
                }.decodeSingle<User>()
            
            // 4. Cerrar sesión del usuario recién creado (ya que el admin no debe quedar logueado como ese usuario)
            client.auth.signOut()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear usuario: ${e.message}"))
        }
    }
    
    /**
     * Actualiza un usuario existente
     */
    suspend fun updateUser(id: String, userDTO: UpdateUserDTO): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = client.from("profiles")
                .update(userDTO) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<User>()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar usuario: ${e.message}"))
        }
    }
    
    /**
     * Elimina un usuario (soft delete - cambiar estado)
     */
    suspend fun deleteUser(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.from("profiles")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar usuario: ${e.message}"))
        }
    }
    
    /**
     * Actualiza el avatar del usuario
     */
    suspend fun updateAvatar(id: String, avatarUrl: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = client.from("profiles")
                .update(mapOf("avatar_url" to avatarUrl)) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<User>()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar avatar: ${e.message}"))
        }
    }
    
    /**
     * Actualiza la ubicación del usuario
     */
    suspend fun updateLocation(
        id: String,
        latitude: Double,
        longitude: Double,
        address: String? = null,
        city: String? = null,
        country: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val updateData = mutableMapOf<String, Any?>(
                "latitude" to latitude,
                "longitude" to longitude
            )
            
            address?.let { updateData["address"] = it }
            city?.let { updateData["city"] = it }
            country?.let { updateData["country"] = it }
            
            val user = client.from("profiles")
                .update(updateData) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<User>()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar ubicación: ${e.message}"))
        }
    }
    
    /**
     * Actualiza los datos de la tienda
     */
    suspend fun updateStore(
        id: String,
        storeName: String?,
        storeDescription: String?,
        storeLatitude: Double?,
        storeLongitude: Double?,
        storeAddress: String?,
        storePhone: String?,
        storeImageUrl: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val updateData = mutableMapOf<String, Any?>()
            
            storeName?.let { updateData["store_name"] = it }
            storeDescription?.let { updateData["store_description"] = it }
            storeLatitude?.let { updateData["store_latitude"] = it }
            storeLongitude?.let { updateData["store_longitude"] = it }
            storeAddress?.let { updateData["store_address"] = it }
            storePhone?.let { updateData["store_phone"] = it }
            storeImageUrl?.let { updateData["store_image_url"] = it }
            
            val user = client.from("profiles")
                .update(updateData) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }.decodeSingle<User>()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar tienda: ${e.message}"))
        }
    }
    
    /**
     * Sube una imagen de avatar al Storage de Supabase
     * @param imageFile archivo de imagen a subir
     * @return URL pública de la imagen subida
     */
    suspend fun uploadAvatar(imageFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bucket = client.storage.from("avatars")
            
            // Generar nombre único para la imagen
            val fileName = "${UUID.randomUUID()}_${imageFile.name}"
            
            // Subir archivo
            bucket.upload(fileName, imageFile.readBytes())
            
            // Obtener URL pública
            val publicUrl = bucket.publicUrl(fileName)
            
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Error al subir avatar: ${e.message}"))
        }
    }
    
    /**
     * Sube una imagen de tienda al Storage
     */
    suspend fun uploadStoreImage(imageFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bucket = client.storage.from("store-images")
            
            // Generar nombre único para la imagen
            val fileName = "${UUID.randomUUID()}_${imageFile.name}"
            
            // Subir archivo
            bucket.upload(fileName, imageFile.readBytes())
            
            // Obtener URL pública
            val publicUrl = bucket.publicUrl(fileName)
            
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Error al subir imagen de tienda: ${e.message}"))
        }
    }
    
    /**
     * Elimina una imagen del Storage
     * @param imageUrl URL de la imagen a eliminar
     * @param bucket nombre del bucket (avatars o store-images)
     */
    suspend fun deleteImage(imageUrl: String, bucket: String = "avatars"): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Extraer el nombre del archivo de la URL
            val fileName = imageUrl.substringAfterLast("/")
            
            val storageBucket = client.storage.from(bucket)
            storageBucket.delete(fileName)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar imagen: ${e.message}"))
        }
    }
    
    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * @return distancia en kilómetros
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadiusKm * c
    }
}
