package com.example.pi_03_equipe_01

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pi_03_equipe_01.databinding.ActivityRiskBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class Risk : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityRiskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.riskSent.setOnClickListener { view ->
            // pega o ID do usuário e tbm ve se ta logado
            val currentUser = auth.currentUser
            val userId = currentUser?.uid
            if (userId == null) {
                Snackbar.make(view, "Usuário não autenticado!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show()
                return@setOnClickListener
            }

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
                "location" to localizacao,
                "title" to tipo,
                "description" to descricao,
                "status" to "NAO INICIADO"
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
    }

    // Função para gerar o id do risco
    private fun generateFiveDigitId(): String {
        val number = Random.nextInt(10000, 100000) // [10000, 99999]
        return number.toString()
    }
}
