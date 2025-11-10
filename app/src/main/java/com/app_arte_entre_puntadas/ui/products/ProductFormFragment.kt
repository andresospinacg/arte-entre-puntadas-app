package com.app_arte_entre_puntadas.ui.products

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.models.Category
import com.app_arte_entre_puntadas.data.models.CreateProductDTO
import com.app_arte_entre_puntadas.data.models.UpdateProductDTO
import com.app_arte_entre_puntadas.databinding.FragmentProductFormBinding
import com.bumptech.glide.Glide
import java.io.File

/**
 * Fragment para crear/editar productos
 */
class ProductFormFragment : Fragment() {
    
    private var _binding: FragmentProductFormBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()
    
    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Int? = null
    private var imageUrl: String? = null
    private var selectedImageUri: Uri? = null
    
    // Para edición
    private var productId: String? = null
    private var isEditMode = false
    
    // Selector de imágenes
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProductPreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(uri)
                .into(binding.ivProductPreview)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Obtener productId de arguments si es modo edición
        productId = arguments?.getString("productId")
        isEditMode = productId != null
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupListeners()
        observeViewModel()
        
        if (isEditMode) {
            binding.toolbar.title = getString(R.string.edit_product)
            loadProductData()
        }
    }
    
    private fun loadProductData() {
        productId?.let { id ->
            viewModel.getProductById(id) { product ->
                // Llenar los campos con los datos del producto
                binding.etProductName.setText(product.name)
                binding.etProductDescription.setText(product.description)
                binding.etProductPrice.setText(product.price.toString())
                binding.etProductStock.setText(product.stock.toString())
                binding.switchAvailable.isChecked = product.isAvailable
                
                // Guardar la imagen URL
                imageUrl = product.imageUrl
                
                // Mostrar imagen si existe
                if (!product.imageUrl.isNullOrEmpty()) {
                    binding.ivProductPreview.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(product.imageUrl)
                        .into(binding.ivProductPreview)
                }
                
                // Seleccionar la categoría correcta en el dropdown
                product.categoryId?.let { categoryId ->
                    selectedCategoryId = categoryId
                    val categoryIndex = categories.indexOfFirst { it.id == categoryId }
                    if (categoryIndex >= 0) {
                        binding.actvCategory.setText(categories[categoryIndex].name, false)
                    }
                }
            }
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupListeners() {
        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                // Si hay una imagen seleccionada, primero subirla
                if (selectedImageUri != null) {
                    uploadImageAndSaveProduct()
                } else {
                    // Si no hay imagen nueva, guardar directamente
                    if (isEditMode) {
                        updateProduct()
                    } else {
                        createProduct()
                    }
                }
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            categories = cats
            setupCategoryDropdown()
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
            binding.btnCancel.isEnabled = !isLoading
        }
        
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }
    }
    
    private fun setupCategoryDropdown() {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categories[position].id
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        if (binding.etProductName.text.isNullOrBlank()) {
            binding.etProductName.error = "El nombre es requerido"
            isValid = false
        }
        
        if (binding.etProductPrice.text.isNullOrBlank()) {
            binding.etProductPrice.error = "El precio es requerido"
            isValid = false
        } else {
            try {
                val price = binding.etProductPrice.text.toString().toDouble()
                if (price <= 0) {
                    binding.etProductPrice.error = "El precio debe ser mayor a 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.etProductPrice.error = "Precio inválido"
                isValid = false
            }
        }
        
        if (binding.etProductStock.text.isNullOrBlank()) {
            binding.etProductStock.error = "El stock es requerido"
            isValid = false
        } else {
            try {
                val stock = binding.etProductStock.text.toString().toInt()
                if (stock < 0) {
                    binding.etProductStock.error = "El stock no puede ser negativo"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.etProductStock.error = "Stock inválido"
                isValid = false
            }
        }
        
        return isValid
    }
    
    /**
     * Sube la imagen y luego guarda el producto
     */
    private fun uploadImageAndSaveProduct() {
        selectedImageUri?.let { uri ->
            try {
                // Convertir URI a File
                val file = uriToFile(uri)
                
                // Subir imagen
                viewModel.uploadImage(file) { uploadedImageUrl ->
                    imageUrl = uploadedImageUrl
                    
                    // Guardar producto con la URL de la imagen
                    if (isEditMode) {
                        updateProduct()
                    } else {
                        createProduct()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al procesar imagen: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Convierte URI a File
     */
    private fun uriToFile(uri: Uri): File {
        val contentResolver = requireContext().contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
        
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
    
    private fun createProduct() {
        val productDTO = CreateProductDTO(
            categoryId = selectedCategoryId,
            name = binding.etProductName.text.toString().trim(),
            description = binding.etProductDescription.text?.toString()?.trim(),
            price = binding.etProductPrice.text.toString().toDouble(),
            stock = binding.etProductStock.text.toString().toInt(),
            imageUrl = imageUrl,
            isAvailable = binding.switchAvailable.isChecked
        )
        
        viewModel.createProduct(productDTO) {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun updateProduct() {
        val productDTO = UpdateProductDTO(
            categoryId = selectedCategoryId,
            name = binding.etProductName.text.toString().trim(),
            description = binding.etProductDescription.text?.toString()?.trim(),
            price = binding.etProductPrice.text.toString().toDouble(),
            stock = binding.etProductStock.text.toString().toInt(),
            imageUrl = imageUrl,
            isAvailable = binding.switchAvailable.isChecked
        )
        
        productId?.let { id ->
            viewModel.updateProduct(id, productDTO) {
                parentFragmentManager.popBackStack()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
