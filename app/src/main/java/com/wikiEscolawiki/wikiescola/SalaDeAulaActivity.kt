package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SalaDeAulaActivity : AppCompatActivity() {


    //to do
    // 1 deu erro quando chama a segunda questao
    //2 O simbolo de visualizado está aparecendo em todos conteudos na recyclerview.

    /*
    LÍNGUA PORTUGUESA I
    LÍNGUA PORTUGUESA II
    MATEMÁTICA I
    MATEMÁTICA II
    HISTÓRIA
    GEOGRAFIA
    CIÊNCIAS
    BIOLOGIA
    FÍSICA
    QUÍMICA
    LÍNGUA INGLESA
    ARTE
    ENSINO RELIGIOSO
    EDUCAÇÃO FÍSICA
     */

    var sawContent: String = "nao"
    var masteredContent: String = "nao"

    var tentativasNaRodada = 0
    var acertosNaRodada = 0
    val arrayErroAcerto: MutableList<String> = ArrayList()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mphotoStorageReference: StorageReference
    private lateinit var mFireBaseStorage: FirebaseStorage

    var userBd: String = "nao"
    var escola: String = "nao"
    var turma: String = "nao"
    var nome: String = "nao"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sala_de_aula)

        databaseReference = FirebaseDatabase.getInstance().reference
        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference

        menuClicks()

        escola = intent.getStringExtra("escola").toString()
        userBd = intent.getStringExtra("userBd").toString()
        turma = intent.getStringExtra("turma").toString()
        nome = intent.getStringExtra("nome").toString()

        if (turma.equals("nao")){
            val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
            val editor = sharedPref.edit()
            turma = sharedPref.getString("turma", "nao").toString()
            if (turma.equals("nao")){
                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()
                //editor.putString("userBd", "nao")
                editor.clear().apply()
                showtoast("Reiniciando aplicação. Seus dados foram perdidos.")
                finish()
            }
        }


    }


    fun menuClicks (){

        val btnGeo: ConstraintLayout = findViewById(R.id.layGeo)
        btnGeo.setOnClickListener {

            getSawContents("Geografia")
        }

        val btnPort1: ConstraintLayout = findViewById(R.id.layPort1)
        btnPort1.setOnClickListener {

            getSawContents("Português 1")
        }

        val btnPort2: ConstraintLayout = findViewById(R.id.layPort2)
        btnPort2.setOnClickListener {

            getSawContents("Português 2")
        }


        val btnMat1: ConstraintLayout = findViewById(R.id.layMat1)
        btnMat1.setOnClickListener {

            getSawContents("Matemática 1")
        }

        val btnMat2: ConstraintLayout = findViewById(R.id.layMat2)
        btnMat2.setOnClickListener {

            getSawContents("Matemática 2")
        }

        val btnHist: ConstraintLayout = findViewById(R.id.layHist)
        btnHist.setOnClickListener {

            getSawContents("História")
        }

        val btnCien: ConstraintLayout = findViewById(R.id.layCien)
        btnCien.setOnClickListener {

            getSawContents("Ciências")
        }

        val btnBio: ConstraintLayout = findViewById(R.id.layBio)
        btnBio.setOnClickListener {

            getSawContents("Biologia")
        }

        val btnFis: ConstraintLayout = findViewById(R.id.layFis)
        btnFis.setOnClickListener {

            getSawContents("Física")
        }

        val btnQuim: ConstraintLayout = findViewById(R.id.layQuim)
        btnQuim.setOnClickListener {

            getSawContents("Química")
        }

        val btnIng: ConstraintLayout = findViewById(R.id.layIng)
        btnIng.setOnClickListener {

            getSawContents("Inglês")
        }

        val btnArte: ConstraintLayout = findViewById(R.id.layArt)
        btnArte.setOnClickListener {

            getSawContents("Arte")
        }

        val btnEnsRel: ConstraintLayout = findViewById(R.id.layEnsRel)
        btnEnsRel.setOnClickListener {

            getSawContents("Ensino religioso")
        }

        val btnEdFis: ConstraintLayout = findViewById(R.id.layEdFis)
        btnEdFis.setOnClickListener {

            getSawContents("Educação física")
        }

    }


    fun montaRecycleConteudosDaTurmaJaComDisciplina(disciplina: String){

        val arrayConteudo: MutableList<String> = ArrayList()
        val arrayBdConteudo: MutableList<String> = ArrayList()

        val paginaIndex: ConstraintLayout = findViewById(R.id.salaDeAulaIndex)
        val paginaDaRecycleConteudos: ConstraintLayout = findViewById(R.id.exibindoConteudos)

        paginaDaRecycleConteudos.visibility = View.VISIBLE
        paginaIndex.visibility = View.GONE


        //chame aqui pelo adaptador que criamos, com o nome dado e o construtor
        var adapter: alunoConteudosAdapter = alunoConteudosAdapter(this, arrayConteudo, arrayBdConteudo, sawContent, masteredContent)

        //chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.exibindoConteudos_recyclerView)

        //define o tipo de layout (linerr, grid)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        //coloca o adapter na recycleview
        recyclerView.adapter = adapter

        recyclerView.layoutManager = linearLayoutManager

        // Notify the adapter for data change.
        adapter.notifyDataSetChanged()

        //constructor: context, nomedarecycleview, object:ClickListener
        recyclerView.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView!!, object: ClickListener{

            override fun onClick(view: View, position: Int) {
                //Log.d("teste", aNome.get(position))
                //Toast.makeText(this@MainActivity, !! aNome.get(position).toString(), Toast.LENGTH_SHORT).show()
                openPopUpDaListaConteudoDaturma("Vamos estudar?", "Você quer abrir este conteúdo pra estudo?", true, "Sim, abrir", "Não", "conteudo", arrayBdConteudo.get(position), arrayConteudo.get(position), disciplina)

            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))


        chamaDialog()
        //turmaSelecionada = escola+turma
        val rootRef = databaseReference.child("conteudos").child(escola+turma).child(disciplina)
        rootRef.orderByChild("disciplina").equalTo(disciplina)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()) {

                        for (querySnapshot in dataSnapshot.children) {

                            var x = querySnapshot.child("titulo").getValue().toString()
                            arrayConteudo.add(x)

                            arrayBdConteudo.add(querySnapshot.key.toString())

                            adapter.notifyDataSetChanged()

                            /*
                            if (sawContent.contains(x)){

                                sawContent = sawContent+"!@#"+x
                                databaseReference.child("user_achievement").child(userBd).child(disciplina).child("sawContent").setValue(sawContent)
                                //salva no shared
                                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                                val editor = sharedPref.edit()
                                editor.putString(disciplina+"Saw", sawContent)
                                editor.apply()

                            }

                             */



                            /*
                            //agora vamos procurar se em shared preferences tem entrada com esse nome
                            val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                            val editor = sharedPref.edit()

                            val doesItExists = sharedPref.getString(disciplina+"_"+x, "nao").toString()
                            if (doesItExists.equals("nao")){
                                //nao existe
                                arrayVisualizado.add("nao")
                                arrayMastered.add("nao")
                            } else {
                                arrayVisualizado.add("sim")
                                val doesHeMastered = sharedPref.getString(disciplina+"_"+x+"Mastered", "nao").toString()
                                if (doesHeMastered.equals("sim")){
                                    arrayMastered.add("sim")
                                } else {
                                    arrayMastered.add("nao")
                                }

                            }

                             */


                        }

                    } else {
                        //showToast("Não existem conteúdos cadastrados na sua disciplina para esta turma.")
                        val tvMensagem: TextView = findViewById(R.id.exibindoConteudos_txtMensagem)
                        tvMensagem.setText("Não existem conteúdos cadastrados")

                    }


                    encerraDialog()

                }




                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }

            })

    }

    fun openPopUpDaListaConteudoDaturma (titulo: String, texto:String, exibeBtnOpcoes:Boolean, btnSim: String, btnNao: String, call: String, BdConteudo: String, conteudo: String, disciplina: String) {
        //exibeBtnOpcoes - se for não, vai exibir apenas o botão com OK, sem opção. Senão, exibe dois botões e pega os textos deles de btnSim e btnNao

        //EXIBIR POPUP
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.popup_model,null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }


        // Get the widgets reference from custom view
        val buttonPopupN = view.findViewById<Button>(R.id.popupBtnNao)
        val buttonPopupS = view.findViewById<Button>(R.id.popupBtnSim)
        val buttonPopupOk = view.findViewById<Button>(R.id.popupBtnOk)
        val txtTitulo = view.findViewById<TextView>(R.id.popupTitulo)
        val txtTexto = view.findViewById<TextView>(R.id.popupTexto)


        if (exibeBtnOpcoes){
            //vai exibir os botões com textos e esconder o btn ok
            buttonPopupOk.visibility = View.GONE
            //exibe e ajusta os textos dos botões
            buttonPopupN.text = btnNao
            buttonPopupS.text = btnSim

            // Set a click listener for popup's button widget
            buttonPopupN.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

        } else {

            //vai esconder os botões com textos e exibir o btn ok
            buttonPopupOk.visibility = View.VISIBLE
            //exibe e ajusta os textos dos botões
            buttonPopupN.visibility = View.GONE
            buttonPopupS.visibility = View.GONE


            buttonPopupOk.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

        }

        txtTitulo.text = titulo
        txtTexto.text = texto


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            //Fecha a janela ao clicar fora também
        }

        //lay_root é o layout parent que vou colocar a popup
        val lay_root: ConstraintLayout = findViewById(R.id.exibindoConteudos)

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(lay_root)
        popupWindow.showAtLocation(
            lay_root, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

        //aqui colocamos os ifs com cada call de cada vez que a popup for chamada
        if (call.equals("conteudo")){

            buttonPopupS.setOnClickListener {
                //faz aqui o que quiser
                //abreAparencia(sexo)
                openConteudoParaEstudo(BdConteudo, conteudo, disciplina)
                popupWindow.dismiss()
            }
        }

    }

    //vai fazer a query aqui dentro e popular o conteúdo. Vai ajustar no bd do user que ele viu esse conteudo
    fun openConteudoParaEstudo(bdConteudo: String, conteudo: String, disciplina: String){

        val paginaComConteudoEmLista: ConstraintLayout = findViewById(R.id.exibindoConteudos)
        val paginaExibindoConteudo: ConstraintLayout = findViewById(R.id.conteudoNaTela)

        paginaComConteudoEmLista.visibility = View.GONE
        paginaExibindoConteudo.visibility = View.VISIBLE

        //marcar como conteudo visto
        sawContent = sawContent+"!@#"+conteudo
        databaseReference.child("user_achievement").child(userBd).child(disciplina).child("sawContent").setValue(sawContent)
        //salva no shared
        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()
        editor.putString(disciplina+"Saw", sawContent)
        editor.apply()



        Toast.makeText(this, "Carregando as informações", Toast.LENGTH_SHORT).show()
        chamaDialog()

        val arrayAlinhamento: MutableList<String> = ArrayList()
        val arrayCor: MutableList<String> = ArrayList()
        val arrayDescricao: MutableList<String> = ArrayList()
        val arrayItem: MutableList<String> = ArrayList()

        val rootRef = databaseReference.child("conteudos").child(escola+turma).child(disciplina).child(bdConteudo)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
                encerraDialog()
            }

            override fun onDataChange(p0: DataSnapshot) {
                //TODO("Not yet implemented")
                var values: String
                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()

                //guardar que visualizou este conteudo
                editor.putString(disciplina+"_"+conteudo, "sim")
                editor.apply()

                values = p0.child("titulo").value.toString()
                val etTitulo: TextView = findViewById(R.id.conteudoNaTela_tvTitulo)
                etTitulo.setText(values)

                values = p0.child("entradas").value.toString()

                val entradas = values.toInt()
                var cont=0
                while (cont<entradas){
                    values = p0.child("valores_entradas").child(cont.toString()).child("alinhamento").value.toString()
                    arrayAlinhamento.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("cor").value.toString()
                    arrayCor.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("descricao").value.toString()
                    arrayDescricao.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("item").value.toString()
                    arrayItem.add(values)
                    cont++
                }

                encerraDialog()
                var adapter: profMontaContAdapter = profMontaContAdapter(this@SalaDeAulaActivity, arrayItem, arrayDescricao, arrayAlinhamento, arrayCor)

                //chame a recyclerview
                var recyclerView: RecyclerView = findViewById(R.id.conteudoNaTela_recyclerView)

                //define o tipo de layout (linerr, grid)
                var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this@SalaDeAulaActivity)

                //coloca o adapter na recycleview
                recyclerView.adapter = adapter

                recyclerView.layoutManager = linearLayoutManager

                // Notify the adapter for data change.
                adapter.notifyDataSetChanged()

                recyclerView.addOnItemTouchListener(RecyclerTouchListener(this@SalaDeAulaActivity, recyclerView!!, object: ClickListener{

                    override fun onClick(view: View, position: Int) {
                        //Log.d("teste", arrayItem.get(position))
                        //Toast.makeText(this@MainActivity, !! aNome.get(position).toString(), Toast.LENGTH_SHORT).show()
                    }

                    override fun onLongClick(view: View?, position: Int) {

                    }
                }))

                //aqui cliques de botão para fazer questoes

            }


            //EncerraDialog()

        })

        val conteudoNaTela_Menu: ConstraintLayout = findViewById(R.id.conteudoNaTela_Menu)


        val btnAbreFechaMenu: Button = findViewById(R.id.conteudoNaTela_Menu_btnAbreFechaMenu)
        btnAbreFechaMenu.setOnClickListener {
            if (conteudoNaTela_Menu.isVisible){
                btnAbreFechaMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_left_black_24dp, 0, 0);
                conteudoNaTela_Menu.visibility = View.GONE
            } else {
                btnAbreFechaMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_right_black_24dp, 0, 0);
                conteudoNaTela_Menu.visibility = View.VISIBLE
            }
        }

        val btnAbreQuestoes: Button = findViewById(R.id.conteudoNaTela_Menu_btnQuestoes)
        btnAbreQuestoes.setOnClickListener {
            abreQuestoes(conteudo, disciplina)
        }

        val btnHelpMe: Button = findViewById(R.id.conteudoNaTela_Menu_btnAjuda)
        btnHelpMe.setOnClickListener {
            helpMePlease(disciplina, conteudo, bdConteudo)
        }

    }

    fun AddColaboracaoDaTurma(){

        val rootRef = databaseReference.child("turmas").child(escola+turma)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
                encerraDialog()
            }

            override fun onDataChange(p0: DataSnapshot) {
                //TODO("Not yet implemented")

                if (p0.child("colaboracao").exists()) {

                    var colaboracaoTurma = p0.child("colaboracao").getValue().toString()
                    colaboracaoTurma = (colaboracaoTurma.toInt() + 1).toString()
                    databaseReference.child("turmas").child(escola + turma).child("colaboracao")
                        .setValue(colaboracaoTurma)

                } else {
                    databaseReference.child("turmas").child(escola + turma).child("colaboracao").setValue(0)
                }
            }

            //EncerraDialog()

        })

    }










    //Pedido de ajuda
    fun helpMePlease(disciplina: String, conteudo: String, bdConteudo: String){

        val paginaAjuda: ConstraintLayout = findViewById(R.id.conteudoNaTela_HelpMe)
        val paginaConteudo: ConstraintLayout = findViewById(R.id.conteudoNaTela_Menu)
        val menuzinho: ConstraintLayout = findViewById(R.id.conteudoNaTela_Menu)
        val btnMenuzinho: Button = findViewById(R.id.conteudoNaTela_Menu_btnAbreFechaMenu)
        menuzinho.visibility = View.GONE
        btnMenuzinho.visibility = View.GONE

        paginaConteudo.visibility = View.INVISIBLE
        paginaAjuda.visibility = View.VISIBLE

        val btnFechar: Button = findViewById(R.id.conteudoNaTela_HelpMe_btnFechar)
        btnFechar.setOnClickListener {

            paginaConteudo.visibility = View.VISIBLE
            paginaAjuda.visibility = View.GONE
            menuzinho.visibility = View.VISIBLE
            btnMenuzinho.visibility = View.VISIBLE

        }

        val editText: EditText = findViewById(R.id.conteudoNaTela_HelpMe_etPreoblema)

        val btnEnviarPedidoDeHelp: Button = findViewById(R.id.conteudoNaTela_HelpMe_btnEnviar)
        btnEnviarPedidoDeHelp.setOnClickListener {
            if (editText.text.isEmpty()){
                editText.requestLayout()
                editText.setError("Fale sobre sua dúvida")
            } else {
                val newCad: DatabaseReference =
                    databaseReference.child("HelpMePlease").child(disciplina).push()
                newCad.child("bdConteudo").setValue(bdConteudo)
                newCad.child("conteudo").setValue(conteudo)
                newCad.child("disciplina").setValue(disciplina)
                newCad.child("estudante").setValue(nome)
                newCad.child("turma").setValue(turma)
                newCad.child("escola").setValue(escola)
                newCad.child("bdAluno").setValue(userBd)
                newCad.child("situacao").setValue("aberta")
                newCad.child("duvida").setValue(editText.text.toString())
                val date = GetDate()
                newCad.child("data").setValue(date)
                showtoast("Sua dúvida foi enviada. Aguarde algum professor responder.")
                btnFechar.performClick()
            }

        }

    }
    //pega  a data
    private fun GetDate () : String {

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())

        return currentDate
    }






    //carrega os conteudos que o aluno viu ou mastered
    fun getSawContents(disciplina: String){

        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()
        sawContent = sharedPref.getString(disciplina+"Saw", "nao").toString()
        masteredContent = sharedPref.getString(disciplina+"Mastered", "nao").toString()

        //if (!sawContent.equals("nao") || !masteredContent.equals("nao")){
            //já tem o que queremos
          //  montaRecycleConteudosDaTurmaJaComDisciplina(disciplina)
        //} else {

            val rootRef = databaseReference.child("user_achievement").child(userBd).child(disciplina)
            rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    //TODO("Not yet implemented")

                    sawContent = p0.child("sawContent").value.toString()
                    masteredContent = p0.child("masteredContent").value.toString()

                    val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                    val editor = sharedPref.edit()
                    editor.putString(disciplina+"Saw", sawContent)
                    editor.putString(disciplina+"Mastered", masteredContent)
                    editor.apply()

                    montaRecycleConteudosDaTurmaJaComDisciplina(disciplina)
                }


                //EncerraDialog()

            })

            /*
            //databaseReference.child("user_achievement").child(userBd).child("sawContent").setValue(masteredContent)
            val rootRef = databaseReference.child("user_achievement").child(userBd)
            rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    //TODO("Not yet implemented")

                    sawContent = p0.child("sawContent").value.toString()
                    masteredContent = p0.child("masteredContent").value.toString()

                    montaRecycleConteudosDaTurmaJaComDisciplina(disciplina)
                }


                //EncerraDialog()

            })


             */
        //}


    }
    //Fim




    //aqui é a query e a montagem da questão
    fun abreQuestoes(conteudo: String, disciplina: String){

        var temPerguntas  = false //evitar que o metodo prossiga caso nao tenha conteudos

        tentativasNaRodada=0
        acertosNaRodada=0

        val paginaConteudo: ConstraintLayout = findViewById(R.id.conteudoNaTela)
        val paginaQuestoes: ConstraintLayout = findViewById(R.id.RespondendoQuestao)

        paginaConteudo.visibility = View.GONE
        paginaQuestoes.visibility = View.VISIBLE

        val arrayPergunta: MutableList<String> = ArrayList()
        val arrayImagem: MutableList<String> = ArrayList()
        val arrayPerguntaExtra: MutableList<String> = ArrayList()
        val arrayOpcaoA: MutableList<String> = ArrayList()
        val arrayOpcaoB: MutableList<String> = ArrayList()
        val arrayOpcaoC: MutableList<String> = ArrayList()
        val arrayOpcaoD: MutableList<String> = ArrayList()
        val arrayOpcaoE: MutableList<String> = ArrayList()
        val arrayOpcaoCorreta: MutableList<String> = ArrayList()
        val arrayBdDaQuestao: MutableList<String> = ArrayList()
        val arrayTentativas: MutableList<String> = ArrayList()
        val arrayAcertos: MutableList<String> = ArrayList()

        chamaDialog()

        val tvConteudo: TextView = findViewById(R.id.respondendoQuestao_tvConteudo)
        tvConteudo.setText("Você está vendo: "+conteudo)

        val rootRef = databaseReference.child("questões").child(disciplina)
        rootRef.orderByChild("conteudo").equalTo(conteudo)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            arrayBdDaQuestao.add(querySnapshot.key.toString())

                            var values = querySnapshot.child("img").value.toString()

                            arrayImagem.add(values)

                            /*
                            if (values.equals("nao")){
                                //do nothing
                                imagem.visibility = View.GONE
                            } else {
                                Glide.with(this@SalaDeAulaActivity).load(values).into(imagem)
                            }

                             */


                            temPerguntas=true

                            values = querySnapshot.child("pergunta").value.toString()
                            //pergunta.setText(values)
                            arrayPergunta.add(values)

                            values = querySnapshot.child("textoExtra").value.toString()
                            arrayPerguntaExtra.add(values)

                            values = querySnapshot.child("opcaoA").value.toString()
                            arrayOpcaoA.add(values)

                            values = querySnapshot.child("opcaoB").value.toString()
                            arrayOpcaoB.add(values)

                            values = querySnapshot.child("opcaoC").value.toString()
                            arrayOpcaoC.add(values)

                            values = querySnapshot.child("opcaoD").value.toString()
                            arrayOpcaoD.add(values)

                            values = querySnapshot.child("opcaoE").value.toString()
                            arrayOpcaoE.add(values)

                            values = querySnapshot.child("opcaoCorreta").value.toString()
                            arrayOpcaoCorreta.add(values)

                            values = querySnapshot.child("tentativas").value.toString()
                            arrayTentativas.add(values)

                            values = querySnapshot.child("acertos").value.toString()
                            arrayAcertos.add(values)


                        }

                    } else {

                        //showToast("Não existem questões cadastradas por você para este conteúdo.")
                        paginaConteudo.visibility = View.VISIBLE
                        paginaQuestoes.visibility = View.GONE
                        showtoast("Não existem questões cadastradas por você para este conteúdo.")

                    }

                    encerraDialog()
                    //agora vamos preparar as perguntas. Precisamos sortea-las para não repetir ordem. E também precisamos embaralhar as perguntas.
                    //primeiro vai sortear um numero pra começar.
                    //para garantir que sempre variemos, vamso sortear um número entre 1 e 2. Se for um vai ser ordem crescente e se for 2 vai ser ordem decrescente. Assim garantimos que a proxima pergunta nao possa ser predita.

                    if (temPerguntas) {
                        val ordem = rand(1, 2)  //sorteia 1 ou 2 aqui. Isso altera a ordem das coisas. Se for 1 é na ordem natural, se for 2 vai ser de baixo pra cima.
                        var inicio = 0
                        if (arrayBdDaQuestao.size == 1) {
                            //faça nada
                        } else {
                            inicio = rand(0, (arrayBdDaQuestao.size - 1)
                            )  //tem que ser -1 pq o numero maximo do array dá erro. Digamos, array size=1...na verdade tem que pegar posicao 0
                        }


                        val arrayOrdenamento: MutableList<Int> = ArrayList() //o valor vai servir como index


                        if (ordem == 1) {
                            var cont = 0
                            while (cont < arrayBdDaQuestao.size) {
                                if (inicio >= arrayBdDaQuestao.size) { //se atingir o tamanho limite volta pro inicio para pegar as primeiras questões, Já que não começa do zero.
                                    inicio = 0
                                }
                                arrayOrdenamento.add(inicio)
                                inicio++
                                cont++
                            }
                        } else {
                            var cont = 0
                            while (cont < arrayBdDaQuestao.size) {
                                arrayOrdenamento.add(inicio)
                                cont++
                                inicio = inicio - 1
                                if (inicio < 0) {
                                    inicio = arrayBdDaQuestao.size-1 //tem que ser -1 pq o array da questão estrapola. Exemplo, se o array é tamanho dois ele tem posi~~ao 0 e 1. Logo, se o inicio fosse 2 (tamanho do array) daria erro pois ia buscar posição que nao existe.
                                }
                            }
                        }

                        var contQuest = 0
                        val btnProxima: Button = findViewById(R.id.respondendoQuestao_btnProxima)
                        btnProxima.setOnClickListener {


                            var cont=0
                            while (cont<arrayOrdenamento.size){
                                cont++
                            }

                            if (contQuest < arrayBdDaQuestao.size) {
                                val position =
                                    arrayOrdenamento.get(contQuest) //vai pegar o numero da vez  --pegar contQuest-1

                                exibePergunta(
                                    position,
                                    arrayPergunta.get(position),
                                    arrayImagem.get(position),
                                    arrayPerguntaExtra.get(position),
                                    arrayOpcaoA.get(position),
                                    arrayOpcaoB.get(position),
                                    arrayOpcaoC.get(position),
                                    arrayOpcaoD.get(position),
                                    arrayOpcaoE.get(position),
                                    arrayOpcaoCorreta.get(position),
                                    arrayBdDaQuestao.get(position),
                                    arrayTentativas.get(position),
                                    arrayAcertos.get(position),
                                    disciplina,
                                    conteudo
                                )
                                contQuest++
                            } else {
                                //acabou
                                //val paginaPergunta: ConstraintLayout = findViewById(R.id.RespondendoQuestao)
                                //val paginaResulFinal: ConstraintLayout = findViewById(R.id.Questao_TelaFinal)
                                //paginaPergunta.visibility = View.GONE
                                //paginaResulFinal.visibility = View.VISIBLE
                                mostraResultados(conteudo, disciplina)
                            }
                        }

                        btnProxima.performClick() //faz este click pra iniciar o processo e chamar a primeira questão

                        val btnEncerra: Button = findViewById(R.id.respondendoQuestao_btnEncerrar)
                        btnEncerra.setOnClickListener {
                            mostraResultados(conteudo, disciplina)
                        }
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })

    }

    fun randomizeRespostaCerta(opcaoCorreta: String){
        val sorteio = rand(1, 5)  //1 é A, 2 é B, 3 C, 4 D e 5 é E

        if (sorteio==1){

        }

    }


    fun exibePergunta(position: Int, pergunta: String, imagem: String, txtExtra: String, opcaoA:String, opcaoB:String, opcaoC:String,
                      opcaoD:String, opcaoE:String, opcaoCorreta:String, bd:String, tentativas: String, acertos: String, disciplina: String, conteudo: String){

        var opcaoAfinal="n"
        var opcaoBfinal="n"
        var opcaoCfinal="n"
        var opcaoDfinal="n"
        var opcaoEfinal="n"
        var opcaoCorretaFinal = "n"

        //embaralhando as questões
        val sorteio = rand(1, 5)  //1 é A, 2 é B, 3 C, 4 D e 5 é E
        if (sorteio==1){
            //n muda nada
            opcaoAfinal = opcaoA
            opcaoBfinal = opcaoB
            opcaoCfinal = opcaoC
            opcaoDfinal = opcaoD
            opcaoEfinal = opcaoE
            opcaoCorretaFinal = opcaoCorreta
        } else if (sorteio==2){
            //letra A vai para letra B
            opcaoBfinal = opcaoA
            opcaoCfinal = opcaoB
            opcaoDfinal = opcaoC
            opcaoEfinal = opcaoD
            opcaoAfinal = opcaoE


            if (opcaoCorreta.equals("a")){
                opcaoCorretaFinal="b"
            } else if (opcaoCorreta.equals("b")){
                opcaoCorretaFinal="c"
            }else if (opcaoCorreta.equals("c")){
                opcaoCorretaFinal="d"
            }else if (opcaoCorreta.equals("d")){
                opcaoCorretaFinal="e"
            }else if (opcaoCorreta.equals("e")){
                opcaoCorretaFinal="a"
            }

        } else if (sorteio==3){
            //letra A vai para letra C
            opcaoCfinal = opcaoA
            opcaoDfinal = opcaoB
            opcaoEfinal = opcaoC
            opcaoAfinal = opcaoD
            opcaoBfinal = opcaoE

            if (opcaoCorreta.equals("a")){
                opcaoCorretaFinal="c"
            } else if (opcaoCorreta.equals("b")){
                opcaoCorretaFinal="d"
            }else if (opcaoCorreta.equals("c")){
                opcaoCorretaFinal="e"
            }else if (opcaoCorreta.equals("d")){
                opcaoCorretaFinal="a"
            }else if (opcaoCorreta.equals("e")){
                opcaoCorretaFinal="b"
            }

        } else if (sorteio==4){
            //letra A vai para letra D
            opcaoDfinal = opcaoA
            opcaoEfinal = opcaoB
            opcaoAfinal = opcaoC
            opcaoBfinal = opcaoD
            opcaoCfinal = opcaoE


            if (opcaoCorreta.equals("a")){
                opcaoCorretaFinal="d"
            } else if (opcaoCorreta.equals("b")){
                opcaoCorretaFinal="e"
            }else if (opcaoCorreta.equals("c")){
                opcaoCorretaFinal="a"
            }else if (opcaoCorreta.equals("d")){
                opcaoCorretaFinal="b"
            }else if (opcaoCorreta.equals("e")){
                opcaoCorretaFinal="c"
            }

        } else if (sorteio==5){
            //letra A vai para letra D
            opcaoEfinal = opcaoA
            opcaoAfinal = opcaoB
            opcaoBfinal = opcaoC
            opcaoCfinal = opcaoD
            opcaoDfinal = opcaoE


            if (opcaoCorreta.equals("a")){
                opcaoCorretaFinal="e"
            } else if (opcaoCorreta.equals("b")){
                opcaoCorretaFinal="a"
            }else if (opcaoCorreta.equals("c")){
                opcaoCorretaFinal="b"
            }else if (opcaoCorreta.equals("d")){
                opcaoCorretaFinal="c"
            }else if (opcaoCorreta.equals("e")){
                opcaoCorretaFinal="d"
            }

        }
        //FIM do embaralho da questao


        val imageView: ImageView = findViewById(R.id.respondendoQuestao_imageView)
        val txtpergunta: TextView = findViewById(R.id.respondendoQuestao_tvPergunta)
        val textoExtra: TextView = findViewById(R.id.respondendoQuestao_tvPerguntaExtra)
        val txtOpcaoA: TextView = findViewById(R.id.question_adicionando_lineA_txt)
        val txtOpcaoB: TextView = findViewById(R.id.question_adicionando_lineB_txt)
        val txtOpcaoC: TextView = findViewById(R.id.question_adicionando_lineC_txt)
        val txtOpcaoD: TextView = findViewById(R.id.question_adicionando_lineD_txt)
        val txtOpcaoE: TextView = findViewById(R.id.question_adicionando_lineE_txt)

        val txtRate: TextView = findViewById(R.id.respondendoQuestao_tvRate)
        Log.d("teste", "acertos "+acertos)
        Log.d("teste", "tentativas "+tentativas)
        val rate = rateQuestion(tentativas, acertos, bd, disciplina)
        txtRate.setText("Dificuldade: "+rate)

        txtpergunta.setText(pergunta)
        if (imagem.equals("nao")){
            imageView.visibility = View.GONE
        } else {
            Glide.with(this).load(imagem).into(imageView)
            var zoom = false
            imageView.setOnClickListener {
                zoomEffectQuestao(0, imageView, zoom)
                if (zoom){
                    zoom=false
                } else {
                    zoom=true
                }
            }

        }
        if (textoExtra.equals("nao")){
            textoExtra.visibility = View.GONE
        } else {
            textoExtra.setText(txtExtra)
        }
        txtOpcaoA.setText(opcaoAfinal)
        txtOpcaoB.setText(opcaoBfinal)
        txtOpcaoC.setText(opcaoCfinal)
        txtOpcaoD.setText(opcaoDfinal)
        txtOpcaoE.setText(opcaoEfinal)

        val btnA: Button = findViewById(R.id.question_adicionando_lineA_btn)
        val btnB: Button = findViewById(R.id.question_adicionando_lineB_btn)
        val btnC: Button = findViewById(R.id.question_adicionando_lineC_btn)
        val btnD: Button = findViewById(R.id.question_adicionando_lineD_btn)
        val btnE: Button = findViewById(R.id.question_adicionando_lineE_btn)

        val layA: ConstraintLayout = findViewById(R.id.question_adicionando_lineA)
        val layB: ConstraintLayout = findViewById(R.id.question_adicionando_lineB)
        val layC: ConstraintLayout = findViewById(R.id.question_adicionando_lineC)
        val layD: ConstraintLayout = findViewById(R.id.question_adicionando_lineD)
        val layE: ConstraintLayout = findViewById(R.id.question_adicionando_lineE)

        val btnProxima: Button = findViewById(R.id.respondendoQuestao_btnProxima)

        btnA.setOnClickListener {
            if (opcaoCorretaFinal.equals("a")){
                computaAcerto(disciplina, bd,tentativas, acertos, position)
            } else {
                computaErro(disciplina, bd, tentativas, position)
            }
            btnProxima.performClick()
        }
        btnB.setOnClickListener {
            if (opcaoCorretaFinal.equals("b")){
                computaAcerto(disciplina, bd,tentativas, acertos, position)
            } else {
                computaErro(disciplina, bd, tentativas, position)
            }
            btnProxima.performClick()
        }
        btnC.setOnClickListener {
            if (opcaoCorretaFinal.equals("c")){
                computaAcerto(disciplina, bd,tentativas, acertos, position)
            } else {
                computaErro(disciplina, bd, tentativas, position)
            }
            btnProxima.performClick()
        }
        btnD.setOnClickListener {
            if (opcaoCorretaFinal.equals("d")){
                computaAcerto(disciplina, bd,tentativas, acertos, position)
            } else {
                computaErro(disciplina, bd, tentativas, position)
            }
            btnProxima.performClick()
        }
        btnE.setOnClickListener {
            if (opcaoCorretaFinal.equals("e")){
                computaAcerto(disciplina, bd,tentativas, acertos, position)
            } else {
                computaErro(disciplina, bd, tentativas, position)
            }
            btnProxima.performClick()
        }

        layA.setOnClickListener {
            btnA.performClick()
        }

        layB.setOnClickListener {
            btnB.performClick()
        }

        layC.setOnClickListener {
            btnC.performClick()
        }
        layD.setOnClickListener {
            btnD.performClick()
        }
        layE.setOnClickListener {
            btnE.performClick()
        }

        val btnReportarErro: Button = findViewById(R.id.respondendoQuestao_btnProblema)
        btnReportarErro.setOnClickListener {
            val paginaErro: ConstraintLayout = findViewById(R.id.telaReportaErro)
            paginaErro.visibility = View.VISIBLE
            val editText: EditText = findViewById(R.id.telaReportaErro_editText)
            val btnReportarErro: Button = findViewById(R.id.telaReportaErro_btnReportar)
            val btnCancelar: Button = findViewById(R.id.telaReportaErro_btnCancelar)

            btnCancelar.setOnClickListener {
                btnReportarErro.setOnClickListener { null }
                paginaErro.visibility = View.GONE
            }

            btnReportarErro.setOnClickListener {
                if(editText.text.isEmpty()){
                    editText.requestFocus()
                    editText.setError("Informe o erro")
                } else {
                    val newCad: DatabaseReference =
                        databaseReference.child("questões_com_erro").child(disciplina).push()
                        newCad.child("bdQuestao").setValue(bd)
                        newCad.child("disciplina").setValue(disciplina)
                        newCad.child("erro").setValue(editText.text.toString())
                        newCad.child("pergunta").setValue(pergunta)
                        showtoast("O erro foi reportado")
                        btnCancelar.performClick()
                }
            }
        }


        val btnEncerrar: Button = findViewById(R.id.respondendoQuestao_btnEncerrar)
        btnEncerrar.setOnClickListener {
            mostraResultados(conteudo,disciplina)
        }

    }

    fun computaAcerto(disciplina: String, bd: String, tentativas: String, acertos: String, position: Int){

        AddColaboracaoDaTurma()

        //salva no bd
        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        //adiciona um exercicio a meta
        var x = sharedPref.getString("atividadesDaMeta", "nao")

        var exerciciosFeitos: Int
        if (x.equals("nao")){
            exerciciosFeitos=0
        } else {
            exerciciosFeitos = x!!.toInt()
        }
        exerciciosFeitos++
        editor.putString("atividadesDaMeta", exerciciosFeitos.toString())
        //atualiza a meta no shared
        editor.apply()


        acertosNaRodada++
        tentativasNaRodada++
        arrayErroAcerto.add( "sim")
        //atualiza no bd
        if (isNetworkAvailable(this)){
            val tentativasParaBd = tentativas.toInt()+1
            val acertosParaBd = acertos.toInt()+1
            databaseReference.child("questões").child(disciplina).child(bd).child("tentativas").setValue(tentativasParaBd)
            databaseReference.child("questões").child(disciplina).child(bd).child("acertos").setValue(acertosParaBd)
        }
    }

    fun computaErro(disciplina: String, bd: String, tentativas: String, position: Int){


        //salva no bd
        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        //adiciona um exercicio a meta
        var x = sharedPref.getString("atividadesDaMeta", "nao")
        var exerciciosFeitos: Int
        if (x.equals("nao")){
            exerciciosFeitos=0
        } else {
            exerciciosFeitos = x!!.toInt()
        }
        exerciciosFeitos++
        editor.putString("atividadesDaMeta", exerciciosFeitos.toString())
        //atualiza a meta no shared
        editor.apply()


        tentativasNaRodada++

        arrayErroAcerto.add( "nao")

        if (isNetworkAvailable(this)){
            val tentativasParaBd = tentativas.toInt()+1
            databaseReference.child("questões").child(disciplina).child(bd).child("tentativas").setValue(tentativasParaBd)
        }
    }

    //este é o fim das perguntas
    fun mostraResultados(conteudo: String, disciplina: String){

        val paginaPergunta: ConstraintLayout = findViewById(R.id.RespondendoQuestao)
        paginaPergunta.visibility = View.GONE
        val paginaResulFinal: ConstraintLayout = findViewById(R.id.Questao_TelaFinal)
        paginaResulFinal.visibility = View.VISIBLE

        val txtResultado: TextView = findViewById(R.id.Questao_TelaFinal_tvDesempenho)
        val desempenho = (acertosNaRodada / tentativasNaRodada) * 100
        //txtResultado.setText("Você acertou: "+acertosNaRodada+" em  "+tentativasNaRodada+" tentativas.\n\nSeu desempenho foi de: "+desempenho+"%.")
        //vou salvar a linha acima no final, para poder incorporar o resultado

        val desempenhoParabens: TextView = findViewById(R.id.Questao_TelaFinal_txtMensagemParabens)
        val imageMedalha: ImageView = findViewById(R.id.Questao_TelaFinal_imageViewMedalha)
        if (desempenho>=70){
            desempenhoParabens.setText("Parabéns! Você ganhou uma medalha pelo ótimo aproveitamento")
            desempenhoParabens.visibility = View.VISIBLE
            imageMedalha.visibility = View.VISIBLE

            //salva no bd
            val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
            val editor = sharedPref.edit()

            if (!masteredContent.contains(conteudo)){   //se já estiver, n precisa o aluno já possui
                masteredContent = masteredContent+"!@#"+conteudo
                databaseReference.child("user_achievement").child(userBd).child(disciplina).child("masteredContent").setValue(masteredContent)
                //salva no shared
                editor.putString(disciplina+"Mastered", masteredContent)

            }

            //agora o desempenho
            val ondeSalvarBd = adjustDisciplinaToStoreDesempenho(disciplina)
            val historico = sharedPref.getString(ondeSalvarBd, null)
            val historicoFinal: Float
            if (historico==null || historico.equals("null")){
                historicoFinal=0.0.toFloat()
            } else {
                historicoFinal = historico.toFloat()
            }

            val novoDes = desempenho.toFloat()
            val desempenhoFinal = (historicoFinal+novoDes)/2

            //salva no sharedpregs
            editor.putString(ondeSalvarBd, desempenhoFinal.toString())
            editor.apply()

            //agora salvar no bd
            databaseReference.child("usuarios").child(userBd).child(ondeSalvarBd).setValue(desempenhoFinal)

            if (historicoFinal<novoDes){
                //melhorou
                val melhora = novoDes-historicoFinal
                txtResultado.setText("Parabéns! Você acertou: "+acertosNaRodada+" em  "+tentativasNaRodada+" tentativas.\n\nSeu desempenho foi de: "+desempenho+"%.\n\nVocê melhorou "+melhora+" % nesta área do conhecimento!")
            } else {
                val piora = historicoFinal-novoDes
                txtResultado.setText("Você acertou: "+acertosNaRodada+" em  "+tentativasNaRodada+" tentativas.\n\nSeu desempenho foi de: "+desempenho+"%.\n\nVocê piorou "+piora+" % nesta área do conhecimento.")
            }

        }

        val btnFechar: Button = findViewById(R.id.Questao_TelaFinal_btnFinalizar)
        btnFechar.setOnClickListener {
            finish()
        }



        //essa recycleview exibe cada questão que o user acertou ou errou
        var adapter: resultadoPerguntasAdapter = resultadoPerguntasAdapter(this, arrayErroAcerto)

        //chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.Questao_TelaFinal_recycleView)

        //define o tipo de layout (linerr, grid)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        //coloca o adapter na recycleview
        recyclerView.adapter = adapter

        recyclerView.layoutManager = linearLayoutManager

        // Notify the adapter for data change.
        adapter.notifyDataSetChanged()


    }

    fun rateQuestion(tentativas: String, acertos: String, bd: String, disciplina: String): String{


        Log.d("teste", "acertos em int  e "+acertos.toInt())
        Log.d("teste", "acertos em str  e "+acertos)
        Log.d("teste", "tentagtivas em int  e "+tentativas.toInt())
        Log.d("teste", "tentativas em str  e "+tentativas)
        var rate = "nao"
        if (acertos.toInt()==0 && tentativas.toInt()==0){
            rate = "nao avaliada"
        } else {

            val acertosInt = acertos.toInt()
            val tentativasInt = tentativas.toInt()
            //val aproveitamento = (acertos.toDouble() / tentativas.toInt()) //* 100
            val aproveitamento = (acertosInt.toDouble() / tentativasInt).toDouble() * 100
            //var percentage = (count.toDouble() / totalCount) * 100

            Log.d("teste", "aproveitamento é "+aproveitamento)
            if (aproveitamento >= 85) {
                rate = "muito fácil"
            } else if (aproveitamento >= 70) {
                rate = "fácil"
            } else if (aproveitamento >= 55) {
                rate = "media"
            } else if (aproveitamento >= 35) {
                rate = "díficil"
            } else {
                rate = "muito díficil"
            }
                /*
            //erro de logica. Se o aluno na primeira vez errase entrava como nao avaliada. Nao avaliada somente quando é criada
            else {
                //aqui é igual a 0
                rate = "nao avaliada"
            }

                 */
        }

        //atualiza no bd
        databaseReference.child("questões").child(disciplina).child(bd).child("rating").setValue(rate)

        return rate
    }

    //as disciplinas estão agrupadas. Então portugues 1 e 2 são salvos no mesmo bd. Aqui vamos fazer essa adaptação. Aqui a fun vai retornar o formatado pra salvar no BD
    fun adjustDisciplinaToStoreDesempenho(disciplina: String) : String{

        var ajustada: String = "nao"

        if (disciplina.equals("Português 1") || disciplina.equals("Português 2")){
            ajustada = "portugues"
        } else if (disciplina.equals("Matemática 1") || disciplina.equals("Matemática 2")){
            ajustada = "matematica"
        } else if (disciplina.equals("História") || disciplina.equals("Geografia")){
            ajustada = "histgeo"
        } else if (disciplina.equals("Ciências") || disciplina.equals("Biologia") || disciplina.equals("Física") || disciplina.equals("Química")){
            ajustada = "ciencias"
        } else {
            ajustada = "outras"
        }

        return ajustada

    }

    //fim da perguntas


    //zoom
    //0 é questão, 1 é conteudo
    fun zoomEffectQuestao (questOrCont: Int, imageView: ImageView, zoom: Boolean){


        if (questOrCont==0){

            if (zoom){
                //está com zoom
                //retirar zoom
                val cL = findViewById(R.id.respondendoQuestao_imageView) as ImageView
                val lp =  cL.layoutParams as ConstraintLayout.LayoutParams
                lp.width = lp.width/2
                lp.height = lp.height/2
                cL.layoutParams = lp
            } else {
                //dar zoom
                val cL = findViewById(R.id.respondendoQuestao_imageView) as ImageView
                val lp =  cL.layoutParams as ConstraintLayout.LayoutParams
                lp.width = lp.width*2
                lp.height = lp.height*2
                cL.layoutParams = lp

            }
        }


    }
















    fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    fun showtoast(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }













    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetworkInfo: NetworkInfo? = null
        activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    //click listener da primeira recycleview
    interface ClickListener {
        fun onClick(view: View, position: Int)

        fun onLongClick(view: View?, position: Int)
    }


    internal class RecyclerTouchListener(context: Context, recyclerView: RecyclerView, private val clickListener: ClickListener?) : RecyclerView.OnItemTouchListener {

        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }
    }


    fun chamaDialog() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        ) //este serve para bloquear cliques que pdoeriam dar erros
        val layout = findViewById(R.id.LayoutProgressBar) as RelativeLayout
        layout.visibility = View.VISIBLE
        val spinner = findViewById(R.id.progressBar1) as ProgressBar
        spinner.visibility = View.VISIBLE
    }

    //este método torna invisivel um layout e encerra o dialogbar spinner.
    fun encerraDialog() {
        val layout = findViewById(R.id.LayoutProgressBar) as RelativeLayout
        val spinner = findViewById(R.id.progressBar1) as ProgressBar
        layout.visibility = View.GONE
        spinner.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) //libera os clicks
    }
}
