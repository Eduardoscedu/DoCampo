package com.example.marketplace1101

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// MODELOS
data class Receita(
    var id: String = "",
    var nome: String = "",
    var imagemUrl: String = "",
    var origem: String = "",
    var instrucoes: String = "",
    var categoria: String = ""
)

data class MealsResponse(val meals: List<Meal>?)
data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strCategory: String?,
    val strInstructions: String?,
    val strArea: String?
)

interface MealService {
    @GET("filter.php")
    fun listarReceitas(@Query("c") categoria: String): Call<MealsResponse>

    @GET("lookup.php")
    fun buscarDetalhesReceita(@Query("i") id: String): Call<MealsResponse>
}

class ReceitaAdapter(
    private val lista: List<Receita>,
    private val onItemClick: (Receita) -> Unit
) : RecyclerView.Adapter<ReceitaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.textViewNome)
        val imagem: ImageView = view.findViewById(R.id.imageViewReceita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receita, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receita = lista[position]
        holder.nome.text = receita.nome
        Picasso.get().load(receita.imagemUrl).into(holder.imagem)

        holder.itemView.setOnClickListener {
            onItemClick(receita)
        }
    }

    override fun getItemCount(): Int = lista.size
}

class RecipeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var retrofit: Retrofit
    private lateinit var service: MealService
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val receitasTotais = mutableListOf<Receita>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        val searchView = findViewById<SearchView>(R.id.searchView)

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recipeRecyclerView)
        chipGroup = findViewById(R.id.chipGroupCategoria)
        recyclerView.layoutManager = LinearLayoutManager(this)

        retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(MealService::class.java)

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            chip?.setOnCheckedChangeListener { _, _ -> buscarReceitasMultiplasCategorias() }
        }

        val primeiroChip = chipGroup.getChildAt(0) as? Chip
        primeiroChip?.isChecked = true

        val headerView = navigationView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.textUserName)
        userName.text = "Olá Cozinheiro!"

        val userImage2 = findViewById<ImageView>(R.id.userImage)
        userImage2.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dados_usuario -> startActivity(Intent(this, PerfilActivity::class.java))
                R.id.nav_favoritos -> startActivity(Intent(this, FavoritosActivity::class.java))
                R.id.nav_sair -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText.orEmpty().lowercase()
                val receitasFiltradas = receitasTotais.filter { receita ->
                    receita.nome.lowercase().contains(texto)
                            || receita.origem.lowercase().contains(texto)
                            || receita.categoria.lowercase().contains(texto)
                            || receita.instrucoes.lowercase().contains(texto)
                }
                recyclerView.adapter = ReceitaAdapter(receitasFiltradas) { receita ->
                    val intent = Intent(this@RecipeListActivity, RecipeDetailActivity::class.java)
                    intent.putExtra("id", receita.id)
                    startActivity(intent)
                }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun buscarReceitasMultiplasCategorias() {
        val categoriasSelecionadas = mutableListOf<String>()

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip != null && chip.isChecked) {
                categoriasSelecionadas.add(chip.text.toString())
            }
        }

        receitasTotais.clear()

        if (categoriasSelecionadas.isEmpty()) {
            recyclerView.adapter = ReceitaAdapter(emptyList()) { receita ->
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("id", receita.id)
                startActivity(intent)
            }
            return
        }

        for (categoria in categoriasSelecionadas) {
            service.listarReceitas(categoria).enqueue(object : Callback<MealsResponse> {
                override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                    val novasReceitas = response.body()?.meals?.map {
                        Receita(
                            id = it.idMeal,
                            nome = it.strMeal,
                            imagemUrl = it.strMealThumb,
                            origem = it.strArea.orEmpty(),
                            categoria = it.strCategory.orEmpty(),
                            instrucoes = it.strInstructions.orEmpty()
                        )
                    } ?: emptyList()

                    receitasTotais.addAll(novasReceitas)

                    recyclerView.adapter = ReceitaAdapter(receitasTotais) { receita ->
                        val intent = Intent(this@RecipeListActivity, RecipeDetailActivity::class.java)
                        intent.putExtra("id", receita.id)
                        startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
                    Toast.makeText(this@RecipeListActivity, "Erro: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}