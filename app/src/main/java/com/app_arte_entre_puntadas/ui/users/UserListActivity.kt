package com.app_arte_entre_puntadas.ui.users

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app_arte_entre_puntadas.R
import com.app_arte_entre_puntadas.data.models.Location
import com.app_arte_entre_puntadas.data.models.User
import com.app_arte_entre_puntadas.databinding.ActivityUserListBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

/**
 * Activity para listar y gestionar usuarios
 */
class UserListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUserListBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
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
                    "Se requieren permisos de ubicación para esta función",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupObservers()
        setupSwipeRefresh()
        
        // Inicializar cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Gestión de Usuarios"
    }
    
    private fun setupRecyclerView() {
        adapter = UserAdapter(
            onUserClick = { user ->
                showUserDetails(user)
            },
            onEditClick = { user ->
                openUserForm(user.id)
            },
            onDeleteClick = { user ->
                confirmDeleteUser(user)
            }
        )
        
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }
    
    private fun setupFab() {
        // Solo los administradores pueden crear usuarios
        binding.fabAddUser.setOnClickListener {
            openUserForm()
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUsers()
        }
    }
    
    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            binding.swipeRefresh.isRefreshing = false
            
            when (state) {
                is UserUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvUsers.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.GONE
                }
                is UserUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvUsers.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    adapter.submitList(state.users)
                }
                is UserUiState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvUsers.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = "No hay usuarios registrados"
                }
                is UserUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvUsers.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = state.message
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_list, menu)
        
        // Configurar SearchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchUsers(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.loadUsers()
                }
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter_all -> {
                viewModel.loadUsers()
                true
            }
            R.id.action_filter_admins -> {
                viewModel.filterByRole("admin")
                true
            }
            R.id.action_filter_clients -> {
                viewModel.filterByRole("client")
                true
            }
            R.id.action_filter_stores -> {
                viewModel.loadUsersWithStore()
                true
            }
            R.id.action_find_nearby -> {
                findNearbyUsers()
                true
            }
            R.id.action_refresh -> {
                viewModel.loadUsers()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun openUserForm(userId: String? = null) {
        val intent = Intent(this, UserFormActivity::class.java)
        userId?.let {
            intent.putExtra("USER_ID", it)
        }
        startActivity(intent)
    }
    
    private fun showUserDetails(user: User) {
        val details = buildString {
            append("Nombre: ${user.fullName}\n")
            user.phone?.let { append("Teléfono: $it\n") }
            append("Rol: ${if (user.isAdmin()) "Administrador" else "Cliente"}\n")
            
            if (user.hasLocation()) {
                append("\n📍 Ubicación Personal:\n")
                user.address?.let { append("Dirección: $it\n") }
                user.city?.let { append("Ciudad: $it\n") }
                user.country?.let { append("País: $it\n") }
            }
            
            if (user.hasStore()) {
                append("\n🏪 Tienda:\n")
                append("Nombre: ${user.storeName}\n")
                user.storeDescription?.let { append("Descripción: $it\n") }
                user.storeAddress?.let { append("Dirección: $it\n") }
                user.storePhone?.let { append("Teléfono: $it\n") }
            }
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Detalles del Usuario")
            .setMessage(details)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Editar") { _, _ ->
                openUserForm(user.id)
            }
            .show()
    }
    
    private fun confirmDeleteUser(user: User) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Está seguro de que desea eliminar a ${user.fullName}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteUser(user.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun findNearbyUsers() {
        if (checkLocationPermissions()) {
            getCurrentLocation()
        } else {
            requestLocationPermissions()
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
                    val latitude = location.latitude
                    val longitude = location.longitude
                    
                    // Obtener dirección
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        val address = addresses?.firstOrNull()
                        
                        val loc = Location(
                            latitude = latitude,
                            longitude = longitude,
                            address = address?.getAddressLine(0),
                            city = address?.locality,
                            country = address?.countryName
                        )
                        
                        viewModel.setCurrentLocation(loc)
                        
                        // Mostrar diálogo para seleccionar radio
                        showRadiusDialog(latitude, longitude)
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
                        "No se pudo obtener la ubicación actual",
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
    
    private fun showRadiusDialog(latitude: Double, longitude: Double) {
        val options = arrayOf("1 km", "5 km", "10 km", "20 km", "50 km")
        val radiusValues = arrayOf(1.0, 5.0, 10.0, 20.0, 50.0)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar Radio de Búsqueda")
            .setItems(options) { _, which ->
                val radius = radiusValues[which]
                
                // Preguntar si buscar usuarios o tiendas
                MaterialAlertDialogBuilder(this)
                    .setTitle("¿Qué desea buscar?")
                    .setItems(arrayOf("Usuarios", "Tiendas")) { _, choice ->
                        if (choice == 0) {
                            viewModel.findNearbyUsers(latitude, longitude, radius)
                        } else {
                            viewModel.findNearbyStores(latitude, longitude, radius)
                        }
                    }
                    .show()
            }
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadUsers()
    }
}
