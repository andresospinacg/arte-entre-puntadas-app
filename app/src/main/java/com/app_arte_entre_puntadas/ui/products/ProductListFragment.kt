package com.app_arte_entre_puntadas.ui.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.local.SessionManager
import com.app_arte_entre_puntadas.databinding.FragmentProductListBinding
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment para mostrar la lista de productos
 */
class ProductListFragment : Fragment() {
    
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ProductAdapter
    
    private var isAdmin = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        isAdmin = sessionManager.isAdmin()
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        setupFab()
        observeViewModel()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            isAdminMode = isAdmin,
            onProductClick = { product ->
                // Navegar a detalle del producto
                val fragment = ProductDetailFragment.newInstance(product.id)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onEditClick = if (isAdmin) { product ->
                // Navegar a editar producto
                val fragment = ProductFormFragment().apply {
                    arguments = Bundle().apply {
                        putString("productId", product.id)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            } else null,
            onDeleteClick = if (isAdmin) { product ->
                showDeleteConfirmation(product.id, product.name)
            } else null,
            onToggleAvailability = if (isAdmin) { product, newAvailability ->
                viewModel.toggleAvailability(product.id, newAvailability)
            } else null
        )
        
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProductListFragment.adapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProducts()
        }
    }
    
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.searchProducts(query)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupFab() {
        if (isAdmin) {
            binding.fabAddProduct.visibility = View.VISIBLE
            binding.fabAddProduct.setOnClickListener {
                // Navegar a crear producto
                val fragment = ProductFormFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            binding.fabAddProduct.visibility = View.GONE
        }
    }
    
    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadProducts()
        }
    }
    
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            
            when (state) {
                is ProductUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvProducts.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                }
                
                is ProductUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                    
                    adapter.submitList(state.products)
                }
                
                is ProductUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvProducts.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.layoutError.visibility = View.GONE
                }
                
                is ProductUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvProducts.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
        
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }
    
    private fun showDeleteConfirmation(productId: String, productName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de eliminar '$productName'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteProduct(productId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
