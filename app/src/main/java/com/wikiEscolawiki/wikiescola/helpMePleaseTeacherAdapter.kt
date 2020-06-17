package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//ao adapter, cuidado com letra maiuscula
class HelpMePleaseTeacherAdapter(private var context: Context, private var arrayDuvida:MutableList<String>, private var arrayConteudo:MutableList<String>, private var arrayAluno:MutableList<String>, private var arrayData:MutableList<String>, private var arrayBd:MutableList<String>, private var arrayBdAluno:MutableList<String>): RecyclerView.Adapter<HelpMePleaseTeacherAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayAluno.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.helpmeteacher_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.tvNome.text = "Aluno: "+arrayAluno.get(position);
        holder.tvData.text = "Data: "+arrayData.get(position)
        holder.tvConteudo.text = "Conteúdo: "+arrayConteudo.get(position)
        holder.tvDuvida.text = arrayDuvida.get(position)
    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        val tvNome: TextView = itemView!!.findViewById(R.id.helpmeteacher_txaluno)
        val tvData: TextView = itemView!!.findViewById(R.id.helpmeteacher_txData)
        var tvDuvida: TextView = itemView!!.findViewById(R.id.helpmeteacher_txduvida)
        var tvConteudo: TextView = itemView!!.findViewById(R.id.helpmeteacher_txconteudo)

    }

}
