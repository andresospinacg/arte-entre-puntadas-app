package com.app_arte_entre_puntadas.data.remote

import android.content.Context
import com.app_arte_entre_puntadas.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Cliente singleton de Supabase para toda la aplicación
 */
object SupabaseClient {
    
    private var client: SupabaseClient? = null
    
    /**
     * Inicializa el cliente de Supabase
     * Debe llamarse al inicio de la aplicación
     */
    fun initialize(context: Context) {
        if (client == null) {
            client = createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_KEY
            ) {
                install(Auth)
                install(Postgrest)
                install(Storage)
            }
        }
    }
    
    /**
     * Obtiene la instancia del cliente
     * @throws IllegalStateException si no se ha inicializado
     */
    fun getClient(): SupabaseClient {
        return client ?: throw IllegalStateException(
            "SupabaseClient no ha sido inicializado. " +
            "Llama a SupabaseClient.initialize(context) primero."
        )
    }
    
    /**
     * Acceso directo al módulo de autenticación
     */
    val auth: Auth
        get() = getClient().auth
    
    /**
     * Acceso directo al módulo de base de datos
     */
    val postgrest: Postgrest
        get() = getClient().postgrest
    
    /**
     * Acceso directo al módulo de storage
     */
    val storage: Storage
        get() = getClient().storage
}
