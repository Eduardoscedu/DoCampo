package com.example.marketplace1101

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var nome: TextView
    private lateinit var origem: TextView
    private lateinit var instrucoes: TextView
    private lateinit var btnFavorite: ImageView

    private var isFavorito = false
    private lateinit var receitaId: String
    private var nomeReceita: String = ""
    private var imagemUrlReceita: String = ""
    private var origemReceita: String = ""
    private var instrucoesReceita: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        image = findViewById(R.id.detailImagem)
        nome = findViewById(R.id.detailNome)
        origem = findViewById(R.id.detailCategoria)
        instrucoes = findViewById(R.id.detailInstrucoes)
        btnFavorite = findViewById(R.id.btnFavorite)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes da Receita"

        receitaId = intent.getStringExtra("id") ?: return

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(MealService::class.java)
        service.buscarDetalhesReceita(receitaId).enqueue(object : Callback<MealsResponse> {
            override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                val meal = response.body()?.meals?.firstOrNull()
                meal?.let {
                    nomeReceita = it.strMeal
                    imagemUrlReceita = it.strMealThumb
                    origemReceita = it.strArea ?: ""
                    instrucoesReceita = it.strInstructions ?: ""

                    nome.text = nomeReceita
                    origem.text = "Origem: $origemReceita"
                    instrucoes.text = instrucoesReceita
                    Picasso.get().load(imagemUrlReceita).into(image)

                    //Verificação se Ja está nos favoritos!
                    val receitaId = it.idMeal
                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                    if (uid != null) {
                        val ref = FirebaseDatabase.getInstance()
                            .getReference("favoritos")
                            .child(uid)
                            .child(receitaId)

                        ref.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                // Já está nos favoritos
                                isFavorito = true
                                btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
                            } else {
                                // Ainda não está
                                isFavorito = false
                                btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this@RecipeDetailActivity, "Erro ao verificar favorito", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                Toast.makeText(this@RecipeDetailActivity, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()
            }
        })

        btnFavorite.setOnClickListener {
            if (!isFavorito) {
                salvarFavorito()
            } else {
                removerFavorito()
            }
        }
    }

    private fun salvarFavorito() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference("favoritos").child(uid).child(receitaId)

        //Adicona esta variaveis no Banco de dados - REALTIME DATABASE -
        val receitaFavorita = mapOf(
            "id" to receitaId,
            "nome" to nomeReceita,
            "imagemUrl" to imagemUrlReceita,
            "origem" to origemReceita,
            "instrucoes" to instrucoesReceita
        )

        referencia.setValue(receitaFavorita)
            .addOnSuccessListener {
                isFavorito = true
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
                Toast.makeText(this, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar favorito: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

    }

    private fun removerFavorito() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference("favoritos").child(uid).child(receitaId)

        referencia.removeValue()
            .addOnSuccessListener {
                isFavorito = false
                btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
