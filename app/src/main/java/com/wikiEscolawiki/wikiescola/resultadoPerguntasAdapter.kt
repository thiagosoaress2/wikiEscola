package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class resultadoPerguntasAdapter(private var context: Context, private var arrayResult:MutableList<String>): RecyclerView.Adapter<resultadoPerguntasAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayResult.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.resultadoquestao_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textViewNome.text = (position+1).toString()
        if (arrayResult.get(position).equals("sim")){
            //n precisa, ja esta visivel
        } else {
            holder.ivErro.visibility = View.VISIBLE
            holder.ivCerto.visibility = View.GONE
        }
    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        val textViewNome: TextView = itemView!!.findViewById(R.id.resultadoquestao_textview)
        val ivErro : ImageView = itemView!!.findViewById(R.id.resultadoquestao_imageViewErro)
        val ivCerto : ImageView = itemView!!.findViewById(R.id.resultadoquestao_imageViewCerto)

    }

}
