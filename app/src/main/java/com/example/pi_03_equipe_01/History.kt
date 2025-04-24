package com.example.pi_03_equipe_01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pi_03_equipe_01.databinding.ActivityHistoryBinding
import com.google.firebase.database.*

class History : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().getReference("risk")
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var adapter: HistoryAdapter
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        fetchHistoryFromFirebase()
    }

    private fun initRecyclerView() {
        adapter = HistoryAdapter(historyList) { historyId ->
            val intent = Intent(this, Information::class.java)
            intent.putExtra("HISTORY_ID", historyId)
            startActivity(intent)
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.setHasFixedSize(true)
        binding.historyRecyclerView.adapter = adapter
    }

    private fun fetchHistoryFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FIREBASE", "Snapshot recebido: ${snapshot.childrenCount}")
                historyList.clear()

                for (itemSnapshot in snapshot.children) {
                    val id = itemSnapshot.child("riskID").getValue(String::class.java) ?: ""
                    if (id.isBlank()) continue

                    val date = itemSnapshot.child("created_at").getValue(String::class.java) ?: ""
                    val statusStr = itemSnapshot.child("status").getValue(String::class.java) ?: "NAO_INICIADO"

                    try {
                        val status = HistoryItem.Status.valueOf(statusStr.replace(" ", "_").uppercase())
                        val historyItem = HistoryItem(id, date, status)
                        historyList.add(historyItem)
                    } catch (e: Exception) {
                        Log.e("FIREBASE", "Status inválido ou erro: $statusStr", e)
                    }
                }

                Log.d("FIREBASE", "Itens carregados: ${historyList.size}")
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Erro de leitura: ${error.message}")
            }
        })
    }
}
