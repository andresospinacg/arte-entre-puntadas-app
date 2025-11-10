package com.app_arte_entre_puntadas.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app_arte_entre_puntadas.BuildConfig
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.local.SessionManager
import com.app_arte_entre_puntadas.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnMenu: ImageView
    private lateinit var btnClose: ImageView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        authRepository = AuthRepository(this)
        
        // Inicializar Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        initializeViews()
        setupListeners()
        loadProfileData()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        btnSaveProfile = findViewById(R.id.btn_save_profile)
        btnChangePassword = findViewById(R.id.btn_change_password)
        btnLogout = findViewById(R.id.btn_logout)
        btnMenu = findViewById(R.id.btn_menu)
        btnClose = findViewById(R.id.btn_close)
        ivProfilePicture = findViewById(R.id.iv_profile_picture)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
    }

    private fun setupListeners() {
        btnSaveProfile.setOnClickListener {
            handleSaveProfile()
        }
        
        btnChangePassword.setOnClickListener {
            handleChangePassword()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        btnMenu.setOnClickListener {
            Toast.makeText(this, "Menú en desarrollo", Toast.LENGTH_SHORT).show()
            // TODO: Implementar menú lateral
        }

        btnClose.setOnClickListener {
            finish()
        }

        ivProfilePicture.setOnClickListener {
            Toast.makeText(this, "Cambiar foto de perfil en desarrollo", Toast.LENGTH_SHORT).show()
            // TODO: Implementar selector de foto de perfil
        }
    }

    private fun loadProfileData() {
        // Cargar datos del usuario desde SessionManager
        val userName = sessionManager.getFullName() ?: "Usuario"
        val userEmail = sessionManager.getEmail() ?: "email@ejemplo.com"
        val userPhone = sessionManager.getPhone() ?: ""
        
        tvUserName.text = userName
        tvUserEmail.text = userEmail
        etName.setText(userName)
        etPhone.setText(userPhone)
        
        // Intentar actualizar el perfil desde Supabase
        lifecycleScope.launch {
            val result = authRepository.getCurrentProfile()
            result.onSuccess { profile ->
                runOnUiThread {
                    tvUserName.text = profile.fullName
                    tvUserEmail.text = sessionManager.getEmail() ?: ""
                    etName.setText(profile.fullName)
                    etPhone.setText(profile.phone ?: "")
                }
            }.onFailure {
                // Si falla, mantenemos los datos del SessionManager
            }
        }
    }
    
    private fun handleSaveProfile() {
        val newName = etName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()
        
        // Validaciones
        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Deshabilitar botón mientras se guarda
        btnSaveProfile.isEnabled = false
        Toast.makeText(this, "Guardando cambios...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            val result = authRepository.updateProfile(
                fullName = newName,
                phone = if (newPhone.isNotEmpty()) newPhone else null
            )
            
            runOnUiThread {
                btnSaveProfile.isEnabled = true
                
                result.onSuccess { profile ->
                    // Actualizar los datos mostrados
                    tvUserName.text = profile.fullName
                    etName.setText(profile.fullName)
                    etPhone.setText(profile.phone ?: "")
                    
                    Toast.makeText(
                        this@ProfileActivity,
                        "Perfil actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { error ->
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error al actualizar: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleChangePassword() {
        Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        // TODO: Implementar actividad para cambiar contraseña
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                handleLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            // Primero cerrar sesión en Google Sign-In
            googleSignInClient.signOut().addOnCompleteListener {
                // Continuar con el cierre de sesión en Supabase
                lifecycleScope.launch {
                    val result = authRepository.logout()
                    
                    runOnUiThread {
                        result.onSuccess {
                            Toast.makeText(this@ProfileActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            // Aunque falle, el logout local ya se ejecutó
                            Toast.makeText(this@ProfileActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Redirigir al login
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
