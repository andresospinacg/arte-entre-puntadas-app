package com.app_arte_entre_puntadas.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.app_arte_entre_puntadas.BuildConfig
import com.app_arte_entre_puntadas.MainActivity
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

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
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInClient: GoogleSignInClient
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_login)

        authRepository = AuthRepository(this)
        setupGoogleSignIn()
        initializeViews()
        setupListeners()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
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
    
    private fun handleGoogleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: throw Exception("No se pudo obtener el token de Google")
            
            lifecycleScope.launch {
                btnGoogle.isEnabled = false
                val result = authRepository.loginWithGoogle(idToken)
                
                runOnUiThread {
                    btnGoogle.isEnabled = true
                    result.onSuccess { profile ->
                        Toast.makeText(
                            this@LoginActivity, 
                            "¡Bienvenido ${profile.fullName}!", 
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToMain()
                    }.onFailure { error ->
                        showError(error.message ?: "Error al iniciar sesión con Google")
                    }
                }
            }
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Error en Google Sign-In: código ${e.statusCode}", e)
            showError("Error al iniciar sesión con Google. Por favor, intenta de nuevo.")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error procesando resultado de Google", e)
            showError(e.message ?: "Error al iniciar sesión con Google")
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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email inválido")
            return
        }

        // Deshabilitar botón mientras se procesa
        btnLogin.isEnabled = false
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()

        // Autenticar con Supabase
        lifecycleScope.launch {
            val result = authRepository.login(email, password)
            
            runOnUiThread {
                btnLogin.isEnabled = true
                
                result.onSuccess { profile ->
                    // Guardar preferencia de recordar
                    if (cbRemember.isChecked) {
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        sharedPref.edit().putString("email", email).apply()
                    }
                    
                    Toast.makeText(this@LoginActivity, "¡Bienvenido ${profile.fullName}!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }.onFailure { error ->
                    showError(error.message ?: "Error al iniciar sesión")
                }
            }
        }
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
