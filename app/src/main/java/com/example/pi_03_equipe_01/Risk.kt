package com.example.pi_03_equipe_01

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pi_03_equipe_01.databinding.ActivityRiskBinding

class Risk : AppCompatActivity() {
    private lateinit var binding: ActivityRiskBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiskBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}