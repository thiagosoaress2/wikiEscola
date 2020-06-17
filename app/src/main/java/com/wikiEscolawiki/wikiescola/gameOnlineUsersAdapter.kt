package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

//obs: O nome apos class tem que ser rigorosamente igual ao que vc deu
//ao adapter, cuidado com letra maiuscula
class gameOnlineUsersAdapter(private var context: Context, private var arrayNome:MutableList<String>, private var arrayImg:MutableList<String>, private var arrayEscola: MutableList<String>, private var arrayPontos:MutableList<String>, private var arrayBd:MutableList<String>): RecyclerView.Adapter<gameOnlineUsersAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayNome.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.alunos_online_game_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textViewNome.text = arrayNome.get(position);

        if (arrayImg.get(position).equals("nao")){

            try {
                Glide.with(context)
                    .load(R.drawable.blankprofile) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(context)) // applying the image transformer
                    .into(holder.img)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else {

            try {
                Glide.with(context)
                    .load(arrayImg.get(position)) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(context)) // applying the image transformer
                    .into(holder.img)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        holder.tvEscola.text = arrayEscola.get(position)
        holder.tvPontos.visibility = ViewGroup.GONE

    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        var textViewNome: TextView = itemView!!.findViewById(R.id.alunos_online_itemrow_txNome)
        var img: ImageView = itemView!!.findViewById(R.id.alunos_online_itemrow_imageView)
        var tvEscola: TextView = itemView!!.findViewById(R.id.alunos_online_itemrow_txEscola)
        var tvPontos: TextView = itemView!!.findViewById(R.id.alunos_online_itemrow_txPontos)

    }

}
