package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class meusAlunosAdapter(private var context: Context, private var arrayNome:MutableList<String>, private var arrayImg:MutableList<String>, private var arrayDesp:MutableList<String>): RecyclerView.Adapter<meusAlunosAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayNome.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.meusalunos_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento
        holder.textViewNome.text = arrayNome.get(position);
        if (arrayImg.get(position)=="nao"){
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

        holder.textDesemp.setText("Desempenho: "+arrayDesp.get(position))
    }


    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        var textViewNome: TextView = itemView!!.findViewById(R.id.meusalunos_itemrow_txNome)
        var img: ImageView = itemView!!.findViewById(R.id.meusalunos_itemrow_imageview)
        var textDesemp: TextView = itemView!!.findViewById(R.id.meusalunos_itemrow_txDesmp)

    }

}
