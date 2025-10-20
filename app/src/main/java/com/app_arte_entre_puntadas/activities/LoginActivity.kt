package com.app_arte_entre_puntadas.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app_arte_entre_puntadas.MainActivity
import com.app_arte_entre_puntadas.R

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnClose: ImageView
    private lateinit var icPasswordToggle: ImageView
    
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        cbRemember = findViewById(R.id.cb_remember)
        btnLogin = findViewById(R.id.btn_login)
        btnGoogle = findViewById(R.id.btn_google)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        tvRegister = findViewById(R.id.tv_register)
        btnBack = findViewById(R.id.btn_back)
        btnClose = findViewById(R.id.btn_close)
        icPasswordToggle = findViewById(R.id.ic_password_toggle)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            handleLogin()
        }

        btnGoogle.setOnClickListener {
            handleGoogleLogin()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
            // TODO: Implementar actividad de recuperación de contraseña
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnClose.setOnClickListener {
            finish()
        }
        
        icPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }
    
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isPasswordVisible = false
        } else {
            // Mostrar contraseña
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            isPasswordVisible = true
        }
        // Mover el cursor al final del texto
        etPassword.setSelection(etPassword.text.length)
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor completa todos los campos")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.contains("@")) {
            // Permitir usuario sin @ o validar email con @
            if (email.contains("@")) {
                showError("Email inválido")
                return
            }
        }

        // Guardar preferencia de recordar
        if (cbRemember.isChecked) {
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPref.edit().putString("email", email).apply()
        }

        // TODO: Implementar autenticación con backend
        // Por ahora, navegar a MainActivity si las credenciales son válidas
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    private fun handleGoogleLogin() {
        Toast.makeText(this, "Autenticación con Google en desarrollo", Toast.LENGTH_SHORT).show()
        // TODO: Implementar autenticación con Google
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
