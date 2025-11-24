package com.app_arte_entre_puntadas.ui.users

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.models.CreateUserDTO
import com.app_arte_entre_puntadas.data.models.Location
import com.app_arte_entre_puntadas.data.models.UpdateUserDTO
import com.app_arte_entre_puntadas.data.models.User
import com.app_arte_entre_puntadas.databinding.ActivityUserFormBinding
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.Locale

/**
 * Activity para crear/editar usuarios con cámara y geolocalización
 */
class UserFormActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUserFormBinding
    private val viewModel: UserViewModel by viewModels()
    
    private var userId: String? = null
    private var isEditMode = false
    
    private var avatarUrl: String? = null
    private var storeImageUrl: String? = null
    
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var storeLatitude: Double? = null
    private var storeLongitude: Double? = null
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    // Para captura de foto
    private var currentPhotoUri: Uri? = null
    private var isCapturingForAvatar = true // true = avatar, false = store image
    
    // Launcher para captura de foto con cámara
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                val file = uriToFile(uri)
                uploadImage(file, isCapturingForAvatar)
            }
        }
    }
    
    // Launcher para selección de imagen de galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(it)
            uploadImage(file, isCapturingForAvatar)
        }
    }
    
    // Permisos de cámara
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(
                this,
                "Se requiere permiso de cámara",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Permisos de ubicación
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Se requieren permisos de ubicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userId = intent.getStringExtra("USER_ID")
        isEditMode = userId != null
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setupToolbar()
        setupListeners()
        setupObservers()
        
        // Mostrar campos de email/password solo en modo creación
        if (!isEditMode) {
            binding.tilEmail.visibility = View.VISIBLE
            binding.tilPassword.visibility = View.VISIBLE
        }
        
        if (isEditMode) {
            loadUserData()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) "Editar Usuario" else "Nuevo Usuario"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupListeners() {
        // Avatar
        binding.btnSelectAvatar.setOnClickListener {
            isCapturingForAvatar = true
            showImageSourceDialog()
        }
        
        // Ubicación del usuario
        binding.btnGetUserLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getCurrentLocation()
            } else {
                requestLocationPermissions()
            }
        }
        
        // Imagen de tienda
        binding.btnSelectStoreImage.setOnClickListener {
            isCapturingForAvatar = false
            showImageSourceDialog()
        }
        
        // Ubicación de tienda
        binding.btnGetStoreLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getStoreLocation()
            } else {
                requestLocationPermissions()
            }
        }
        
        // Switch de tienda
        binding.switchHasStore.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutStoreInfo.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Botones
        binding.btnCancel.setOnClickListener {
            finish()
        }
        
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                saveUser()
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
            binding.btnCancel.isEnabled = !isLoading
        }
        
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }
        
        viewModel.selectedUser.observe(this) { user ->
            user?.let {
                populateForm(it)
            }
        }
    }
    
    private fun loadUserData() {
        userId?.let { id ->
            viewModel.getUserById(id)
        }
    }
    
    private fun populateForm(user: User) {
        binding.etFullName.setText(user.fullName)
        binding.etPhone.setText(user.phone)
        binding.etAddress.setText(user.address)
        binding.etCity.setText(user.city)
        binding.etCountry.setText(user.country)
        
        // Rol
        binding.spinnerRole.setSelection(if (user.isAdmin()) 0 else 1)
        
        // Avatar
        avatarUrl = user.avatarUrl
        if (!user.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.ivAvatarPreview)
            binding.ivAvatarPreview.visibility = View.VISIBLE
        }
        
        // Ubicación
        userLatitude = user.latitude
        userLongitude = user.longitude
        if (user.hasLocation()) {
            binding.tvUserLocationStatus.text = "✓ Ubicación guardada"
            binding.tvUserLocationStatus.setTextColor(getColor(R.color.success))
        }
        
        // Tienda
        if (user.hasStore()) {
            binding.switchHasStore.isChecked = true
            binding.etStoreName.setText(user.storeName)
            binding.etStoreDescription.setText(user.storeDescription)
            binding.etStoreAddress.setText(user.storeAddress)
            binding.etStorePhone.setText(user.storePhone)
            
            storeImageUrl = user.storeImageUrl
            if (!user.storeImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.storeImageUrl)
                    .placeholder(R.drawable.ic_store_placeholder)
                    .into(binding.ivStorePreview)
                binding.ivStorePreview.visibility = View.VISIBLE
            }
            
            storeLatitude = user.storeLatitude
            storeLongitude = user.storeLongitude
            if (user.hasStoreLocation()) {
                binding.tvStoreLocationStatus.text = "✓ Ubicación de tienda guardada"
                binding.tvStoreLocationStatus.setTextColor(getColor(R.color.success))
            }
        }
    }
    
    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (checkCameraPermission()) {
                            openCamera()
                        } else {
                            requestCameraPermission()
                        }
                    }
                    1 -> openGallery()
                }
            }
            .show()
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    private fun openCamera() {
        try {
            val photoFile = File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                cacheDir
            )
            
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            
            currentPhotoUri?.let { uri ->
                takePictureLauncher.launch(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al abrir cámara: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
    
    private fun uploadImage(file: File?, isAvatar: Boolean) {
        if (file == null) {
            Toast.makeText(
                this,
                "Error: archivo no válido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        uploadImageInternal(file, isAvatar)
    }
    
    private fun uploadImageInternal(file: File, isAvatar: Boolean) {
        if (isAvatar) {
            viewModel.uploadAvatar(file) { url ->
                avatarUrl = url
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatarPreview)
                binding.ivAvatarPreview.visibility = View.VISIBLE
            }
        } else {
            viewModel.uploadStoreImage(file) { url ->
                storeImageUrl = url
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_store_placeholder)
                    .into(binding.ivStorePreview)
                binding.ivStorePreview.visibility = View.VISIBLE
            }
        }
    }
    
    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun getCurrentLocation() {
        if (!checkLocationPermissions()) {
            return
        }
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLatitude = location.latitude
                    userLongitude = location.longitude
                    
                    // Obtener dirección
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        val address = addresses?.firstOrNull()
                        
                        address?.let {
                            binding.etAddress.setText(it.getAddressLine(0))
                            binding.etCity.setText(it.locality)
                            binding.etCountry.setText(it.countryName)
                        }
                        
                        binding.tvUserLocationStatus.text = "✓ Ubicación obtenida"
                        binding.tvUserLocationStatus.setTextColor(getColor(R.color.success))
                        
                        Toast.makeText(
                            this,
                            "Ubicación obtenida correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Error al obtener dirección: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Error de permisos: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun getStoreLocation() {
        if (!checkLocationPermissions()) {
            return
        }
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    storeLatitude = location.latitude
                    storeLongitude = location.longitude
                    
                    // Obtener dirección
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        val address = addresses?.firstOrNull()
                        
                        address?.let {
                            binding.etStoreAddress.setText(it.getAddressLine(0))
                        }
                        
                        binding.tvStoreLocationStatus.text = "✓ Ubicación de tienda obtenida"
                        binding.tvStoreLocationStatus.setTextColor(getColor(R.color.success))
                        
                        Toast.makeText(
                            this,
                            "Ubicación de tienda obtenida",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Error al obtener dirección: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Error de permisos: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        if (binding.etFullName.text.isNullOrBlank()) {
            binding.etFullName.error = "El nombre es requerido"
            isValid = false
        }
        
        if (binding.switchHasStore.isChecked) {
            if (binding.etStoreName.text.isNullOrBlank()) {
                binding.etStoreName.error = "El nombre de la tienda es requerido"
                isValid = false
            }
        }
        
        return isValid
    }
    
    private fun saveUser() {
        val role = if (binding.spinnerRole.selectedItemPosition == 0) "admin" else "client"
        
        if (isEditMode) {
            val updateDTO = UpdateUserDTO(
                fullName = binding.etFullName.text.toString().trim(),
                phone = binding.etPhone.text?.toString()?.trim(),
                role = role,
                avatarUrl = avatarUrl,
                latitude = userLatitude,
                longitude = userLongitude,
                address = binding.etAddress.text?.toString()?.trim(),
                city = binding.etCity.text?.toString()?.trim(),
                country = binding.etCountry.text?.toString()?.trim(),
                storeName = if (binding.switchHasStore.isChecked) 
                    binding.etStoreName.text?.toString()?.trim() else null,
                storeDescription = if (binding.switchHasStore.isChecked)
                    binding.etStoreDescription.text?.toString()?.trim() else null,
                storeLatitude = if (binding.switchHasStore.isChecked) storeLatitude else null,
                storeLongitude = if (binding.switchHasStore.isChecked) storeLongitude else null,
                storeAddress = if (binding.switchHasStore.isChecked)
                    binding.etStoreAddress.text?.toString()?.trim() else null,
                storePhone = if (binding.switchHasStore.isChecked)
                    binding.etStorePhone.text?.toString()?.trim() else null,
                storeImageUrl = if (binding.switchHasStore.isChecked) storeImageUrl else null
            )
            
            userId?.let { id ->
                viewModel.updateUser(id, updateDTO) {
                    finish()
                }
            }
        } else {
            // Validar email y contraseña para crear usuario
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isEmpty()) {
                binding.tilEmail.error = "Email requerido"
                return
            }
            
            if (password.isEmpty() || password.length < 6) {
                binding.tilPassword.error = "Contraseña requerida (mínimo 6 caracteres)"
                return
            }
            
            val createDTO = CreateUserDTO(
                fullName = binding.etFullName.text.toString().trim(),
                phone = binding.etPhone.text?.toString()?.trim(),
                role = role,
                avatarUrl = avatarUrl,
                latitude = userLatitude,
                longitude = userLongitude,
                address = binding.etAddress.text?.toString()?.trim(),
                city = binding.etCity.text?.toString()?.trim(),
                country = binding.etCountry.text?.toString()?.trim(),
                storeName = if (binding.switchHasStore.isChecked)
                    binding.etStoreName.text?.toString()?.trim() else null,
                storeDescription = if (binding.switchHasStore.isChecked)
                    binding.etStoreDescription.text?.toString()?.trim() else null,
                storeLatitude = if (binding.switchHasStore.isChecked) storeLatitude else null,
                storeLongitude = if (binding.switchHasStore.isChecked) storeLongitude else null,
                storeAddress = if (binding.switchHasStore.isChecked)
                    binding.etStoreAddress.text?.toString()?.trim() else null,
                storePhone = if (binding.switchHasStore.isChecked)
                    binding.etStorePhone.text?.toString()?.trim() else null,
                storeImageUrl = if (binding.switchHasStore.isChecked) storeImageUrl else null,
                email = email,
                password = password
            )
            
            viewModel.createUser(createDTO) {
                finish()
            }
        }
    }
    
    private fun uriToFile(uri: Uri?): File? {
        if (uri == null) return null
        
        return try {
            val contentResolver = contentResolver
            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
            
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al procesar imagen: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            null
        }
    }
}
