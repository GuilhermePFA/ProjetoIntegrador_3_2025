package com.example.pi_03_equipe_01

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pi_03_equipe_01.databinding.ActivityRiskBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Risk : AppCompatActivity() {

    private lateinit var binding: ActivityRiskBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var imageUri: Uri? = null
    private var imageUrlFirebase: String? = null
    private lateinit var imageFile: File

    private val PERMISSION_REQUEST_LOCATION = 100
    private val PERMISSION_REQUEST_CAMERA = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.riskSent.isEnabled = false

        drawerLayout = binding.main
        navigationView = binding.navView
        binding.menuButton.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.root.windowToken, 0)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_history -> startActivity(Intent(this, History::class.java)).also { finish() }
                R.id.nav_risk -> {}
                R.id.nav_sair -> finishAffinity()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.riskLocEditText.setOnClickListener {
            requestLocation()
        }

        binding.riskAnexEditIcon.setOnClickListener {
            val opts = arrayOf("Tirar foto", "Escolher da galeria")
            AlertDialog.Builder(this)
                .setTitle("Selecionar imagem")
                .setItems(opts) { _, which ->
                    if (which == 0) checkCameraPermission() else openGallery()
                }
                .show()
        }

        binding.riskSent.setOnClickListener { view ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(view, "Usuário não autenticado!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                    .show()
                return@setOnClickListener
            }

            val pic = imageUrlFirebase ?: ""
            val loc = binding.riskLocEditText.text.toString()
            val tipo = binding.riskTypeEditText.text.toString()
            val desc = binding.riskDescEditText.text.toString()

            if (pic.isBlank() || loc.isBlank() || tipo.isBlank() || desc.isBlank()) {
                Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                    .show()
                return@setOnClickListener
            }

            val now = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
            val riskId = (10000..99999).random().toString()

            val db = FirebaseDatabase.getInstance().getReference("risk")
            val data = mapOf(
                "riskID" to riskId,
                "created_at" to now,
                "created_by_userID" to uid,
                "picture" to pic,
                "latitude" to userLatitude,
                "longitude" to userLongitude,
                "title" to tipo,
                "description" to desc,
                "status" to "NAO_INICIADO"
            )

            db.child(riskId).setValue(data)
                .addOnSuccessListener {
                    Snackbar.make(view, "Risco salvo com ID $riskId!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                        .show()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(view, "Falha ao salvar: ${e.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                        .show()
                }
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
            return
        }

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    updateLocationFields(loc)
                } else {
                    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { current: Location? ->
                            if (current != null) updateLocationFields(current)
                            else Toast.makeText(this, "Localização não encontrada", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateLocationFields(loc: Location) {
        userLatitude = loc.latitude
        userLongitude = loc.longitude
        binding.riskLocEditText.text = "Lat: $userLatitude, Long: $userLongitude"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        }
        if (requestCode == PERMISSION_REQUEST_CAMERA &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            if (intent.resolveActivity(packageManager) != null) {
                try {
                    imageFile = File.createTempFile("foto_", ".jpg", cacheDir)
                    imageUri = FileProvider.getUriForFile(this, "$packageName.provider", imageFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    cameraLauncher.launch(intent)
                } catch (e: IOException) {
                    Toast.makeText(this, "Erro ao criar arquivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.riskAnexEditIcon.setImageURI(imageUri)
            uploadImageToFirebase(imageUri!!)
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.riskAnexEditIcon.setImageURI(it)
            uploadImageToFirebase(it)
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val timestamp = System.currentTimeMillis()
        val ref = FirebaseStorage.getInstance().reference.child("riskImages/$timestamp.jpg")

        Log.d("UploadImage", "URI: $uri")

        ref.putFile(uri)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro no upload: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("UploadImage", "Erro no upload", e)
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                ref.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageUrlFirebase = task.result.toString()
                    Toast.makeText(this, "Imagem enviada com sucesso", Toast.LENGTH_SHORT).show()
                    binding.riskSent.isEnabled = true
                } else {
                    Toast.makeText(this, "Erro ao obter URL: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    Log.e("UploadImage", "Erro ao obter URL", task.exception)
                }
            }
    }
}
