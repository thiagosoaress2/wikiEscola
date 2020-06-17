package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Html
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class profMontaContAdapter(private var context: Context, private var arrayListaItens:MutableList<String>, private var arrayDescricao:MutableList<String>, private var arrayAlinhamento:MutableList<String>, private var arrayCor:MutableList<String>): RecyclerView.Adapter<profMontaContAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayListaItens.size;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.conteudos_itemrow, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //aqui você define os valores em cada elemento

        if (arrayDescricao.get(position).equals("subtitulo")){
            holder.tvSubtitulo.visibility = View.VISIBLE
            holder.tvSubtitulo.setText(arrayListaItens.get(position))
            ajustaCor(arrayCor.get(position), holder.tvSubtitulo)
            ajustaAlinhamento(arrayAlinhamento.get(position), holder.tvSubtitulo)
        } else if (arrayDescricao.get(position).equals("texto")){
            holder.tvTexto.visibility = View.VISIBLE
            holder.tvTexto.setText(arrayListaItens.get(position))
            ajustaCor(arrayCor.get(position), holder.tvSubtitulo)
            ajustaAlinhamento(arrayAlinhamento.get(position), holder.tvSubtitulo)
        } else if (arrayDescricao.get(position).equals("img")){
            holder.img.visibility = View.VISIBLE
            ajustaAlinhamentoImg(arrayAlinhamento.get(position), holder.img, holder.layout)
            Glide.with(context).load(arrayListaItens.get(position)).into(holder.img)
        } else if (arrayDescricao.get(position).equals("link")){
            holder.tvLink.setText(Html.fromHtml("<u>Link de vídeo</u>"))
            ajustaCor(arrayCor.get(position), holder.tvLink)
            ajustaAlinhamento(arrayAlinhamento.get(position), holder.tvLink)
            holder.tvLink.visibility = View.VISIBLE
            holder.imgLink.visibility = View.VISIBLE

            //o texto é um padrão
            //o link será chamado da recyclerView
        }

    }

    fun ajustaCor(cor: String, textView: TextView){

        if (cor.equals("#000000")){
            textView.setTextColor(Color.parseColor("#000000"))
        } else if (cor.equals("#2140EF")){
            textView.setTextColor(Color.parseColor("#2140EF"))
        }else if (cor.equals("#F44336")){
            textView.setTextColor(Color.parseColor("#F44336"))
        }else if (cor.equals("#4CAF50")){
            textView.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            //do nothing. é que foi link ou imagem
        }
    }

    fun ajustaAlinhamento(align: String, textView: TextView){

        if (align.equals("center")){
            //textView.gravity = Gravity.CENTER_HORIZONTAL
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else if (align.equals("left")){
            //textView.gravity = Gravity.LEFT

            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        } else if (align.equals("right")) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            //do nothing. é que foi link ou imagem
        }
    }

    fun ajustaAlinhamentoImg(align: String, imageView: ImageView, layout: ConstraintLayout){

        val params = imageView.layoutParams as ConstraintLayout.LayoutParams

        if (align.equals("left")){
            params.leftToLeft = layout.id
        } else if (align.equals("center")){
            params.leftToLeft = layout.id
            params.rightToRight = layout.id
        } else {
            params.rightToRight = layout.id
        }
        imageView.requestLayout()
    }

    class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        //aqui você associa cada elemento a um nome para invocar nesta classe
        //val tvTitulo: TextView = itemView!!.findViewById(R.id.conteudos_itemrow_tvtitulo)
        val tvSubtitulo: TextView = itemView!!.findViewById(R.id.conteudos_itemrow_tvSubTitulo)
        val img: ImageView = itemView!!.findViewById(R.id.conteudos_itemrow_imageview)
        val tvTexto: TextView = itemView!!.findViewById(R.id.conteudos_itemrow_tvTexto)
        val tvLink : TextView = itemView!!.findViewById(R.id.conteudos_itemrow_tvLink)
        val imgLink : ImageView = itemView!!.findViewById(R.id.conteudos_itemrow_tvLinkiv)
        val layout: ConstraintLayout = itemView!!.findViewById(R.id.layoutBackground)

    }

}
