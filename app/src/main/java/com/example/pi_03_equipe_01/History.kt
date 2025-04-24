package com.example.pi_03_equipe_01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.example.pi_03_equipe_01.databinding.ActivityHistoryBinding

class History : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialize o DrawerLayout e NavigationView
        drawerLayout = findViewById(R.id.hist)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Configure o botão para abrir o menu lateral
        val menuButton: Button = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Abre o menu lateral
        }

        // Configure as ações do menu lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    // Fechar o menu sem recarregar, já estamos na atividade "History"
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_signup -> {
                    val intent = Intent(this, SignUp::class.java)
                    startActivity(intent)
                }
                R.id.nav_sair -> {
                    finish() // Encerra a atividade atual
                }
            }
            true
        }

        // Inicialize o RecyclerView
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.setHasFixedSize(true)
        binding.historyRecyclerView.adapter = HistoryAdapter(getHistoryList())
    }

    private fun getHistoryList(): List<HistoryItem> {
        return listOf(
            HistoryItem("1", "10/04/2025", Status.FINALIZADO),
            HistoryItem("2", "12/04/2025", Status.ANDAMENTO),
            HistoryItem("3", "15/04/2025", Status.NAO_INICIADO)
        )
    }
}