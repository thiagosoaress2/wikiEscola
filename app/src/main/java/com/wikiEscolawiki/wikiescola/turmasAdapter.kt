package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class turmasAdapter(private var context: Context, private var arrayTurmas:MutableList<String>): RecyclerView.Adapter<turmasAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayTurmas.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.turmas_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textViewNome.text = arrayTurmas.get(position);

    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        var textViewNome: TextView = itemView!!.findViewById(R.id.resultadoquestao_textview)

    }

}
