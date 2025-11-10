package com.app_arte_entre_puntadas.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.models.ProductWithCategory
import com.app_arte_entre_puntadas.databinding.FragmentProductDetailBinding
import com.app_arte_entre_puntadas.data.local.SessionManager
import com.app_arte_entre_puntadas.ui.cart.CartViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar detalle completo de un producto
 */
class ProductDetailFragment : Fragment() {
    
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    
    private var productId: String? = null
    private var product: ProductWithCategory? = null
    private var selectedQuantity = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString("productId")
        sessionManager = SessionManager(requireContext())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupButtons()
        loadProductDetails()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupButtons() {
        val isAdmin = sessionManager.isAdmin()
        
        if (isAdmin) {
            // Mostrar botón de editar para admin
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnEdit.setOnClickListener {
                navigateToEditProduct()
            }
        } else {
            // Mostrar selector de cantidad y botón agregar al carrito para clientes
            binding.llQuantitySelector.visibility = View.VISIBLE
            binding.btnAddToCart.visibility = View.VISIBLE
            
            binding.btnDecrease.setOnClickListener {
                if (selectedQuantity > 1) {
                    selectedQuantity--
                    updateQuantityDisplay()
                }
            }
            
            binding.btnIncrease.setOnClickListener {
                product?.let { prod ->
                    if (selectedQuantity < prod.stock) {
                        selectedQuantity++
                        updateQuantityDisplay()
                    } else {
                        Toast.makeText(context, "No hay más stock disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            binding.btnAddToCart.setOnClickListener {
                addToCart()
            }
        }
    }
    
    private fun loadProductDetails() {
        productId?.let { id ->
            binding.progressBar.visibility = View.VISIBLE
            
            viewModel.getProductById(id) { productDetail ->
                binding.progressBar.visibility = View.GONE
                product = productDetail
                displayProductDetails(productDetail)
            }
        } ?: run {
            Toast.makeText(context, "Error: ID de producto no válido", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun displayProductDetails(product: ProductWithCategory) {
        with(binding) {
            // Nombre
            tvProductName.text = product.name
            
            // Categoría
            tvCategory.text = product.categoryName ?: "Sin categoría"
            
            // Precio
            tvProductPrice.text = product.getFormattedPrice()
            
            // Stock
            tvStock.text = "Stock: ${product.stock}"
            
            // Disponibilidad
            tvAvailability.text = if (product.isAvailable) {
                tvAvailability.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                "Disponible"
            } else {
                tvAvailability.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                "No disponible"
            }
            
            // Descripción
            tvProductDescription.text = product.description ?: "Sin descripción"
            
            // Imagen
            if (!product.imageUrl.isNullOrEmpty()) {
                Glide.with(this@ProductDetailFragment)
                    .load(product.imageUrl)
                    .into(ivProductImage)
            }
            
            // Deshabilitar agregar al carrito si no está disponible o no hay stock
            if (!sessionManager.isAdmin()) {
                btnAddToCart.isEnabled = product.isAvailable && product.stock > 0
                llQuantitySelector.visibility = if (product.isAvailable && product.stock > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }
    
    private fun addToCart() {
        product?.let { prod ->
            if (!prod.isAvailable) {
                Toast.makeText(context, "Este producto no está disponible", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (prod.stock < selectedQuantity) {
                Toast.makeText(context, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Agregar al carrito usando CartViewModel
            lifecycleScope.launch {
                cartViewModel.addToCart(prod.id, selectedQuantity)
            }
            
            // Observar el resultado
            cartViewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
                message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    // Resetear la cantidad
                    selectedQuantity = 1
                    updateQuantityDisplay()
                }
            }
            
            cartViewModel.error.observe(viewLifecycleOwner) { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * Actualiza el display de la cantidad seleccionada
     */
    private fun updateQuantityDisplay() {
        binding.tvQuantity.text = selectedQuantity.toString()
    }
    
    private fun navigateToEditProduct() {
        productId?.let { id ->
            val fragment = ProductFormFragment().apply {
                arguments = Bundle().apply {
                    putString("productId", id)
                }
            }
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(productId: String): ProductDetailFragment {
            return ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("productId", productId)
                }
            }
        }
    }
}
