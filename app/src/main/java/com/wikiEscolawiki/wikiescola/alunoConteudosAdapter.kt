package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class alunoConteudosAdapter(private var context: Context, private var arrayConteudo:MutableList<String>, private var arrayBd:MutableList<String>, val sawContent: String, val masteredContent: String): RecyclerView.Adapter<alunoConteudosAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayConteudo.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.aluno_conteudos_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textViewConteudo.text = arrayConteudo.get(position);
        //implementar a visualização
        if (sawContent.contains(arrayConteudo.get(position))){
            holder.saw.visibility = View.VISIBLE
        }

        if (masteredContent.contains(arrayConteudo.get(position))){
            holder.mastered.visibility = View.VISIBLE
        }

    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        val textViewConteudo: TextView = itemView!!.findViewById(R.id.aluno_conteudos_itemrow_txt)
        val saw: ImageView = itemView!!.findViewById(R.id.aluno_conteudos_itemrow_imageview)
        val mastered: ImageView = itemView!!.findViewById(R.id.aluno_conteudos_itemrow_Star)

    }

}
