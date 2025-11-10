package com.app_arte_entre_puntadas.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.app_arte_entre_puntadas.MainActivity
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.local.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoading: TextView
    private lateinit var cvLogo: CardView
    private lateinit var tvTitle: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val SPLASH_DURATION = 3000L // 3 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Inicializar vistas
        initViews()

        // Configurar progress bar
        setupProgressBar()

        // Iniciar animaciones
        startAnimations()

        // Iniciar carga
        startLoading()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvLoading = findViewById(R.id.tv_loading)
        cvLogo = findViewById(R.id.cv_logo)
        tvTitle = findViewById(R.id.tv_title)
    }

    private fun setupProgressBar() {
        progressBar.max = 100
        progressBar.progress = 0
    }

    private fun startAnimations() {
        // Animación de fade in para el logo
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 1000
        }
        cvLogo.startAnimation(fadeIn)
    }

    private fun startLoading() {
        Thread {
            val totalSteps = 100
            val stepDelay = SPLASH_DURATION / totalSteps // Calcula el delay por paso

            for (progress in 0..100) {
                // Actualizar en el hilo principal
                handler.post {
                    progressBar.progress = progress

                    // Actualizar texto de carga según el progreso
                    tvLoading.text = when (progress) {
                        in 0..30 -> "Cargando..."
                        in 31..60 -> "Preparando..."
                        in 61..90 -> "Casi listo..."
                        else -> "¡Completado!"
                    }
                }

                try {
                    Thread.sleep(stepDelay)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    break
                }
            }

            // Navegar después de completar
            handler.postDelayed({
                navigateToMainActivity()
            }, 300)

        }.start()
    }

    private fun navigateToMainActivity() {
        // Verificar si el usuario ya tiene sesión activa
        val sessionManager = SessionManager(this)
        
        val intent = when {
            // Si tiene sesión, ir directo a MainActivity
            sessionManager.isLoggedIn() -> {
                Intent(this, MainActivity::class.java)
            }
            // Si no tiene sesión pero ya vio la bienvenida, ir a Login
            getSharedPreferences("app_preferences", MODE_PRIVATE)
                .getBoolean("has_seen_welcome", false) -> {
                Intent(this, LoginActivity::class.java)
            }
            // Si no ha visto la bienvenida, mostrarla
            else -> {
                Intent(this, Bienvenida::class.java)
            }
        }
        
        startActivity(intent)
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}