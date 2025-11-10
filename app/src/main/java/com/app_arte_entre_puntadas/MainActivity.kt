package com.app_arte_entre_puntadas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app_arte_entre_puntadas.activities.LoginActivity
import com.app_arte_entre_puntadas.activities.ProfileActivity
import com.app_arte_entre_puntadas.data.local.SessionManager
import com.app_arte_entre_puntadas.data.remote.SupabaseClient
import com.app_arte_entre_puntadas.ui.cart.CartFragment
import com.app_arte_entre_puntadas.ui.cart.CartViewModel
import com.app_arte_entre_puntadas.ui.products.ProductListFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var cartViewModel: CartViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private var cartMenuItem: MenuItem? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar Edge-to-Edge para que la app use toda la pantalla
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        try {
            setContentView(R.layout.activity_main_simple)
            
            sessionManager = SessionManager(this)
            cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
            
            // Inicializar Google Sign-In Client
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)

            // Configurar toolbar como ActionBar para que el menú sea visible
            val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar_main)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "Productos"

            // Cargar el fragmento de productos directamente
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProductListFragment())
                    .commit()
            }
            
            // Configurar ActionBar
            supportActionBar?.title = "Productos"
            
            // Observar cambios en el carrito para actualizar el badge
            observeCartCount()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        cartMenuItem = menu?.findItem(R.id.action_cart)
        updateCartBadge(cartViewModel.cartItemCount.value ?: 0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                openCart()
                true
            }
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Configuración - En desarrollo", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Observa el conteo de items del carrito
     */
    private fun observeCartCount() {
        cartViewModel.cartItemCount.observe(this) { count ->
            updateCartBadge(count)
        }
    }
    
    /**
     * Actualiza el badge del carrito en el menú
     */
    private fun updateCartBadge(count: Int) {
        cartMenuItem?.let { menuItem ->
            if (count > 0) {
                menuItem.title = "Carrito ($count)"
            } else {
                menuItem.title = "Carrito"
            }
        }
    }
    
    /**
     * Abre el fragmento del carrito
     */
    private fun openCart() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CartFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
    
    /**
     * Muestra un diálogo de confirmación antes de cerrar sesión
     */
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Está seguro que desea cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Realiza el cierre de sesión
     */
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Cerrar sesión en Google Sign-In
                googleSignInClient.signOut().addOnCompleteListener {
                    // Continuar con el cierre de sesión en Supabase
                    lifecycleScope.launch {
                        try {
                            // Cerrar sesión en Supabase
                            SupabaseClient.auth.signOut()
                            
                            // Limpiar sesión local
                            sessionManager.clearSession()
                            
                            // Redirigir al login
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                            
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error al cerrar sesión: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al cerrar sesión: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}