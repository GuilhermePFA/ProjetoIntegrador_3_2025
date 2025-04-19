package com.example.pi_03_equipe_01

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pi_03_equipe_01.databinding.ActivityHistoryBinding

class History : AppCompatActivity() {

    private lateinit var binding:ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun initRecycleView(){
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.setHasFixedSize(true)
        binding.historyRecyclerView.adapter = HistoryAdapter(getList())
    }
    private fun getList() = listOf<HistoryItem>(
        HistoryItem("1", "10/04/2025", Status.FINALIZADO),
        HistoryItem("2", "12/04/2025", Status.ANDAMENTO),
        HistoryItem("3", "15/04/2025", Status.NAO_INICIADO)
    )
}