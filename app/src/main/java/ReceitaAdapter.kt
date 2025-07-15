import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.marketplace1101.R
import com.example.marketplace1101.Receita
import com.squareup.picasso.Picasso

class ReceitaAdapter(
    private val lista: List<Receita>,
    private val onClick: (Receita) -> Unit
) : RecyclerView.Adapter<ReceitaAdapter.ViewHolder>() {

    private val favoritos = mutableSetOf<String>() // Armazena IDs das receitas favoritas

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.textViewNome)
        val imagem: ImageView = view.findViewById(R.id.imageViewReceita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receita, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receita = lista[position]

        holder.nome.text = receita.nome
        Picasso.get().load(receita.imagemUrl).into(holder.imagem)


        // Clique na receita
        holder.itemView.setOnClickListener { view -> onClick(receita)  }

    }

    override fun getItemCount(): Int = lista.size
}

