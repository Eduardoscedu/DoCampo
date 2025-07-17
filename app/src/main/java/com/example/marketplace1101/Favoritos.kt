package com.example.marketplace1101

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*



class FavoritosActivity : AppCompatActivity() {

    // RecyclerView e adapter para exibir as receitas favoritas
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceitaAdapter
    private val listaFavoritos = mutableListOf<Receita>()
    private lateinit var btnback: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        // Configura título e botão de voltar
        supportActionBar?.title = "Meus Favoritos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configura RecyclerView e Adapter
        recyclerView = findViewById(R.id.recyclerViewFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnback = findViewById(R.id.btn_back)

        // 👇 Aqui você define o clique para abrir a receita
        adapter = ReceitaAdapter(listaFavoritos) { receita ->
            // INTENT PARA ABRIR OS DETALHES DA RECEITA
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("id", receita.id) // Envia o ID da receita
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Carrega os favoritos do Firebase
        carregarFavoritos()

        btnback.setOnClickListener {
            startActivity(Intent(this, RecipeListActivity::class.java))
            finish()
        }
    }


    // Busca os favoritos no Firebase Realtime Database
    private fun carregarFavoritos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val referencia = FirebaseDatabase.getInstance()
            .getReference("favoritos")
            .child(uid)

        referencia.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaFavoritos.clear()
                for (receitaSnap in snapshot.children) {
                    val receita = receitaSnap.getValue(Receita::class.java)
                    receita?.let { listaFavoritos.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavoritosActivity, "Erro ao carregar favoritos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Ação para o botão de voltar na action bar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
