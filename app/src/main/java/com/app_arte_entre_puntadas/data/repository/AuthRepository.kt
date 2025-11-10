package com.app_arte_entre_puntadas.data.repository

import android.content.Context
import android.util.Log
import com.app_arte_entre_puntadas.data.local.SessionManager
import com.app_arte_entre_puntadas.data.models.Profile
import com.app_arte_entre_puntadas.data.models.RegisterDTO
import com.app_arte_entre_puntadas.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio de autenticación
 * Maneja todo lo relacionado con login, registro y sesión
 */
class AuthRepository(context: Context) {
    
    private val client = SupabaseClient.getClient()
    private val sessionManager = SessionManager(context)
    private val contextRef = context.applicationContext
    
    /**
     * Registra un nuevo usuario
     * @return Result con el Profile del usuario o error
     */
    suspend fun register(registerDTO: RegisterDTO): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            // 1. Crear usuario en Supabase Auth
            client.auth.signUpWith(Email) {
                email = registerDTO.email
                password = registerDTO.password
            }
            
            // 2. Verificar si el usuario está logueado (si no requiere confirmación de email)
            val session = client.auth.currentSessionOrNull()
            
            if (session == null) {
                // El usuario necesita confirmar su email
                throw Exception("Cuenta creada. Por favor verifica tu email para activarla.")
            }
            
            // Obtener el ID del usuario de la sesión
            val userId = session.user?.id 
                ?: throw Exception("Error al obtener ID de usuario")
            
            // 3. Actualizar el perfil con teléfono y nombre (el trigger ya creó el perfil básico)
            client.from("profiles").update(
                mapOf(
                    "phone" to registerDTO.phone,
                    "full_name" to registerDTO.fullName
                )
            ) {
                filter {
                    eq("id", userId)
                }
            }
            
            // 4. Obtener el perfil completo
            val profile = client.from("profiles")
                .select(columns = Columns.list("id", "full_name", "phone", "role", "avatar_url")) {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<Profile>()
            
            // 5. Guardar en sesión
            sessionManager.saveUserProfile(
                profile = profile,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
            )
            sessionManager.saveEmail(registerDTO.email)
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception("Error al registrar: ${e.message}"))
        }
    }
    
    /**
     * Inicia sesión con email y contraseña
     * @return Result con el Profile del usuario o error
     */
    suspend fun login(email: String, password: String): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            // 1. Autenticar con Supabase
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Obtener el usuario actual después del login
            val currentUser = client.auth.currentUserOrNull()
                ?: throw Exception("Error al obtener ID de usuario")
            val userId = currentUser.id
            
            // 2. Obtener perfil del usuario
            val profile = client.from("profiles")
                .select(columns = Columns.list("id", "full_name", "phone", "role", "avatar_url")) {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<Profile>()
            
            // 3. Guardar en sesión
            val session = client.auth.currentSessionOrNull()
            sessionManager.saveUserProfile(
                profile = profile,
                accessToken = session?.accessToken ?: "",
                refreshToken = session?.refreshToken ?: ""
            )
            sessionManager.saveEmail(email)
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception("Error al iniciar sesión: ${e.message}"))
        }
    }
    
    /**
     * Cierra la sesión del usuario
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Cerrar sesión en Supabase
            client.auth.signOut()
            
            // 2. Limpiar sesión local
            sessionManager.clearSession()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Aunque falle el logout remoto, limpiamos la sesión local
            sessionManager.clearSession()
            Result.failure(Exception("Error al cerrar sesión: ${e.message}"))
        }
    }
    
    /**
     * Inicia sesión con Google usando ID Token
     * @param idToken Token de Google obtenido del Google Sign-In
     * @return Result con el Profile del usuario o error
     */
    suspend fun loginWithGoogle(idToken: String): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthRepository", "Iniciando autenticación con Google...")
            Log.d("AuthRepository", "ID Token recibido: ${idToken.take(50)}...")
            
            // 1. Autenticar con Supabase usando el ID Token de Google
            client.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            
            Log.d("AuthRepository", "Autenticación exitosa con Supabase")
            
            // 2. Obtener el usuario actual
            val currentUser = client.auth.currentUserOrNull()
                ?: throw Exception("Error al obtener usuario después de autenticación")
            
            Log.d("AuthRepository", "Usuario obtenido: ${currentUser.email}")
            
            val userId = currentUser.id
            val email = currentUser.email
                ?: throw Exception("No se pudo obtener el email del usuario")
            
            // 3. Verificar si existe el perfil, si no, crearlo
            val profileResult = try {
                client.from("profiles")
                    .select(columns = Columns.list("id")) {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingleOrNull<Map<String, String>>()
            } catch (e: Exception) {
                null
            }
            
            val profileExists = profileResult != null
            
            if (!profileExists) {
                // Crear perfil básico con el nombre de Google
                val userName = currentUser.userMetadata?.get("full_name")?.toString()
                    ?: currentUser.userMetadata?.get("name")?.toString()
                    ?: email.substringBefore("@")
                
                client.from("profiles").insert(
                    mapOf(
                        "id" to userId,
                        "full_name" to userName,
                        "role" to "client"
                    )
                )
            }
            
            // 4. Obtener el perfil completo
            val profile = client.from("profiles")
                .select(columns = Columns.list("id", "full_name", "phone", "role", "avatar_url")) {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<Profile>()
            
            // 5. Guardar en sesión
            val session = client.auth.currentSessionOrNull()
            sessionManager.saveUserProfile(
                profile = profile,
                accessToken = session?.accessToken ?: "",
                refreshToken = session?.refreshToken ?: ""
            )
            sessionManager.saveEmail(email)
            
            Log.d("AuthRepository", "Sesión guardada exitosamente")
            
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en loginWithGoogle", e)
            Log.e("AuthRepository", "Mensaje de error: ${e.message}")
            Log.e("AuthRepository", "Stack trace: ${e.stackTraceToString()}")
            Result.failure(Exception("Error al iniciar sesión con Google: ${e.message}"))
        }
    }
    
    /**
     * Obtiene el perfil del usuario actual
     */
    suspend fun getCurrentProfile(): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() 
                ?: throw Exception("No hay usuario logueado")
            
            val profile = client.from("profiles")
                .select(columns = Columns.list("id", "full_name", "phone", "role", "avatar_url")) {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<Profile>()
            
            // Actualizar sesión con datos frescos
            sessionManager.saveUserProfile(profile)
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener perfil: ${e.message}"))
        }
    }
    
    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateProfile(fullName: String, phone: String?): Result<Profile> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() 
                ?: throw Exception("No hay usuario logueado")
            
            // Actualizar en Supabase
            client.from("profiles").update(
                mapOf(
                    "full_name" to fullName,
                    "phone" to phone
                )
            ) {
                filter {
                    eq("id", userId)
                }
            }
            
            // Obtener perfil actualizado
            val profile = client.from("profiles")
                .select(columns = Columns.list("id", "full_name", "phone", "role", "avatar_url")) {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<Profile>()
            
            // Actualizar sesión
            sessionManager.saveUserProfile(profile)
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar perfil: ${e.message}"))
        }
    }
    
    /**
     * Verifica si hay un usuario logueado
     */
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    
    /**
     * Obtiene el rol del usuario actual
     */
    fun getUserRole(): String = sessionManager.getRole()
    
    /**
     * Verifica si el usuario actual es administrador
     */
    fun isAdmin(): Boolean = sessionManager.isAdmin()
    
    /**
     * Obtiene el SessionManager
     */
    fun getSessionManager(): SessionManager = sessionManager
}
