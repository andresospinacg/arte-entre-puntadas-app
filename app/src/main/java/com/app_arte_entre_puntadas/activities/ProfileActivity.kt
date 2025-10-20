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
import com.app_arte_entre_puntadas.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnMenu: ImageView
    private lateinit var btnClose: ImageView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupListeners()
        loadProfileData()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        btnChangePassword = findViewById(R.id.btn_change_password)
        btnLogout = findViewById(R.id.btn_logout)
        btnMenu = findViewById(R.id.btn_menu)
        btnClose = findViewById(R.id.btn_close)
        ivProfilePicture = findViewById(R.id.iv_profile_picture)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
    }

    private fun setupListeners() {
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
        // Cargar datos del usuario desde SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "María González") ?: "Usuario"
        val userEmail = sharedPref.getString("email", "maria@email.com") ?: ""
        val userPhone = sharedPref.getString("phone", "+57 300 123 4567") ?: ""

        tvUserName.text = userName
        tvUserEmail.text = userEmail
        etName.setText(userName)
        etPhone.setText(userPhone)
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
        // Limpiar preferencias y volver a login
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
