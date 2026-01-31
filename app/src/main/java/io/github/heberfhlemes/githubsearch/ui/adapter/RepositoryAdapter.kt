package io.github.heberfhlemes.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.heberfhlemes.githubsearch.R
import io.github.heberfhlemes.githubsearch.domain.Repository

class RepositoryAdapter(
    private val repositories: List<Repository>
) : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var onItemClick: (Repository) -> Unit = {}
    var onShareClick: (Repository) -> Unit = {}

    /**
     * Cria uma nova view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * Pega o conteúdo da view e troca pela informação de item de uma lista
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repository = repositories[position]

        holder.name.text = repository.name

        holder.itemView.setOnClickListener {
            onItemClick(repository)
        }

        holder.btnShare.setOnClickListener {
            onShareClick(repository)
        }
    }

    /**
     * Pega a quantidade de repositórios da lista.
     */
    override fun getItemCount(): Int = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_nome_repositorio)
        val btnShare: ImageView = view.findViewById(R.id.iv_compartilhar)
    }
}


