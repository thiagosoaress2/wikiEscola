package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class conteudosAdapter(private var context: Context, private var arrayConteudo:MutableList<String>, private var arrayBd:MutableList<String>): RecyclerView.Adapter<conteudosAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayConteudo.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.turmas_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textViewConteudo.text = arrayConteudo.get(position);

    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        var textViewConteudo: TextView = itemView!!.findViewById(R.id.resultadoquestao_textview)

    }

}
