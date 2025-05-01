package com.example.pi_03_equipe_01

import android.content.Intent
import android.view.MotionEvent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pi_03_equipe_01.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivitySignUpBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpBtn.setOnClickListener {
            val name = binding.signUpFullNameEditText.text.toString()
            val phone = binding.signUpPhoneEditText.text.toString()
            val email = binding.signUpEmailEditText.text.toString()
            val password = binding.signUpPasswordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Snackbar.make(it, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show()
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { signUp ->
                    if (signUp.isSuccessful) {
                        Snackbar.make(it, "Sucesso ao cadastrar usuário!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.GREEN)
                            .show()

                        val userId = auth.currentUser?.uid
                        saveUserInDatabase(userId)

                        binding.signUpFullNameEditText.setText("")
                        binding.signUpPhoneEditText.setText("")
                        binding.signUpEmailEditText.setText("")
                        binding.signUpPasswordEditText.setText("")

                        startActivity(Intent(this, Risk::class.java).apply {
                            putExtra("USER_ID", userId)
                        })
                    }
                }.addOnFailureListener { exception ->
                    Snackbar.make(binding.root, "Erro: ${exception.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.RED)
                        .show()
                }
            }
        }

        binding.signUpLoginText1.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
        @Suppress("ClickableViewAccessibility")
        binding.signUpPasswordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.signUpPasswordEditText.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val touchAreaStart = binding.signUpPasswordEditText.width -
                            binding.signUpPasswordEditText.paddingEnd -
                            drawableEnd.intrinsicWidth
                    if (event.rawX >= touchAreaStart) {
                        v.performClick() // ✅ Notifica que houve um click
                        isPasswordVisible = !isPasswordVisible
                        togglePasswordVisibility(isPasswordVisible)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }
    private fun saveUserInDatabase(userId: String?){
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("user")

        val userInfo = mapOf(
            "userId" to userId,
            "name" to binding.signUpFullNameEditText.text.toString(),
            "phone" to binding.signUpPhoneEditText.text.toString(),
            "email" to binding.signUpEmailEditText.text.toString(),
            "role" to "User"
        )

        userId?.let {
            ref.child(it).setValue(userInfo)
        }
    }
    private fun togglePasswordVisibility(visible: Boolean) {
        if (visible) {
            binding.signUpPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.signUpPasswordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.password_eye_off_24, 0)
        } else {
            binding.signUpPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.signUpPasswordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.password_eye_24, 0)
        }
        // Move cursor para o fim
        binding.signUpPasswordEditText.setSelection(binding.signUpPasswordEditText.text.length)
    }
}