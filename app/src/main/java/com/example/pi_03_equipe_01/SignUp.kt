package com.example.pi_03_equipe_01

import android.graphics.Color
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpBtn.setOnClickListener{
            val name = binding.signUpFullNameEditText.text.toString()
            val phone = binding.signUpPhoneEditText.text.toString()
            val email = binding.signUpEmailEditText.text.toString()
            val password = binding.signUpPasswordEditText.text.toString()

            if(email.isEmpty() || password.isEmpty() || name.isEmpty()||phone.isEmpty()){
                val snackbar = Snackbar.make(it, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.show()
            }else{
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ signUp ->
                    if (signUp.isSuccessful){
                        val snackbar = Snackbar.make(it, "Sucesso ao cadastrar usuário!",Snackbar.LENGTH_SHORT)
                        snackbar.setBackgroundTint(Color.GREEN)
                        snackbar.show()
                        val userId = auth.currentUser?.uid
                        saveUserInDatabase(userId)
                        binding.signUpFullNameEditText.setText("")
                        binding.signUpPhoneEditText.setText("")
                        binding.signUpEmailEditText.setText("")
                        binding.signUpPasswordEditText.setText("")
                    }
                }.addOnFailureListener{

                }
            }

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
}