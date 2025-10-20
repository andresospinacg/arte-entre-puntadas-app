package com.app_arte_entre_puntadas.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app_arte_entre_puntadas.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnClose: ImageView
    private lateinit var icInfoName: ImageView
    private lateinit var icInfoEmail: ImageView
    private lateinit var icInfoPhone: ImageView
    private lateinit var icInfoPassword: ImageView
    private lateinit var icInfoConfirm: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etFullname = findViewById(R.id.et_fullname)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        cbTerms = findViewById(R.id.cb_terms)
        btnRegister = findViewById(R.id.btn_register)
        tvLogin = findViewById(R.id.tv_login)
        btnBack = findViewById(R.id.btn_back)
        btnClose = findViewById(R.id.btn_close)
        icInfoName = findViewById(R.id.ic_info_name)
        icInfoEmail = findViewById(R.id.ic_info_email)
        icInfoPhone = findViewById(R.id.ic_info_phone)
        icInfoPassword = findViewById(R.id.ic_info_password)
        icInfoConfirm = findViewById(R.id.ic_info_confirm)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            handleRegister()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnClose.setOnClickListener {
            finish()
        }
        
        // Listeners para iconos de información
        icInfoName.setOnClickListener {
            Toast.makeText(this, "Campo obligatorio: Ingresa tu nombre completo", Toast.LENGTH_SHORT).show()
        }
        
        icInfoEmail.setOnClickListener {
            Toast.makeText(this, "Campo obligatorio: Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
        }
        
        icInfoPhone.setOnClickListener {
            Toast.makeText(this, "Campo obligatorio: Ingresa tu número de teléfono", Toast.LENGTH_SHORT).show()
        }
        
        icInfoPassword.setOnClickListener {
            Toast.makeText(this, "Campo obligatorio: La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        }
        
        icInfoConfirm.setOnClickListener {
            Toast.makeText(this, "Campo obligatorio: Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRegister() {
        val fullname = etFullname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullname.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("Por favor completa todos los campos")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email inválido")
            return
        }

        if (password.length < 6) {
            showError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (password != confirmPassword) {
            showError("Las contraseñas no coinciden")
            return
        }

        if (!cbTerms.isChecked) {
            showError("Debes aceptar los términos y condiciones")
            return
        }

        // TODO: Implementar registro con backend
        showSuccess("¡Cuenta creada exitosamente!")
        navigateToLogin()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
