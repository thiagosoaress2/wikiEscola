package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class questoes_Adapter(private var context: Context, private var arrayPergunta:MutableList<String>, private var arrayBdPergunta:MutableList<String>): RecyclerView.Adapter<questoes_Adapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayPergunta.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.questoes_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textView.text = arrayPergunta.get(position);

    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        var textView: TextView = itemView!!.findViewById(R.id.questoes_itemrow_txt)

    }

}
