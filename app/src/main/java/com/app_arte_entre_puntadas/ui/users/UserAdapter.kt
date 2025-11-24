package com.app_arte_entre_puntadas.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.models.User
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip

/**
 * Adaptador para la lista de usuarios
 */
class UserAdapter(
    private val onUserClick: (User) -> Unit,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onUserClick, onEditClick, onDeleteClick)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class UserViewHolder(
        itemView: View,
        private val onUserClick: (User) -> Unit,
        private val onEditClick: (User) -> Unit,
        private val onDeleteClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_user_phone)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_user_address)
        private val chipRole: Chip = itemView.findViewById(R.id.chip_user_role)
        private val chipStore: Chip = itemView.findViewById(R.id.chip_has_store)
        private val chipLocation: Chip = itemView.findViewById(R.id.chip_has_location)
        private val btnEdit: View = itemView.findViewById(R.id.btn_edit_user)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete_user)
        
        fun bind(user: User) {
            // Nombre
            tvName.text = user.fullName
            
            // Teléfono
            if (!user.phone.isNullOrEmpty()) {
                tvPhone.visibility = View.VISIBLE
                tvPhone.text = user.phone
            } else {
                tvPhone.visibility = View.GONE
            }
            
            // Dirección
            if (!user.address.isNullOrEmpty()) {
                tvAddress.visibility = View.VISIBLE
                tvAddress.text = user.address
            } else {
                tvAddress.visibility = View.GONE
            }
            
            // Avatar
            if (!user.avatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
            }
            
            // Rol
            chipRole.text = if (user.isAdmin()) "Admin" else "Cliente"
            chipRole.setChipBackgroundColorResource(
                if (user.isAdmin()) R.color.chip_admin else R.color.chip_client
            )
            
            // Tiene tienda
            if (user.hasStore()) {
                chipStore.visibility = View.VISIBLE
                chipStore.text = "Tienda: ${user.storeName}"
            } else {
                chipStore.visibility = View.GONE
            }
            
            // Tiene ubicación
            chipLocation.visibility = if (user.hasLocation()) View.VISIBLE else View.GONE
            
            // Listeners
            itemView.setOnClickListener { onUserClick(user) }
            btnEdit.setOnClickListener { onEditClick(user) }
            btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }
    
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
