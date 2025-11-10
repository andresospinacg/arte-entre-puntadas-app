package com.app_arte_entre_puntadas.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.models.ProductWithCategory
import com.bumptech.glide.Glide

/**
 * Adapter para mostrar productos en RecyclerView
 */
class ProductAdapter(
    private val isAdminMode: Boolean = false,
    private val onProductClick: (ProductWithCategory) -> Unit,
    private val onEditClick: ((ProductWithCategory) -> Unit)? = null,
    private val onDeleteClick: ((ProductWithCategory) -> Unit)? = null,
    private val onToggleAvailability: ((ProductWithCategory, Boolean) -> Unit)? = null
) : ListAdapter<ProductWithCategory, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvProductStock: TextView = itemView.findViewById(R.id.tvProductStock)
        private val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val btnEdit: ImageButton? = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton? = itemView.findViewById(R.id.btnDelete)
        private val tvAvailability: TextView? = itemView.findViewById(R.id.tvAvailability)
        
        fun bind(product: ProductWithCategory) {
            tvProductName.text = product.name
            tvProductPrice.text = "$${String.format("%,.0f", product.price)}"
            tvProductStock.text = "Stock: ${product.stock}"
            tvProductCategory.text = product.categoryName ?: "Sin categoría"
            
            // Cargar imagen con Glide
            if (!product.imageUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_camera)
                    .error(R.drawable.ic_camera)
                    .centerCrop()
                    .into(ivProductImage)
            } else {
                ivProductImage.setImageResource(R.drawable.ic_camera)
            }
            
            // Indicador de disponibilidad
            tvAvailability?.apply {
                visibility = View.VISIBLE
                text = if (product.isAvailable) "Disponible" else "No disponible"
                setTextColor(
                    if (product.isAvailable) 
                        itemView.context.getColor(android.R.color.holo_green_dark)
                    else 
                        itemView.context.getColor(android.R.color.holo_red_dark)
                )
                
                // Click para cambiar disponibilidad (solo admin)
                if (isAdminMode) {
                    setOnClickListener {
                        onToggleAvailability?.invoke(product, !product.isAvailable)
                    }
                }
            }
            
            // Click en el item completo
            itemView.setOnClickListener {
                onProductClick(product)
            }
            
            // Botones de admin
            if (isAdminMode) {
                btnEdit?.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        onEditClick?.invoke(product)
                    }
                }
                
                btnDelete?.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        onDeleteClick?.invoke(product)
                    }
                }
            } else {
                btnEdit?.visibility = View.GONE
                btnDelete?.visibility = View.GONE
            }
        }
    }
    
    private class ProductDiffCallback : DiffUtil.ItemCallback<ProductWithCategory>() {
        override fun areItemsTheSame(
            oldItem: ProductWithCategory,
            newItem: ProductWithCategory
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: ProductWithCategory,
            newItem: ProductWithCategory
        ): Boolean {
            return oldItem == newItem
        }
    }
}
