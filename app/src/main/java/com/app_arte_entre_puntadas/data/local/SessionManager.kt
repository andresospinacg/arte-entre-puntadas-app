package com.app_arte_entre_puntadas.data.local

import android.content.Context
import android.content.SharedPreferences
import com.app_arte_entre_puntadas.data.models.Profile

/**
 * Gestor de sesión del usuario
 * Maneja el almacenamiento local de datos de sesión
 */
class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "arte_entre_puntadas_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome"
    }
    
    /**
     * Guarda los datos del perfil del usuario
     */
    fun saveUserProfile(profile: Profile, accessToken: String? = null, refreshToken: String? = null) {
        prefs.edit().apply {
            putString(KEY_USER_ID, profile.id)
            putString(KEY_EMAIL, "")  // El email está en auth.users, no en profile
            putString(KEY_FULL_NAME, profile.fullName)
            putString(KEY_PHONE, profile.phone)
            putString(KEY_ROLE, profile.role)
            putString(KEY_AVATAR_URL, profile.avatarUrl)
            accessToken?.let { putString(KEY_ACCESS_TOKEN, it) }
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    /**
     * Obtiene el email del usuario actual
     */
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    
    /**
     * Obtiene el nombre completo del usuario
     */
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    
    /**
     * Obtiene el teléfono del usuario
     */
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    
    /**
     * Obtiene el rol del usuario (client o admin)
     */
    fun getRole(): String = prefs.getString(KEY_ROLE, "client") ?: "client"
    
    /**
     * Verifica si el usuario es administrador
     */
    fun isAdmin(): Boolean = getRole() == "admin"
    
    /**
     * Verifica si el usuario es cliente
     */
    fun isClient(): Boolean = getRole() == "client"
    
    /**
     * Obtiene la URL del avatar
     */
    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)
    
    /**
     * Obtiene el access token
     */
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    
    /**
     * Obtiene el refresh token
     */
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    
    /**
     * Verifica si el usuario está logueado
     */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    /**
     * Guarda el email del usuario
     */
    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }
    
    /**
     * Guarda los tokens de autenticación
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }
    
    /**
     * Limpia la sesión del usuario (logout)
     */
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_EMAIL)
            remove(KEY_FULL_NAME)
            remove(KEY_PHONE)
            remove(KEY_ROLE)
            remove(KEY_AVATAR_URL)
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    /**
     * Marca que el usuario ya vio la bienvenida
     */
    fun setHasSeenWelcome(seen: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEN_WELCOME, seen).apply()
    }
    
    /**
     * Verifica si el usuario ya vio la bienvenida
     */
    fun hasSeenWelcome(): Boolean = prefs.getBoolean(KEY_HAS_SEEN_WELCOME, false)
    
    /**
     * Actualiza el nombre completo del usuario
     */
    fun updateFullName(fullName: String) {
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply()
    }
    
    /**
     * Actualiza el teléfono del usuario
     */
    fun updatePhone(phone: String) {
        prefs.edit().putString(KEY_PHONE, phone).apply()
    }
    
    /**
     * Actualiza la URL del avatar
     */
    fun updateAvatarUrl(avatarUrl: String) {
        prefs.edit().putString(KEY_AVATAR_URL, avatarUrl).apply()
    }
}
