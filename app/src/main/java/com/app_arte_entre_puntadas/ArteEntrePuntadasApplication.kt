package com.app_arte_entre_puntadas

import android.app.Application
import com.app_arte_entre_puntadas.data.remote.SupabaseClient

/**
 * Clase Application principal
 * Se inicializa al arrancar la app
 */
class ArteEntrePuntadasApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Supabase
        SupabaseClient.initialize(this)
        
        // Log para debug
        android.util.Log.d("App", "Supabase inicializado correctamente")
    }
}
