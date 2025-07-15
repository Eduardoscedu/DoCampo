package com.example.marketplace1101

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class PerfilActivity : AppCompatActivity() {

    private lateinit var nomeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val user = FirebaseAuth.getInstance().currentUser

        nomeTextView = findViewById(R.id.profileName)
        val emailText = findViewById<TextView>(R.id.profileEmail)
        val imageView = findViewById<ImageView>(R.id.profileImage)
        val textViewDataCadastro = findViewById<TextView>(R.id.textViewDataCadastro)
        val textViewTotalFavoritos = findViewById<TextView>(R.id.textViewTotalFavoritos)

        val uid = user?.uid

        if (uid != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nome = document.getString("nome")
                        nomeTextView.text = nome ?: "Nome não encontrado"
                    } else {
                        Toast.makeText(this, "Documento não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show()
                }

            // Consulta total de favoritos no Realtime Database
            val ref = FirebaseDatabase.getInstance().getReference("favoritos").child(uid)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    textViewTotalFavoritos.text = "Receitas Favoritas: $count"
                }

                override fun onCancelled(error: DatabaseError) {
                    textViewTotalFavoritos.text = "Erro ao contar favoritos"
                }
            })
        } else {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
        }

        // Mostra dados do FirebaseAuth
        nomeTextView.text = user?.displayName ?: "Usuário"
        emailText.text = user?.email ?: "Email não disponível"

        user?.metadata?.creationTimestamp?.let { timestamp ->
            val data = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
            textViewDataCadastro.text = "Cadastrado em: $data"
        }

        user?.photoUrl?.let {
            Picasso.get().load(it).into(imageView)
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            startActivity(Intent(this, RecipeListActivity::class.java))
        }
    }
}
