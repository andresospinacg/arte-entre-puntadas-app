package com.app_arte_entre_puntadas.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app_arte_entre_puntadas.data.models.CreateUserDTO
import com.app_arte_entre_puntadas.data.models.Location
import com.app_arte_entre_puntadas.data.models.UpdateUserDTO
import com.app_arte_entre_puntadas.data.models.User
import com.app_arte_entre_puntadas.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.io.File

/**
 * Estados posibles de la UI de usuarios
 */
sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    data class Error(val message: String) : UserUiState()
    object Empty : UserUiState()
}

/**
 * ViewModel para gestión de usuarios
 */
class UserViewModel : ViewModel() {
    
    private val repository = UserRepository()
    
    // Estado de la lista de usuarios
    private val _uiState = MutableLiveData<UserUiState>()
    val uiState: LiveData<UserUiState> = _uiState
    
    // Usuario seleccionado
    private val _selectedUser = MutableLiveData<User?>()
    val selectedUser: LiveData<User?> = _selectedUser
    
    // Mensajes de éxito/error
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Ubicación actual del dispositivo
    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation
    
    // URL de imagen temporal (antes de guardar)
    private val _tempImageUrl = MutableLiveData<String?>()
    val tempImageUrl: LiveData<String?> = _tempImageUrl
    
    init {
        loadUsers()
    }
    
    /**
     * Carga todos los usuarios
     */
    fun loadUsers() {
        viewModelScope.launch {
            try {
                _uiState.value = UserUiState.Loading
                
                repository.getAllUsers().fold(
                    onSuccess = { users ->
                        if (users.isEmpty()) {
                            _uiState.value = UserUiState.Empty
                        } else {
                            _uiState.value = UserUiState.Success(users)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = UserUiState.Error(error.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error("Error al cargar usuarios: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene un usuario por ID
     */
    fun getUserById(id: String, onSuccess: (User) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.getUserById(id).fold(
                onSuccess = { user ->
                    _selectedUser.value = user
                    _isLoading.value = false
                    onSuccess(user)
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Busca usuarios por nombre
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            loadUsers()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            repository.searchUsers(query).fold(
                onSuccess = { users ->
                    if (users.isEmpty()) {
                        _uiState.value = UserUiState.Empty
                    } else {
                        _uiState.value = UserUiState.Success(users)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UserUiState.Error(error.message ?: "Error en búsqueda")
                }
            )
        }
    }
    
    /**
     * Filtra usuarios por rol
     */
    fun filterByRole(role: String?) {
        if (role == null) {
            loadUsers()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            repository.getUsersByRole(role).fold(
                onSuccess = { users ->
                    if (users.isEmpty()) {
                        _uiState.value = UserUiState.Empty
                    } else {
                        _uiState.value = UserUiState.Success(users)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UserUiState.Error(error.message ?: "Error al filtrar")
                }
            )
        }
    }
    
    /**
     * Obtiene usuarios con tienda
     */
    fun loadUsersWithStore() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            repository.getUsersWithStore().fold(
                onSuccess = { users ->
                    if (users.isEmpty()) {
                        _uiState.value = UserUiState.Empty
                    } else {
                        _uiState.value = UserUiState.Success(users)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UserUiState.Error(error.message ?: "Error al cargar tiendas")
                }
            )
        }
    }
    
    /**
     * Busca usuarios cercanos a una ubicación
     */
    fun findNearbyUsers(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            repository.getUsersNearLocation(latitude, longitude, radiusKm).fold(
                onSuccess = { users ->
                    if (users.isEmpty()) {
                        _uiState.value = UserUiState.Empty
                    } else {
                        _uiState.value = UserUiState.Success(users)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UserUiState.Error(error.message ?: "Error al buscar usuarios cercanos")
                }
            )
        }
    }
    
    /**
     * Busca tiendas cercanas a una ubicación
     */
    fun findNearbyStores(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            repository.getStoresNearLocation(latitude, longitude, radiusKm).fold(
                onSuccess = { stores ->
                    if (stores.isEmpty()) {
                        _uiState.value = UserUiState.Empty
                    } else {
                        _uiState.value = UserUiState.Success(stores)
                    }
                },
                onFailure = { error ->
                    _uiState.value = UserUiState.Error(error.message ?: "Error al buscar tiendas cercanas")
                }
            )
        }
    }
    
    /**
     * Crea un nuevo usuario
     */
    fun createUser(userDTO: CreateUserDTO, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.createUser(userDTO).fold(
                onSuccess = {
                    _message.value = "Usuario creado exitosamente"
                    _isLoading.value = false
                    loadUsers()
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
     * Actualiza un usuario existente
     */
    fun updateUser(id: String, userDTO: UpdateUserDTO, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.updateUser(id, userDTO).fold(
                onSuccess = {
                    _message.value = "Usuario actualizado exitosamente"
                    _isLoading.value = false
                    loadUsers()
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
     * Elimina un usuario
     */
    fun deleteUser(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.deleteUser(id).fold(
                onSuccess = {
                    _message.value = "Usuario eliminado exitosamente"
                    _isLoading.value = false
                    loadUsers()
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Actualiza la ubicación de un usuario
     */
    fun updateUserLocation(
        id: String,
        latitude: Double,
        longitude: Double,
        address: String? = null,
        city: String? = null,
        country: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.updateLocation(id, latitude, longitude, address, city, country).fold(
                onSuccess = { user ->
                    _message.value = "Ubicación actualizada"
                    _selectedUser.value = user
                    _isLoading.value = false
                    loadUsers()
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Actualiza los datos de la tienda de un usuario
     */
    fun updateStore(
        id: String,
        storeName: String?,
        storeDescription: String?,
        storeLatitude: Double?,
        storeLongitude: Double?,
        storeAddress: String?,
        storePhone: String?,
        storeImageUrl: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.updateStore(
                id,
                storeName,
                storeDescription,
                storeLatitude,
                storeLongitude,
                storeAddress,
                storePhone,
                storeImageUrl
            ).fold(
                onSuccess = { user ->
                    _message.value = "Tienda actualizada"
                    _selectedUser.value = user
                    _isLoading.value = false
                    loadUsers()
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Sube una imagen de avatar
     */
    fun uploadAvatar(imageFile: File, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.uploadAvatar(imageFile).fold(
                onSuccess = { imageUrl ->
                    _tempImageUrl.value = imageUrl
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
     * Sube una imagen de tienda
     */
    fun uploadStoreImage(imageFile: File, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.uploadStoreImage(imageFile).fold(
                onSuccess = { imageUrl ->
                    _tempImageUrl.value = imageUrl
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
     * Actualiza el avatar de un usuario
     */
    fun updateAvatar(id: String, avatarUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.updateAvatar(id, avatarUrl).fold(
                onSuccess = { user ->
                    _message.value = "Avatar actualizado"
                    _selectedUser.value = user
                    _isLoading.value = false
                    loadUsers()
                },
                onFailure = { error ->
                    _message.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    /**
     * Establece la ubicación actual del dispositivo
     */
    fun setCurrentLocation(location: Location) {
        _currentLocation.value = location
    }
    
    /**
     * Limpia el mensaje
     */
    fun clearMessage() {
        _message.value = null
    }
    
    /**
     * Limpia la imagen temporal
     */
    fun clearTempImage() {
        _tempImageUrl.value = null
    }
    
    /**
     * Limpia el usuario seleccionado
     */
    fun clearSelectedUser() {
        _selectedUser.value = null
    }
}
