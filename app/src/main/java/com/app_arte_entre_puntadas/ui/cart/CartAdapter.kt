package com.app_arte_entre_puntadas.ui.cart

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
import com.app_arte_entre_puntadas.data.models.CartItemDetail
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

/**
 * Adapter para mostrar los items del carrito
 */
class CartAdapter(
    private val onIncreaseClick: (CartItemDetail) -> Unit,
    private val onDecreaseClick: (CartItemDetail) -> Unit,
    private val onRemoveClick: (CartItemDetail) -> Unit
) : ListAdapter<CartItemDetail, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        private val btnIncrease: MaterialButton = itemView.findViewById(R.id.btnIncrease)
        private val btnDecrease: MaterialButton = itemView.findViewById(R.id.btnDecrease)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(item: CartItemDetail) {
            // Nombre del producto
            tvProductName.text = item.productName
            
            // Precio unitario
            tvProductPrice.text = "$${String.format("%,.0f", item.price)} c/u"
            
            // Cantidad
            tvQuantity.text = item.quantity.toString()
            
            // Subtotal
            tvSubtotal.text = "$${String.format("%,.0f", item.subtotal)}"
            
            // Stock disponible
            tvStock.text = "Disponibles: ${item.stock}"
            
            // Cargar imagen
            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .into(ivProductImage)
            } else {
                 // Fallback a un drawable existente en el proyecto
                 ivProductImage.setImageResource(R.drawable.ic_back)
            }
            
            // Deshabilitar botón de incrementar si se alcanzó el stock
            btnIncrease.isEnabled = item.quantity < item.stock && item.isAvailable
            
            // Deshabilitar si el producto no está disponible
            if (!item.isAvailable) {
                btnIncrease.isEnabled = false
                tvStock.text = "Producto no disponible"
                tvStock.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            }
            
            // Listeners
            btnIncrease.setOnClickListener {
                if (item.quantity < item.stock) {
                    onIncreaseClick(item)
                }
            }
            
            btnDecrease.setOnClickListener {
                onDecreaseClick(item)
            }
            
            btnRemove.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItemDetail>() {
        override fun areItemsTheSame(oldItem: CartItemDetail, newItem: CartItemDetail): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItemDetail, newItem: CartItemDetail): Boolean {
            return oldItem == newItem
        }
    }
}
