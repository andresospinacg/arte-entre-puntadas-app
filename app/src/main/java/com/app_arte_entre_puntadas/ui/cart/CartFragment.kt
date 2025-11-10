package com.app_arte_entre_puntadas.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import com.app_arte_entre_puntadas.activities.LoginActivity
import com.app_arte_entre_puntadas.data.local.SessionManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.databinding.FragmentCartBinding

/**
 * Fragment para mostrar el carrito de compras
 */
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[CartViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        // Si no hay usuario logueado, enviar al Login
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncreaseClick = { item ->
                viewModel.updateQuantity(item.id, item.quantity + 1)
            },
            onDecreaseClick = { item ->
                if (item.quantity > 1) {
                    viewModel.updateQuantity(item.id, item.quantity - 1)
                } else {
                    // Si la cantidad es 1, mostrar confirmación de eliminación
                    showRemoveItemDialog(item.id, item.productName)
                }
            },
            onRemoveClick = { item ->
                showRemoveItemDialog(item.id, item.productName)
            }
        )
        
        binding.rvCartItems.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        // Botón Vaciar Carrito
        binding.btnClearCart.setOnClickListener {
            showClearCartDialog()
        }
        
        // Botón Realizar Pedido
        binding.btnCheckout.setOnClickListener {
            // TODO: Implementar flujo de checkout (crear orden)
            Toast.makeText(
                requireContext(),
                "Función de checkout - En desarrollo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewModel() {
        // Observar items del carrito
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
            
            // Mostrar vista vacía si no hay items
            if (items.isEmpty()) {
                binding.layoutEmptyCart.visibility = View.VISIBLE
                binding.rvCartItems.visibility = View.GONE
                binding.cardCartSummary.visibility = View.GONE
            } else {
                binding.layoutEmptyCart.visibility = View.GONE
                binding.rvCartItems.visibility = View.VISIBLE
                binding.cardCartSummary.visibility = View.VISIBLE
            }
        }
        
        // Observar total del carrito
        viewModel.cartTotal.observe(viewLifecycleOwner) { total ->
            val formattedTotal = "$${String.format("%,.0f", total)}"
            binding.tvTotal.text = formattedTotal
            binding.tvSubtotal.text = formattedTotal
        }
        
        // Observar cantidad de items
        viewModel.cartItemCount.observe(viewLifecycleOwner) { count ->
            binding.tvItemCount.text = if (count == 1) {
                "1 item"
            } else {
                "$count items"
            }
        }
        
        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observar éxito en operaciones
        viewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación para eliminar un item
     */
    private fun showRemoveItemDialog(cartItemId: String, productName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Desea eliminar \"$productName\" del carrito?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.removeFromCart(cartItemId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra un diálogo de confirmación para vaciar el carrito
     */
    private fun showClearCartDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Vaciar carrito")
            .setMessage("¿Está seguro que desea vaciar completamente el carrito?")
            .setPositiveButton("Vaciar") { _, _ ->
                viewModel.clearCart()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CartFragment()
    }
}
