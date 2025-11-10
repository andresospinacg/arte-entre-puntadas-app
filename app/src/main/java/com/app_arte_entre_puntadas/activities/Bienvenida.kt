package com.app_arte_entre_puntadas.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.app_arte_entre_puntadas.MainActivity
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.adapters.BienvenidaPagerAdapter

class Bienvenida : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var ivClose: ImageView
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences

    private val totalPages = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        // Verificar si ya se mostró la bienvenida
        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val hasSeenWelcome = sharedPreferences.getBoolean("has_seen_welcome", false)
        if (hasSeenWelcome) {
            navigateToMain()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupViewPager()
        setupDots()
        setupListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btn_next)
        tvSkip = findViewById(R.id.tv_skip)
        ivClose = findViewById(R.id.iv_close)
        dotsIndicator = findViewById(R.id.dotsIndicator)
    }

    private fun setupViewPager() {
        val adapter = BienvenidaPagerAdapter()
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUI(position)
                updateDots(position)
            }
        })
    }

    private fun setupDots() {
        val dots = arrayOfNulls<View>(totalPages)
        for (i in 0 until totalPages) {
            dots[i] = View(this).apply {
                val params = LinearLayout.LayoutParams(12, 12).apply {
                    setMargins(8, 0, 8, 0)
                }
                layoutParams = params
                setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel)
            }
            dotsIndicator.addView(dots[i])
        }
        updateDots(0)
    }

    private fun updateDots(currentPage: Int) {
        for (i in 0 until totalPages) {
            val dot = dotsIndicator.getChildAt(i)
            if (i == currentPage) {
                dot?.alpha = 1f
                dot?.scaleX = 1.2f
                dot?.scaleY = 1.2f
            } else {
                dot?.alpha = 0.5f
                dot?.scaleX = 1f
                dot?.scaleY = 1f
            }
        }
    }

    private fun updateUI(position: Int) {
        // Cambiar texto del botón en la última página
        btnNext.text = if (position == totalPages - 1) {
            getString(R.string.start)
        } else {
            getString(R.string.next)
        }
    }

    private fun setupListeners() {
        btnNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < totalPages - 1) {
                viewPager.currentItem = currentItem + 1
            } else {
                completeWelcome()
            }
        }

        tvSkip.setOnClickListener {
            completeWelcome()
        }

        ivClose.setOnClickListener {
            completeWelcome()
        }
    }

    private fun completeWelcome() {
        // Guardar que ya se mostró la bienvenida
        sharedPreferences.edit().putBoolean("has_seen_welcome", true).apply()
        navigateToMain()
    }

    private fun navigateToMain() {
        // Navegar a LoginActivity en lugar de MainActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}