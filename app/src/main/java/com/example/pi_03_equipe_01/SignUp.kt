package com.example.pi_03_equipe_01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pi_03_equipe_01.databinding.ActivityHistoryBinding
import com.google.android.material.navigation.NavigationView

class SignUp : AppCompatActivity() {
    private lateinit var binding: SignUp
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inicialize o DrawerLayout
        drawerLayout = findViewById(R.id.SingUp) //
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Configure ações do menu lateral
        configureNavigationMenu(navigationView)

        // Configure o botão para abrir o menu
        val menuButton: Button = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun configureNavigationMenu(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navegar para "Home"
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    // Navegar para "Histórico"
                    val intent = Intent(this, History::class.java)
                    startActivity(intent)
                }
                R.id.nav_sair -> {
                    // Finalizar atividade atual
                    finish()
                }
            }
            // Fecha o menu após a seleção
            drawerLayout.closeDrawers()
            true
        }
    }
}