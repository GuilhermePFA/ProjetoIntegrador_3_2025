package com.example.pi_03_equipe_01

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pi_03_equipe_01.databinding.ActivityInformationsBinding
import com.google.firebase.database.FirebaseDatabase


class Information : AppCompatActivity() {
    private lateinit var binding:ActivityInformationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val historyId = intent.getStringExtra("HISTORY_ID") ?: return
        val ref = FirebaseDatabase.getInstance().getReference("risk").child(historyId)

        Log.d("FIREBASE", "Id: ${historyId}")
    }
}