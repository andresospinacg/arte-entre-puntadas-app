package com.app_arte_entre_puntadas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app_arte_entre_puntadas.R
import android.widget.LinearLayout

class BienvenidaPagerAdapter : RecyclerView.Adapter<BienvenidaPagerAdapter.BienvenidaViewHolder>() {

    private val pages = listOf(
        R.layout.page_bienvenida,
        R.layout.page_productos,
        R.layout.page_personalizado,
        R.layout.page_calidad
    )

    inner class BienvenidaViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BienvenidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(pages[viewType], parent, false)
        return BienvenidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BienvenidaViewHolder, position: Int) {
        // Los layouts ya están inflados, no hay necesidad de configurar nada adicional
    }

    override fun getItemCount(): Int = pages.size

    override fun getItemViewType(position: Int): Int = position
}
