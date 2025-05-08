package com.example.pi_03_equipe_01

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pi_03_equipe_01.databinding.ActivityRiskBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class Risk : AppCompatActivity() {

    private lateinit var binding: ActivityRiskBinding
    private lateinit var drawerLayout: DrawerLayout

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    private var imageBase64Firebase: String? = null

    private val PERM_LOCATION = 100
    private val PERM_CAMERA = 200
    private val PERM_GALLERY = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.riskSent.isEnabled = false

        drawerLayout = binding.main
        binding.menuButton.setOnClickListener {
            hideKeyboard()
            drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_history -> startActivity(Intent(this, History::class.java)).also { finish() }
                R.id.nav_sair -> finishAffinity()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.riskLocEditText.apply {
            inputType = InputType.TYPE_NULL
            setOnClickListener { requestLocation(fusedLocationClient) }
        }

        binding.riskAnexEditIcon.setOnClickListener {
            hideKeyboard()
            AlertDialog.Builder(this)
                .setTitle("Selecionar imagem")
                .setItems(arrayOf("Tirar foto", "Escolher da galeria")) { _, which ->
                    if (which == 0) checkCameraPermission() else checkGalleryPermission()
                }
                .show()
        }

        binding.riskSent.setOnClickListener { view ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(view, "Usuário não autenticado!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pic = imageBase64Firebase ?: ""
            val loc = binding.riskLocEditText.text.toString()
            val tipo = binding.riskTypeEditText.text.toString()
            val desc = binding.riskDescEditText.text.toString()

            if (pic.isBlank() || loc.isBlank() || tipo.isBlank() || desc.isBlank()) {
                Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))
                .format(java.util.Date())
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
                    Snackbar.make(view, "Risco salvo com ID $riskId!", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(view, "Falha ao salvar: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun requestLocation(fused: com.google.android.gms.location.FusedLocationProviderClient) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERM_LOCATION)
            return
        }
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }
        fused.lastLocation.addOnSuccessListener { loc: Location? ->
            loc?.let {
                userLatitude = it.latitude
                userLongitude = it.longitude
                binding.riskLocEditText.text = "Lat: ${it.latitude}, Long: ${it.longitude}"
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, perms: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(requestCode, perms, res)
        when (requestCode) {
            PERM_CAMERA -> if (res.firstOrNull() == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null)
            PERM_GALLERY -> if (res.firstOrNull() == PackageManager.PERMISSION_GRANTED) galleryLauncher.launch("image/*")
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERM_CAMERA)
        } else {
            cameraLauncher.launch(null)
        }
    }

    private fun checkGalleryPermission() {
        val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, readPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(readPerm), PERM_GALLERY)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        bmp?.let {
            binding.riskAnexEditIcon.setImageBitmap(it)
            imageBase64Firebase = bitmapToBase64(it)
            Toast.makeText(this, "Imagem convertida para Base64", Toast.LENGTH_SHORT).show()
            binding.riskSent.isEnabled = true
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.riskAnexEditIcon.setImageBitmap(bitmap)
            imageBase64Firebase = bitmapToBase64(bitmap)
            Toast.makeText(this, "Imagem da galeria convertida para Base64", Toast.LENGTH_SHORT).show()
            binding.riskSent.isEnabled = true
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}