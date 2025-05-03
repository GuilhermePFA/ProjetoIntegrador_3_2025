package com.example.pi_03_equipe_01

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pi_03_equipe_01.databinding.ActivityRiskBinding
import com.google.android.gms.common.api.Api.Client
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class Risk : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityRiskBinding
    lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    lateinit var tvLatitude: TextView
    lateinit var tvLongitude: TextView
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        binding.riskLocEditText.setOnClickListener {
            getLocation()
        }

        binding.riskSent.setOnClickListener { view ->
            // pega o ID do usuário e tbm ve se ta logado
            //val currentUser = intent.getStringExtra("USER_ID") ?: return@setOnClickListener
            //val userId = currentUser
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            if (userId == null) {
                Snackbar.make(view, "Usuário não autenticado!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show()
                return@setOnClickListener
            }

            tvLatitude = findViewById(R.id.riskLocEditText)
            tvLongitude = findViewById(R.id.riskLocEditText)




            // forms do codigo
            val anexo = binding.riskAnexEdit.text.toString()
            val localizacao = binding.riskLocEditText.text.toString()
            val tipo = binding.riskTypeEditText.text.toString()
            val descricao = binding.riskDescEditText.text.toString()

            if (anexo.isEmpty() || localizacao.isEmpty() || tipo.isEmpty() || descricao.isEmpty()) {
                Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show()
                return@setOnClickListener
            }

            //faz a data funcionar (AMEM)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val now = sdf.format(Date())


            val riskId = generateFiveDigitId()

            // map do banco
            val db = FirebaseDatabase.getInstance().getReference("risk")
            val riskInfo = mapOf(
                "riskID" to riskId,
                "created_at" to now,
                "created_by_userID" to userId,
                "picture" to anexo,
                "latitude" to userLatitude,
                "longitude" to userLongitude,
                "title" to tipo,
                "description" to descricao,
                "status" to "NAO INICIADO",
            )

            db.child(riskId).setValue(riskInfo)
                .addOnSuccessListener {
                    Snackbar.make(view, "Risco salvo com ID $riskId!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.GREEN)
                        .show()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(view, "Falha ao salvar: ${e.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.RED)
                        .show()
                }
        }
        drawerLayout = findViewById(R.id.nav_risk)

        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val menuButton: TextView = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, History::class.java))
                    finish()
                }
                R.id.nav_risk -> {
                    startActivity(Intent(this, Risk::class.java))
                }
                R.id.nav_sair -> {
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Função para gerar um String numérico de exatamente 5 dígitos
    private fun generateFiveDigitId(): String {
        val number = Random.nextInt(10000, 100000) // [10000, 99999]
        return number.toString()

    }
    private fun getLocation() {
        Log.d("DEBUG_CHECK", "Permissão OK? ${checkedPermission()}")
        if (checkedPermission()) {

            if (isLocationEnable()) {
                Log.d("DEBUG_CHECK", "GPS ativo? ${isLocationEnable()}")

                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        Log.d("DEBUG_CHECK", "fusedLocationProviderClient está null? ${::fusedLocationProviderClient.isInitialized.not()}")

                        if (location == null) {
                            Toast.makeText(this, "Localização não encontrada", Toast.LENGTH_SHORT).show()
                        } else {
                            val locTextView = binding.riskLocEditText

                            userLatitude = location.latitude
                            userLongitude = location.longitude

                            locTextView.text = "Lat: $userLatitude, Long: $userLongitude"
                        }
                    }
                    .addOnFailureListener {
                         exception ->
                            Log.e("DEBUG_LOCATION", "Erro ao obter localização: ${exception.message}")
                        Toast.makeText(this, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Ative a Localização", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestPermission()
        }
    }


    private fun isLocationEnable():Boolean{
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACESS_LOCATION
        )

    }
    private fun checkedPermission():Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        else
        {return false}

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("PERMISSAO", "requestCode: $requestCode")
        Log.d("PERMISSAO", "permissions: ${permissions.joinToString()}")
        Log.d("PERMISSAO", "grantResults: ${grantResults.joinToString()}")

        if (requestCode == PERMISSION_REQUEST_ACESS_LOCATION) {
            // Verifica se a permissão foi realmente concedida
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permissão concedida", Toast.LENGTH_SHORT).show()
                getLocation()
            } else {
                // Permissão foi negada — perguntar se o usuário quer abrir as configurações
                AlertDialog.Builder(this)
                    .setTitle("Permissão negada")
                    .setMessage("A permissão de localização é necessária para continuar. Deseja abrir as configurações do app para concedê-la manualmente?")
                    .setPositiveButton("Abrir configurações") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = android.net.Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    companion object{
        private const val PERMISSION_REQUEST_ACESS_LOCATION=100
    }

}