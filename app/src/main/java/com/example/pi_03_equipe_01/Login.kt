package com.example.pi_03_equipe_01

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pi_03_equipe_01.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                val snackbarError = Snackbar.make(it, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                snackbarError.setBackgroundTint(Color.RED)
                snackbarError.show()
                return@setOnClickListener
            }

            realizarLogin(email, password)
        }

        binding.signInLink.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

    private fun realizarLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LOGIN", "Usuário logado com sucesso: ${auth.currentUser?.uid}")
                    val intent = Intent(this, Risk::class.java)
                    intent.putExtra("USER_ID", auth.currentUser?.uid)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("LOGIN", "Erro no login", task.exception)
                    Toast.makeText(
                        this,
                        "Erro ao fazer login: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}