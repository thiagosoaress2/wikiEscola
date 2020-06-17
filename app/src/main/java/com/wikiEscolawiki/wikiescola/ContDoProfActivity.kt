package com.wikiEscolawiki.wikiescola

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class ContDoProfActivity : AppCompatActivity() {

    var escola = "nao"
    val turmas: MutableList<String> = ArrayList()
    //var qntTurmas = 0
    var disciplina = "nao"
    var userBd = "nao"

    var turmaSelecionada = "nao"

    private lateinit var databaseReference: DatabaseReference

    //variaveis do envio de foto
    private lateinit var filePath: Uri
    private var urifinal: String = "nao"
    private lateinit var mphotoStorageReference: StorageReference
    private lateinit var mFireBaseStorage: FirebaseStorage

    val arrayItens: MutableList<String> = ArrayList() //este array precisava ser global por causa da imagem. Os outros são locais em adicionandoConteudoMetodo()
    val arrayDescItens: MutableList<String> = ArrayList()
    val arrayAlinhamento: MutableList<String> = ArrayList()
    val arrayCor: MutableList<String> = ArrayList()

    var colaboracao: Int = 0


    lateinit var adapterSpecific: profMontaContAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cont_do_prof)

        databaseReference = FirebaseDatabase.getInstance().reference
        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference


        escola = intent.getStringExtra("escola").toString()
        //qntTurmas = intent.getStringExtra("qntTurmas").toInt()
        disciplina = intent.getStringExtra("disciplina").toString()
        userBd = intent.getStringExtra("userBd").toString()
        colaboracao = intent.getIntExtra("colaboracao", 0).toInt()
        Log.d("teste", "userbd "+userBd)

        val x = intent.getStringExtra("turmas")

        Log.d("teste", "turmas em x é "+x)
        if (x.contains(";")){
            val tokens = StringTokenizer(x, ";") //”*” este é delim
            val size = tokens.countTokens()
            var cont=0
            while (cont<size){
                val value = tokens.nextToken()
                turmas.add(value)
                cont++
            }
        } else {
            turmas.add(x)
        }
        /*
        if (x.contains(";")){
            var cont=0
            val tokens = StringTokenizer(x, ";") //imagine a string with fruit; they
            Log.d("teste", "qntTurmas é "+qntTurmas.toInt())
            Log.d("teste", "turmas em x é "+x)
            while (cont<qntTurmas.toInt()){
                var y = tokens.nextToken() // this will contain "Fruit"
                turmas.add(y)
                cont++
            }
        } else {
            turmas.add(x)
        }

         */



        if (turmas.size!=0){
            if (turmas.get(0).equals("null") || turmas.get(0).equals("nao")){
                showToast("Você não possui turmas cadastradas")
            } else {
                montaRecyclerViewTurmas()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        setupPermissions()

    }

    //aqui monta a primeira recyclerview que exibe o nome das turmas
    //ao clicar chama query com informações da turma
    fun montaRecyclerViewTurmas(){

        var adapter: turmasAdapter = turmasAdapter(this, turmas)

//chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.contDoPrf_index_recyclerviewComTurmas)

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
                //Toast.makeText(this@ContDoProfActivity, turmas.get(position).toString(), Toast.LENGTH_SHORT).show()
                queryInfoTurma(turmas.get(position))

            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))




    }

    //esta query é quando clicou na turma e tá buscando os conteudos dela
    fun queryInfoTurma(turma: String){

        val conteudos: MutableList<String> = ArrayList()

        //val rootRef = databaseReference.child("turmas").child(escola+turma).child("conteudos").child(disciplina)
        chamaDialog()
        turmaSelecionada = escola+turma
        val rootRef = databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina)
        rootRef.orderByChild("disciplina").equalTo(disciplina)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            var x = querySnapshot.child("turma").getValue().toString()
                            conteudos.add(x)
                            //ajusta texto da proxima tela
                            val tvMensagem: TextView = findViewById(R.id.detalhesDasTurmas_tvMensagem)
                            tvMensagem.setText("Exibindo conteúdos cadastrados para esta turma")

                        }

                    } else {
                        //showToast("Não existem conteúdos cadastrados na sua disciplina para esta turma.")
                        val tvMensagem: TextView = findViewById(R.id.detalhesDasTurmas_tvMensagem)
                        tvMensagem.setText("Não existem conteúdos cadastrados")

                    }


                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@ContDoProfActivity)
                    builder.setMessage("Exibir detalhes dos conteúdos desta turma?")
                        .setTitle("O que deseja fazer?")
                        .setCancelable(false)
                        .setPositiveButton("Sim, exibir", DialogInterface.OnClickListener { dialog, which ->

                            val detalhesDaTurma: ConstraintLayout = findViewById(R.id.detalhesDasTurmas)
                            val contDoProfIndex: ConstraintLayout = findViewById(R.id.contDoPrf_index)

                            contDoProfIndex.visibility = View.GONE
                            detalhesDaTurma.visibility = View.VISIBLE
                            //exibeConteudosDaquelaTurma()
                            exibeConteudosDaquelaTurma()

                        })
                    // Display a negative button on alert dialog
                    builder.setNegativeButton("Não"){dialog,which ->

                    }
                    val alert : AlertDialog = builder.create()
                    alert.show()


                //aqui lógica do botão de add conteúdo pra aproveitar o array local
                    val btnAddCont: Button = findViewById(R.id.detalhesDasTurmas_btnAddCont)
                    btnAddCont.setOnClickListener {

                     adicionandoConteudoMetodo("new")
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

    //aqui tem uma query que monta a recyclerview
    fun exibeConteudosDaquelaTurma(){

        val adicionandoConteudo: ConstraintLayout = findViewById(R.id.adicionandoConteudo)
        adicionandoConteudo.visibility = View.GONE
        val index: ConstraintLayout = findViewById(R.id.contDoPrf_index)
        index.visibility = View.GONE
        val infoTurma: ConstraintLayout = findViewById(R.id.detalhesDasTurmas)
        infoTurma.visibility = View.VISIBLE

        val arrayConteudos: MutableList<String> = ArrayList()
        val arrayBd: MutableList<String> = ArrayList()

        val rootRef = databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina)
        rootRef.orderByChild("disciplina").equalTo(disciplina)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            var x = querySnapshot.child("titulo").getValue().toString()
                            arrayConteudos.add(x)
                            arrayBd.add(querySnapshot.key.toString())

                        }

                    } else {
                        //showToast("Não existem conteúdos cadastrados na sua disciplina para esta turma.")
                        val tvMensagem: TextView = findViewById(R.id.detalhesDasTurmas_tvMensagem)
                        tvMensagem.setText("Não existem conteúdos cadastrados")

                    }

                    montaRecycleConteudosDaTurma(arrayConteudos, arrayBd)
                    encerraDialog()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })

    }

    fun montaRecycleConteudosDaTurma(arrayConteudos: MutableList<String> = ArrayList(), arrayBd: MutableList<String> = ArrayList()){

        ///montarRecyclerView
        //chame aqui pelo adaptador que criamos, com o nome dado e o construtor
        //o adaptador é proprio pra exibir conteúdo mas o layout vamos reaproveitar da turmas_itemRow que usamos no adaptador das turmas.
        val adapter = conteudosAdapter(this, arrayConteudos, arrayBd)

        //chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.detalhesDasTurmas_recyclerView)

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

                //vamos abrir a popup
                // Initialize a new layout inflater instance
                val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                // Inflate a custom view using layout inflater
                val view = inflater.inflate(R.layout.popup_tres_botoes,null)

                // Initialize a new instance of popup window
                val popupWindow = PopupWindow(
                    view, // Custom view to show in popup window
                    LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT // Window height
                )

                // Set an elevation for the popup window
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    popupWindow.elevation = 20.0F
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

                //lay_root é o layout parent que vou colocar a popup
                val lay_root: ConstraintLayout = findViewById(R.id.detalhesDasTurmas)

                // Finally, show the popup window on app
                TransitionManager.beginDelayedTransition(lay_root)
                popupWindow.showAtLocation(
                    lay_root, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
                )

                // Get the widgets reference from custom view
                val buttonPopup1 = view.findViewById<Button>(R.id.popupBtn)
                val buttonPopup2 = view.findViewById<Button>(R.id.popupBtn2)
                val buttonPopup3 = view.findViewById<Button>(R.id.popupBtn3)
                val buttonPopup4 = view.findViewById<Button>(R.id.popupBtn4)
                val buttonPopup5 = view.findViewById<Button>(R.id.popupBtn5)
                val txtTitulo = view.findViewById<TextView>(R.id.popupTitulo)
                val txtTexto = view.findViewById<TextView>(R.id.popupTexto)

                txtTitulo.setText(arrayConteudos.get(position))
                txtTexto.setText("O que deseja fazer com este conteúdo?")

                buttonPopup1.setText("Editar")
                buttonPopup2.setText("Copiar")
                buttonPopup3.setText("Excluir")
                buttonPopup4.setText("Fechar")
                buttonPopup5.setText("Adicionar exercícios")


                // Set a dismiss listener for popup window
                popupWindow.setOnDismissListener {

                }

                buttonPopup5.setOnClickListener {
                    abreCadQuest(arrayConteudos.get(position))
                    popupWindow.dismiss()
                }

                buttonPopup4.setOnClickListener {
                    popupWindow.dismiss()
                }

                buttonPopup3.setOnClickListener {

                    popupWindow.dismiss()
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@ContDoProfActivity)
                    builder.setMessage("Deseja mesmo excluir este conteúdo?")
                        .setTitle("ATENÇÃO")
                        .setCancelable(false)
                        .setPositiveButton("Sim, excluir", DialogInterface.OnClickListener { dialog, which ->

                            databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child(arrayBd.get(position)).removeValue()
                            finish()

                        })
                    // Display a negative button on alert dialog
                    builder.setNegativeButton("Cancelar"){dialog,which ->

                    }
                    val alert : AlertDialog = builder.create()
                    alert.show()

                }

                if (turmas.size>1){  //se for menor ou igual a 1 não tem turmas ou só tem a turma em questão. Logo não pdoe copiar


                    buttonPopup2.setOnClickListener {
                        popupWindow.dismiss()
                        val popupSelecionaTurma: ConstraintLayout = findViewById(R.id.popup_selecionaTurma)
                        popupSelecionaTurma.visibility = View.VISIBLE
                        popupWindow.dismiss()

                        val btnCancelar: Button = findViewById(R.id.popup_selecionaTurma_btnCancelar)
                        btnCancelar.setOnClickListener {
                            popupSelecionaTurma.visibility = View.GONE
                        }
                        //adicionar ao spinner
                        var list_of_items = turmas
                        var turmaEscolhida = "nao"
                        val spinnerTurmaParaCopiarCont: Spinner = findViewById(R.id.popup_selecionaTurma_spinner)
                        //Adapter for spinner
                        spinnerTurmaParaCopiarCont.adapter = ArrayAdapter(this@ContDoProfActivity, android.R.layout.simple_spinner_dropdown_item, list_of_items)

                        //item selected listener for spinner
                        spinnerTurmaParaCopiarCont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }

                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                positionSpinner: Int,
                                id: Long
                            ) {
                                turmaEscolhida = list_of_items[positionSpinner]
                                //val bdDaTurmaQueRecebaraDados = arrayBd.get(positionSpinner)
                                val btnCopiar: Button = findViewById(R.id.popup_selecionaTurma_btnCopiar)
                                btnCopiar.setOnClickListener {

                                    moveDataFirebase(arrayBd.get(position), turmaEscolhida)
                                    showToast("Copiano conteúdo, espere.")
                                    btnCancelar.performClick()

                                }

                            }
                        }


                    }
                }

                buttonPopup1.setOnClickListener {
                    popupWindow.dismiss()
                    val popup: ConstraintLayout = findViewById(R.id.popup_selecionaTurma)
                    popup.visibility = View.GONE
                    queryParaEdicao(arrayBd.get(position))
                }




            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))


    }

    //Este método é chamado depois de clicar em copiar conteúdo. Aqui ele copia os dados e cola na outra turma.
    private fun moveDataFirebase(bdOriginal: String, turmaEscolhida: String) {

        chamaDialog()
        val rootRef = databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child(bdOriginal)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
                encerraDialog()
            }

            override fun onDataChange(p0: DataSnapshot) {

                arrayAlinhamento.clear()
                arrayCor.clear()
                arrayDescItens.clear()
                arrayItens.clear()

                var values = p0.child("titulo").value.toString()
                val titulo = values

                values = p0.child("entradas").value.toString()

                val entradas = values
                var cont=0
                while (cont<entradas.toInt()){
                    values = p0.child("valores_entradas").child(cont.toString()).child("alinhamento").value.toString()
                    arrayAlinhamento.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("cor").value.toString()
                    arrayCor.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("descricao").value.toString()
                    arrayDescItens.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("item").value.toString()
                    arrayItens.add(values)
                    cont++
                }

                //copiamos tudo. Agora vamos gravar no novo BD
                databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("titulo").setValue(titulo)
                databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("entradas").setValue(entradas.toString())
                databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("disciplina").setValue(disciplina)
                cont =0
                while (cont<entradas.toInt()){
                    databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("valores_entradas").child(cont.toString()).child("alinhamento").setValue(arrayAlinhamento.get(cont))
                    databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("valores_entradas").child(cont.toString()).child("cor").setValue(arrayCor.get(cont))
                    databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("valores_entradas").child(cont.toString()).child("descricao").setValue(arrayDescItens.get(cont))
                    databaseReference.child("conteudos").child(escola+turmaEscolhida).child(disciplina).child(bdOriginal).child("valores_entradas").child(cont.toString()).child("item").setValue(arrayItens.get(cont))
                    cont++
                }

                showToast("O conteúdo foi copiado para a turma "+turmaEscolhida)
                encerraDialog()

            }

            //EncerraDialog()

        })

    }

    //quando clica em editar um conteudo e ele faz uma query para levantar os dados e chama o método adicionando ConteudoMetodo()
    fun queryParaEdicao(bd:String){

        chamaDialog()
        val rootRef = databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child(bd)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
                encerraDialog()
            }

            override fun onDataChange(p0: DataSnapshot) {
                //TODO("Not yet implemented")

                var values = p0.child("titulo").value.toString()
                val etTitulo: EditText = findViewById(R.id.adicionandoConteudo_etTitulo)
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
                    arrayDescItens.add(values)
                    values = p0.child("valores_entradas").child(cont.toString()).child("item").value.toString()
                    arrayItens.add(values)
                    cont++
                }

                encerraDialog()
                adicionandoConteudoMetodo(bd)

            }


            //EncerraDialog()

        })



    }

    //este método vai ser chamado quando clicar em add conteudo
    //aqui estão todos os clicks do btnpopup de adicionar
    fun adicionandoConteudoMetodo(bd: String){  //bd so vai ser usado na edição

        var bdnew = bd

        var posicao = 0

        val adicionandoConteudo: ConstraintLayout = findViewById(R.id.adicionandoConteudo)
        adicionandoConteudo.visibility = View.VISIBLE
        val infoTurma: ConstraintLayout = findViewById(R.id.detalhesDasTurmas)
        infoTurma.visibility = View.GONE

        val popup: ConstraintLayout = findViewById(R.id.popup_edit_newCont)

        val btnAbreFechaMenu: Button = findViewById(R.id.adicionandoConteudo_btnAbrefechaMenu)
        btnAbreFechaMenu.setOnClickListener {
            val menu: ConstraintLayout = findViewById(R.id.adicionandoConteudo_menu)
            if (menu.isVisible){
                //btnAbreFechaMenu.setText("abrir")
                btnAbreFechaMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_left_black_24dp, 0, 0);
                menu.visibility = View.GONE
            } else {
                btnAbreFechaMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_right_black_24dp, 0, 0);
                menu.visibility = View.VISIBLE
            }

        }

        val btnAddSubtitulo: Button = findViewById(R.id.adicionandoConteudo_menu_btnSubtitulo)
        val btnAddTexto: Button = findViewById(R.id.adicionandoConteudo_menu_btnTexto)
        val btnAddImage: Button = findViewById(R.id.adicionandoConteudo_menu_btnImagem)
        val btnAddLink: Button = findViewById(R.id.adicionandoConteudo_menu_btnVideo)

        ///montarRecyclerView
        //chame aqui pelo adaptador que criamos, com o nome dado e o construtor
        adapterSpecific = profMontaContAdapter(this, arrayItens, arrayDescItens, arrayAlinhamento, arrayCor)

        //chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.adicionandoConteudo_recyclerView)

        //define o tipo de layout (linerr, grid)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        //coloca o adapter na recycleview
        recyclerView.adapter = adapterSpecific

        recyclerView.layoutManager = linearLayoutManager

        // Notify the adapter for data change.
        adapterSpecific.notifyDataSetChanged()

        //constructor: context, nomedarecycleview, object:ClickListener
        recyclerView.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView!!, object: ClickListener{

            override fun onClick(view: View, position: Int) {
               // Log.d("teste", aNome.get(position))
                //Toast.makeText(this@MainActivity, !! aNome.get(position).toString(), Toast.LENGTH_SHORT).show()
                val btnApagar: Button = findViewById(R.id.popup_edit_newCont_btnApagar)
                btnApagar.visibility = View.VISIBLE
                val btnFechaPopup: Button = findViewById(R.id.popup_edit_newCont_btnFechar)

                btnApagar.setOnClickListener {
                    //arrayItens, arrayDescItens, arrayAlinhamento, arrayCor
                    if (arrayDescItens.get(posicao).equals("img")){
                        // Create a reference to the file to delete

                        mphotoStorageReference =mFireBaseStorage.getReference().child(turmaSelecionada).child(disciplina)
                        // Delete the file
                        mphotoStorageReference.delete().addOnSuccessListener {
                            // File deleted successfully
                        }.addOnFailureListener {
                            // Uh-oh, an error occurred!
                        }

                        val imageview: ImageView = findViewById(R.id.popup_edit_newCont_imageView)
                        imageview.setImageBitmap(null)
                        Glide.with(imageview.context).clear(imageview)

                    }

                    arrayItens.removeAt(posicao)
                    arrayAlinhamento.removeAt(posicao)
                    arrayCor.removeAt(posicao)
                    arrayDescItens.removeAt(posicao)
                    btnFechaPopup.performClick()
                    adapterSpecific.notifyItemRemoved(posicao)
                    adapterSpecific.notifyItemRangeChanged(posicao, arrayDescItens.size)
                    //adapterSpecific.notifyDataSetChanged()
                    recyclerView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }


                if (arrayDescItens.get(position).equals("subtitulo")){
                    btnAddSubtitulo.performClick()
                    val editText: EditText = findViewById(R.id.popup_edit_newCont_etSubtitulo)
                    editText.setText(arrayItens.get(position))
                    posicao = position
                    bdnew="edit"

                } else if (arrayDescItens.get(position).equals("texto")){
                    btnAddTexto.performClick()
                    val editText: EditText = findViewById(R.id.popup_edit_newCont_etTexto)
                    editText.setText(arrayItens.get(position))
                    posicao = position
                    bdnew="edit"
                } else if (arrayDescItens.get(position).equals("img")){
                    btnAddImage.performClick()
                    //val imageView : ImageView = findViewById(R.id.popup_edit_newCont_imageView)
                    //Glide.with(this@ContDoProfActivity).load(arrayItens.get(position)).into(imageView)
                    posicao =position

                } else {
                    btnAddLink.performClick()
                    val etLink: EditText = findViewById(R.id.popup_edit_newCont_etYoutube)
                    etLink.setText(arrayItens.get(position))
                    posicao=position
                    bdnew="edit"

                }
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))


        //agora os botões
        val btnFechaPopup: Button = findViewById(R.id.popup_edit_newCont_btnFechar)
        btnFechaPopup.setOnClickListener {
            popup.visibility = View.GONE
            val etSubtitulo: EditText = findViewById(R.id.popup_edit_newCont_etSubtitulo)
            val etTexto: EditText = findViewById(R.id.popup_edit_newCont_etTexto)
            val etlink: EditText = findViewById(R.id.popup_edit_newCont_etYoutube)
            val imageView: ImageView = findViewById(R.id.popup_edit_newCont_imageView)
            val btnUpload: Button = findViewById(R.id.popup_edit_newCont_btnUpload)
            val btnRotate: Button = findViewById(R.id.popup_edit_newCont_btnRotateFoto)
            val btnApagar: Button = findViewById(R.id.popup_edit_newCont_btnApagar)

            etSubtitulo.setText("")
            etSubtitulo.visibility = View.GONE
            etTexto.setText("")
            etTexto.visibility = View.GONE
            etlink.setText("")
            etlink.visibility = View.GONE
            Glide.with(this).clear(imageView)
            imageView.visibility = View.GONE
            btnUpload.visibility = View.GONE
            btnRotate.visibility = View.GONE
            btnApagar.visibility = View.GONE

        }


        btnAddSubtitulo.setOnClickListener {
            popup.visibility = View.VISIBLE
            val editText: EditText = findViewById(R.id.popup_edit_newCont_etSubtitulo)
            editText.visibility = View.VISIBLE

            var alinhamento: String = "left"
            var cor: String = "#000000"
            val desc = "subtitulo"

            val btnSalvar: Button = findViewById(R.id.popup_edit_newCont_btnSalvar)
            btnSalvar.setOnClickListener {
                //salvar aqui no array
                if (bdnew.equals("new")) {
                    if (editText.text.isEmpty()){
                        editText.setError("Escreva o subtítulo")
                        editText.requestFocus()
                    } else {
                        arrayItens.add(editText.text.toString())
                        arrayDescItens.add(desc)
                        arrayAlinhamento.add(alinhamento)
                        arrayCor.add(cor) //padrao
                        adapterSpecific.notifyDataSetChanged()
                        btnFechaPopup.performClick()
                        hideKeyboard()
                    }
                } else {
                    if (editText.text.isEmpty()){
                        arrayAlinhamento.removeAt(posicao.toInt())
                        arrayCor.removeAt(posicao.toInt())
                        arrayDescItens.removeAt(posicao.toInt())
                        arrayItens.removeAt(posicao.toInt())
                        adapterSpecific.notifyDataSetChanged()
                        btnFechaPopup.performClick()
                        hideKeyboard()
                    } else {
                        arrayAlinhamento.set(posicao.toInt(), alinhamento)  //add(posicao.toInt(), alinhamento)
                        arrayCor.set(posicao.toInt(), cor)   //add(posicao.toInt(), cor)
                        arrayDescItens.set(posicao.toInt(), desc)  //.add(posicao.toInt(), desc)
                        arrayItens.set(posicao.toInt(), editText.text.toString())   //add(posicao.toInt(), editText.text.toString())
                        adapterSpecific.notifyDataSetChanged()
                        btnFechaPopup.performClick()
                        hideKeyboard()
                    }
                }

                bdnew = bd //volta ao que era antes (quando está editando um item ele ganha o valor "edit" provisoriamente só para salvar a edicação enquanto o prof. faz o conteudo
            }

            val btnAlignLeft: Button = findViewById(R.id.popup_edit_newCont_btnAlignLeft)
            btnAlignLeft.setOnClickListener {
                ajustaAlinhamento("left", editText)
                alinhamento = "left"
            }

            val btnAlignCenter: Button = findViewById(R.id.popup_edit_newCont_btnAlignCenter)
            btnAlignCenter.setOnClickListener {
                ajustaAlinhamento("center", editText)
                alinhamento = "center"
            }

            val btnAlignRight: Button = findViewById(R.id.popup_edit_newCont_btnAlignRight)
            btnAlignRight.setOnClickListener {
                ajustaAlinhamento("right", editText)
                alinhamento = "right"
            }

            val btnPreto: Button = findViewById(R.id.popup_edit_newCont_btnPreto)
            val btnAzul: Button = findViewById(R.id.popup_edit_newCont_btnAzul)
            val btnVermelho: Button = findViewById(R.id.popup_edit_newCont_btnVermelho)
            val btnVerde: Button = findViewById(R.id.popup_edit_newCont_btnVerde)
            btnPreto.setOnClickListener {
                ajustaCor("#000000", editText)
                cor = "#000000"
            }
            btnAzul.setOnClickListener {
                ajustaCor("#2140EF", editText)
                cor = "2140EF"
            }
            btnVermelho.setOnClickListener {
                ajustaCor("#F44336", editText)
                cor = "#F44336"
            }
            btnVerde.setOnClickListener {
                ajustaCor("#4CAF50", editText)
                cor = "#4CAF50"
            }

        }


        btnAddTexto.setOnClickListener {
            popup.visibility = View.VISIBLE
            val editText: EditText = findViewById(R.id.popup_edit_newCont_etTexto)
            editText.visibility = View.VISIBLE

            var alinhamento: String = "left"
            var cor: String = "#000000"
            val desc = "texto"

            val btnSalvar: Button = findViewById(R.id.popup_edit_newCont_btnSalvar)
            btnSalvar.setOnClickListener {
                //salvar aqui no array
                if (bdnew.equals("new")) {
                    if (editText.text.isEmpty()){
                        editText.setError("Escreva o texto")
                        editText.requestFocus()
                    } else {
                        arrayItens.add(editText.text.toString())
                        arrayDescItens.add(desc)
                        arrayAlinhamento.add(alinhamento)
                        arrayCor.add(cor) //padrao
                        adapterSpecific.notifyDataSetChanged()
                        btnFechaPopup.performClick()
                        hideKeyboard()
                    }
                } else {
                    if (editText.text.isEmpty()){
                        arrayAlinhamento.removeAt(posicao.toInt())
                        arrayCor.removeAt(posicao.toInt())
                        arrayDescItens.removeAt(posicao.toInt())
                        arrayItens.removeAt(posicao.toInt())
                        adapterSpecific.notifyDataSetChanged()
                        btnFechaPopup.performClick()
                        hideKeyboard()
                    } else {
                        arrayAlinhamento.set(posicao.toInt(), alinhamento)  //add(posicao.toInt(), alinhamento)
                        arrayCor.set(posicao.toInt(), cor) //add(posicao.toInt(), cor)
                        arrayDescItens.set(posicao.toInt(), desc)   //add(posicao.toInt(), desc)
                        arrayItens.set(posicao.toInt(), editText.text.toString())   //add(posicao.toInt(), editText.text.toString())
                        adapterSpecific.notifyDataSetChanged()
                        btnFechaPopup.performClick()
                        hideKeyboard()
                    }
                }

                bdnew = bd //volta ao que era antes (quando está editando um item ele ganha o valor "edit" provisoriamente só para salvar a edicação enquanto o prof. faz o conteudo

            }

            val btnAlignLeft: Button = findViewById(R.id.popup_edit_newCont_btnAlignLeft)
            btnAlignLeft.setOnClickListener {
                ajustaAlinhamento("left", editText)
                alinhamento = "left"
            }

            val btnAlignCenter: Button = findViewById(R.id.popup_edit_newCont_btnAlignCenter)
            btnAlignCenter.setOnClickListener {
                ajustaAlinhamento("center", editText)
                alinhamento = "center"
            }

            val btnAlignRight: Button = findViewById(R.id.popup_edit_newCont_btnAlignRight)
            btnAlignRight.setOnClickListener {
                ajustaAlinhamento("right", editText)
                alinhamento = "right"
            }

            val btnPreto: Button = findViewById(R.id.popup_edit_newCont_btnPreto)
            val btnAzul: Button = findViewById(R.id.popup_edit_newCont_btnAzul)
            val btnVermelho: Button = findViewById(R.id.popup_edit_newCont_btnVermelho)
            val btnVerde: Button = findViewById(R.id.popup_edit_newCont_btnVerde)
            btnPreto.setOnClickListener {
                ajustaCor("#000000", editText)
                cor = "#000000"
            }
            btnAzul.setOnClickListener {
                ajustaCor("#2140EF", editText)
                cor = "2140EF"
            }
            btnVermelho.setOnClickListener {
                ajustaCor("#F44336", editText)
                cor = "#F44336"
            }
            btnVerde.setOnClickListener {
                ajustaCor("#4CAF50", editText)
                cor = "#4CAF50"
            }

        }


        btnAddImage.setOnClickListener {

            if (CheckPermissions()){


                bdnew=bd
                popup.visibility = View.VISIBLE
                val imageView : ImageView = findViewById(R.id.popup_edit_newCont_imageView)
                imageView.visibility = View.VISIBLE
                val btnUpload: Button = findViewById(R.id.popup_edit_newCont_btnUpload)
                btnUpload.visibility = View.VISIBLE

                urifinal="nao"

                val desc = "img"
                var alinhamento: String = "center"

                val btnSalvar: Button = findViewById(R.id.popup_edit_newCont_btnSalvar)
                btnSalvar.setOnClickListener {
                    //salvar aqui no array
                    if (bdnew.equals("new")) {
                        if (urifinal.equals("nao")){
                            showToast("Primeiro envie a imagem")
                        } else {
                            chamaDialog()
                            uploadImage()
                            //adapter.notifyDataSetChanged()
                            arrayAlinhamento.add("nao")
                            arrayCor.add("nao")
                            arrayDescItens.add(desc)
                            btnFechaPopup.performClick()
                            hideKeyboard()
                        }
                    } else {

                            hideKeyboard()
                            showToast("Desculpe, imagens não podem ser editadas")

                    }

                }

                btnUpload.setOnClickListener {

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@ContDoProfActivity)
                    builder.setMessage("Qual a origem da imagem?")
                        .setTitle("Selecione a forma de upload")
                        .setCancelable(false)
                        .setPositiveButton("Camera", DialogInterface.OnClickListener { dialog, which ->

                            takePictureFromCamera()

                        })
                    // Display a negative button on alert dialog
                    builder.setNegativeButton("Galeria"){dialog,which ->
                        takePictureFromGallery()
                    }
                    val alert : AlertDialog = builder.create()
                    alert.show()
                }

                val btnAlignLeft: Button = findViewById(R.id.popup_edit_newCont_btnAlignLeft)
                btnAlignLeft.setOnClickListener {
                    ajustaAlinhamentoImg("left", imageView, popup)
                    alinhamento = "left"
                }

                val btnAlignCenter: Button = findViewById(R.id.popup_edit_newCont_btnAlignCenter)
                btnAlignCenter.setOnClickListener {
                    ajustaAlinhamentoImg("center", imageView, popup)
                    alinhamento = "center"
                }

                val btnAlignRight: Button = findViewById(R.id.popup_edit_newCont_btnAlignRight)
                btnAlignRight.setOnClickListener {
                    ajustaAlinhamentoImg("right", imageView, popup)
                    alinhamento = "right"
                }


            }
        }


        btnAddLink.setOnClickListener {

                popup.visibility = View.VISIBLE
                val etLink: EditText = findViewById(R.id.popup_edit_newCont_etYoutube)
                etLink.visibility = View.VISIBLE

                val desc = "link"
                val cor: String = "#000000"
                val alinhamento = "center"


                val btnSalvar: Button = findViewById(R.id.popup_edit_newCont_btnSalvar)
                btnSalvar.setOnClickListener {
                    //salvar aqui no array
                    if (bdnew.equals("new")) {
                        if (etLink.text.isEmpty()) {
                            etLink.setError("Informe o link")
                            etLink.requestFocus()
                        } else {
                            arrayAlinhamento.add(alinhamento)
                            arrayCor.add(cor)
                            arrayDescItens.add(desc)
                            arrayItens.add(etLink.text.toString())
                            adapterSpecific.notifyDataSetChanged()
                            btnFechaPopup.performClick()
                            hideKeyboard()
                        }
                    } else {
                        if (etLink.text.isEmpty()){
                            arrayAlinhamento.removeAt(posicao.toInt())
                            arrayCor.removeAt(posicao.toInt())
                            arrayDescItens.removeAt(posicao.toInt())
                            arrayItens.removeAt(posicao.toInt())
                            adapterSpecific.notifyDataSetChanged()
                            btnFechaPopup.performClick()
                            hideKeyboard()
                        } else {
                            arrayAlinhamento.set(posicao.toInt(), alinhamento)  //add(posicao.toInt(), alinhamento)
                            arrayCor.set(posicao.toInt(), cor)    //add(posicao.toInt(), cor)
                            arrayDescItens.set(posicao.toInt(), desc)   //add(posicao.toInt(), desc)
                            arrayItens.set(posicao.toInt(), etLink.text.toString()) //add(posicao.toInt(), etLink.text.toString())
                            adapterSpecific.notifyDataSetChanged()
                            btnFechaPopup.performClick()
                            hideKeyboard()
                        }

                    }

                    bdnew = bd //volta ao que era antes (quando está editando um item ele ganha o valor "edit" provisoriamente só para salvar a edicação enquanto o prof. faz o conteudo

                }

        }

        val btnSalvarEsair: Button = findViewById(R.id.adicionandoConteudo_menu_btnSalvarESair)
        btnSalvarEsair.setOnClickListener {
            val etTitulo: EditText = findViewById(R.id.adicionandoConteudo_etTitulo)
            if (etTitulo.text.isEmpty()){
                etTitulo.requestFocus()
                etTitulo.setError("Informe o título deste conteúdo")
            } else {

                if (bdnew.equals("new")) { //se for uma nova entrada usar push
                    //vamos salvar no bd
                    val newCad: DatabaseReference =
                        databaseReference.child("conteudos").child(turmaSelecionada)
                            .child(disciplina).push()
                    newCad.child("titulo").setValue(etTitulo.text.toString())
                    newCad.child("entradas").setValue(arrayItens.size.toString())
                    var cont = 0
                    while (cont < arrayItens.size) {
                        newCad.child("valores_entradas").child(cont.toString()).child("item")
                            .setValue(arrayItens.get(cont))
                        newCad.child("valores_entradas").child(cont.toString()).child("descricao")
                            .setValue(arrayDescItens.get(cont))
                        newCad.child("valores_entradas").child(cont.toString()).child("alinhamento")
                            .setValue(arrayAlinhamento.get(cont))
                        newCad.child("valores_entradas").child(cont.toString()).child("cor")
                            .setValue(arrayCor.get(cont))
                        cont++
                    }
                    newCad.child("disciplina").setValue(disciplina)

                    //salvar os pontos do user
                    Log.d("teste", "chegou aqui")
                    colaboracao = colaboracao+10
                    databaseReference.child("professores").child(userBd)
                        .child("colaboracao").setValue(colaboracao).toString()
                    val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                    val editor = sharedPref.edit()
                    editor.putString("colaboracao", colaboracao.toString())
                    editor.apply()

                    showToast("Parabéns! Você ganhou 10 pontos de colaboração.")

                    arrayAlinhamento.clear()
                    arrayItens.clear()
                    arrayDescItens.clear()
                    arrayCor.clear()
                    exibeConteudosDaquelaTurma()
                } else { //senao atualiza no bd

                    databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child(bd).child("titulo").setValue(etTitulo.text.toString())
                    databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child(bd).child("entradas").setValue(arrayItens.size.toString())
                    var cont = 0
                    while (cont < arrayItens.size) {
                        databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child("valores_entradas").child(cont.toString()).child("item")
                            .setValue(arrayItens.get(cont))
                        databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child("valores_entradas").child(cont.toString()).child("descricao")
                            .setValue(arrayDescItens.get(cont))
                        databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child("valores_entradas").child(cont.toString()).child("alinhamento")
                            .setValue(arrayAlinhamento.get(cont))
                        databaseReference.child("conteudos").child(turmaSelecionada).child(disciplina).child("valores_entradas").child(cont.toString()).child("cor")
                            .setValue(arrayCor.get(cont))
                        cont++
                    }

                    showToast("Tudo salvo!")
                    arrayAlinhamento.clear()
                    arrayItens.clear()
                    arrayDescItens.clear()
                    arrayCor.clear()
                    exibeConteudosDaquelaTurma()

                }
            }
        }

    }

    fun ajustaAlinhamento(align: String, textView: EditText){

        if (align.equals("center")){
            textView.gravity = Gravity.CENTER_HORIZONTAL
        } else if (align.equals("left")){
            textView.gravity = Gravity.LEFT
        } else {
            textView.gravity = Gravity.RIGHT
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

    fun ajustaCor(cor: String, textView: EditText){

        if (cor.equals("#000000")){
            textView.setTextColor(Color.parseColor("#000000"))
        } else if (cor.equals("#2140EF")){
            textView.setTextColor(Color.parseColor("#2140EF"))
        }
        else if (cor.equals("#F44336")){
            textView.setTextColor(Color.parseColor("#F44336"))
        }
        else if (cor.equals("#4CAF50")){
            textView.setTextColor(Color.parseColor("#4CAF50"))
        }

    }















    //cadastro de questões
    fun abreCadQuest(conteudo: String) {

        val arraybdQuestao: MutableList<String> = ArrayList()
        val arrayPerguntaQuestao: MutableList<String> = ArrayList()

        val layAnterior: ConstraintLayout = findViewById(R.id.detalhesDasTurmas)
        val questoesIndex: ConstraintLayout = findViewById(R.id.questoes_index)

        layAnterior.visibility = View.GONE
        questoesIndex.visibility = View.VISIBLE

        val btnAddNewQuestion: Button = findViewById(R.id.questoes_index_btnAddQuestoes)
        btnAddNewQuestion.setOnClickListener {
            questoesIndex.visibility = View.GONE
            val addQuest: ConstraintLayout = findViewById(R.id.question_adicionando)
            addQuest.visibility = View.VISIBLE
            addNewQuest(conteudo)
        }


        chamaDialog()
        val rootRef = databaseReference.child("questões").child(disciplina)
        rootRef.orderByChild("conteudo").equalTo(conteudo)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            var x = querySnapshot.child("criador").getValue().toString()
                            if (x.equals(userBd)){

                                x = querySnapshot.child("pergunta").getValue().toString()
                                arrayPerguntaQuestao.add(x)
                                x = querySnapshot.key.toString()
                                arraybdQuestao.add(x)

                            }


                        }

                    } else {

                        //showToast("Não existem questões cadastradas por você para este conteúdo.")

                    }

                    //metodo aqui
                    //abrir a pergunta pra edição
                    encerraDialog()
                    //montar recyclerview
                    //chame aqui pelo adaptador que criamos, com o nome dado e o construtor
                    var adapter: questoes_Adapter = questoes_Adapter(this@ContDoProfActivity, arrayPerguntaQuestao, arraybdQuestao)

                    //chame a recyclerview
                    var recyclerView: RecyclerView = findViewById(R.id.questoes_index_recyclerView)

                        //define o tipo de layout (linerr, grid)
                    var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this@ContDoProfActivity)

                    //coloca o adapter na recycleview
                    recyclerView.adapter = adapter

                    recyclerView.layoutManager = linearLayoutManager

                        // Notify the adapter for data change.
                    adapter.notifyDataSetChanged()

                    //constructor: context, nomedarecycleview, object:ClickListener
                    recyclerView.addOnItemTouchListener(RecyclerTouchListener(this@ContDoProfActivity, recyclerView!!, object: ClickListener{

                        override fun onClick(view: View, position: Int) {

                            //abrir tela para edicao
                            editQuestao(conteudo, arraybdQuestao.get(position))

                        }

                        override fun onLongClick(view: View?, position: Int) {

                        }
                    }))





                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })


    }


    fun addNewQuest(conteudo: String){


        val textConteudo: TextView = findViewById(R.id.question_adicionando_txtConteudo)
        textConteudo.setText(conteudo)

        urifinal = "nao"

        val btnFechar: Button = findViewById(R.id.question_adicionando_btnFechar)
        btnFechar.setOnClickListener {
            finish()
        }

        val btnAddTextoExtra: Button = findViewById(R.id.question_adicionando_btnMaisTexto)
        val txtTextoExtra: EditText = findViewById(R.id.question_adicionando_txtMaisTexto)
        btnAddTextoExtra.setOnClickListener {
            if (txtTextoExtra.isVisible){
                txtTextoExtra.visibility = View.GONE
                btnAddTextoExtra.setText("Adicionar mais texto a pergunta")
            } else {
                txtTextoExtra.visibility = View.VISIBLE
                btnAddTextoExtra.setText("esconder")
            }

        }

        val btnUpload: Button = findViewById(R.id.question_adicionando_btnUpload)
        val imageView: ImageView = findViewById(R.id.question_adicionando_imageview)
        val btnAddImagem: Button = findViewById(R.id.question_adicionando_btnAddImg)
        btnAddImagem.setOnClickListener {

            if (imageView.isVisible){
                imageView.visibility = View.GONE
                btnUpload.visibility = View.GONE
                urifinal="nao"
                btnAddImagem.setText("Adicionar imagem")
            }else {
                imageView.visibility = View.VISIBLE
                btnUpload.visibility = View.VISIBLE
                btnAddImagem.setText("Remover imagem")
            }
        }


        btnUpload.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ContDoProfActivity)
            builder.setMessage("Qual a origem da imagem?")
                .setTitle("Selecione a forma de upload")
                .setCancelable(false)
                .setPositiveButton("Camera", DialogInterface.OnClickListener { dialog, which ->

                    takePictureFromCameraToQuestion()

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Galeria"){dialog,which ->
                takePictureFromGalleryToQuestion()
            }
            val alert : AlertDialog = builder.create()
            alert.show()

        }

        val radioA: RadioButton = findViewById(R.id.question_adicionando_lineA_radio)
        val radioB: RadioButton = findViewById(R.id.question_adicionando_lineB_radio)
        val radioC: RadioButton = findViewById(R.id.question_adicionando_lineC_radio)
        val radioD: RadioButton = findViewById(R.id.question_adicionando_lineD_radio)
        val radioE: RadioButton = findViewById(R.id.question_adicionando_lineE_radio)

        var opcaoCerta = "nao"
        radioA.setOnClickListener {
            radioB.isChecked = false
            radioC.isChecked = false
            radioD.isChecked = false
            radioE.isChecked = false
            opcaoCerta="a"
        }
        radioB.setOnClickListener {
            radioA.isChecked = false
            radioC.isChecked = false
            radioD.isChecked = false
            radioE.isChecked = false
            opcaoCerta="b"
        }
        radioC.setOnClickListener {
            radioA.isChecked = false
            radioB.isChecked = false
            radioD.isChecked = false
            radioE.isChecked = false
            opcaoCerta="c"
        }
        radioD.setOnClickListener {
            radioA.isChecked = false
            radioB.isChecked = false
            radioC.isChecked = false
            radioE.isChecked = false
            opcaoCerta="d"
        }
        radioE.setOnClickListener {
            radioA.isChecked = false
            radioB.isChecked = false
            radioC.isChecked = false
            radioD.isChecked = false
            opcaoCerta="e"
        }

        val opcaoA: EditText = findViewById(R.id.question_adicionando_lineA_txt)
        val opcaoB: EditText = findViewById(R.id.question_adicionando_lineB_txt)
        val opcaoC: EditText = findViewById(R.id.question_adicionando_lineC_txt)
        val opcaoD: EditText = findViewById(R.id.question_adicionando_lineD_txt)
        val opcaoE: EditText = findViewById(R.id.question_adicionando_lineE_txt)
        val pergunta: EditText = findViewById(R.id.question_adicionando_etPergunta)


        val btnSalvarePublicar: Button = findViewById(R.id.question_adicionando_btnSalvar)
        btnSalvarePublicar.setOnClickListener {

            if (opcaoCerta.equals("nao")){
                showToast("Marque a opção correta")
            } else if (opcaoA.text.isEmpty()){
                opcaoA.requestFocus()
                opcaoA.setError("informe valor")
            } else if (opcaoB.text.isEmpty()){
                opcaoB.requestFocus()
                opcaoB.setError("informe valor")
            } else if (opcaoC.text.isEmpty()){
                opcaoC.requestFocus()
                opcaoC.setError("informe valor")
            } else if (opcaoD.text.isEmpty()){
                opcaoD.requestFocus()
                opcaoD.setError("informe valor")
            } else if (opcaoE.text.isEmpty()){
                opcaoE.requestFocus()
                opcaoE.setError("informe valor")
            } else if (pergunta.text.isEmpty()){
                pergunta.requestFocus()
                pergunta.setError("Preenche a pergunta")
            } else {

                val newCad: DatabaseReference = databaseReference.child("questões").child(disciplina).push()
                newCad.child("criador").setValue(userBd)
                newCad.child("conteudo").setValue(conteudo)
                newCad.child("tentativas").setValue(0)
                newCad.child("rating").setValue(0)
                newCad.child("acertos").setValue(0)
                newCad.child("pergunta").setValue(pergunta.text.toString())
                newCad.child("opcaoA").setValue(opcaoA.text.toString())
                newCad.child("opcaoB").setValue(opcaoB.text.toString())
                newCad.child("opcaoC").setValue(opcaoC.text.toString())
                newCad.child("opcaoD").setValue(opcaoD.text.toString())
                newCad.child("opcaoE").setValue(opcaoE.text.toString())
                newCad.child("opcaoCorreta").setValue(opcaoCerta)
                if (urifinal.equals("nao")){
                    newCad.child("img").setValue("nao")
                } else {
                    newCad.child("img").setValue(urifinal)
                }
                if (txtTextoExtra.isVisible){
                    newCad.child("textoExtra").setValue(txtTextoExtra.text.toString())
                } else {newCad.child("textoExtra").setValue("nao")

                }
                newCad.child("escola").setValue(escola)

                //salva os pontos do user
                colaboracao = colaboracao + 5
                databaseReference.child("professores").child(userBd)
                    .child("colaboracao").setValue(colaboracao)
                val sharedPref: SharedPreferences = getSharedPreferences(
                    getString(R.string.sharedpreferences),
                    0
                ) //0 é private mode
                val editor = sharedPref.edit()
                editor.putString("colaboracao", colaboracao.toString())
                editor.apply()

                showToast("Parabéns! Você ganhou 5 pontos de colaboração.")

                showToast("A questão foi salva.")

                urifinal="nao"

                //limpar o imageview
                pergunta.setText("")
                opcaoA.setText("")
                opcaoB.setText("")
                opcaoC.setText("")
                opcaoD.setText("")
                opcaoE.setText("")
                imageView.visibility = View.GONE
                btnUpload.visibility = View.GONE
                btnAddTextoExtra.performClick()

            }


        }

    }

    fun editQuestao (conteudo: String, bdQuestao: String){


        val layAnterior: ConstraintLayout = findViewById(R.id.detalhesDasTurmas)
        val questoesIndex: ConstraintLayout = findViewById(R.id.questoes_index)

        layAnterior.visibility = View.GONE
        questoesIndex.visibility = View.GONE
        val addQuest: ConstraintLayout = findViewById(R.id.question_adicionando)
        addQuest.visibility = View.VISIBLE

        //a query está no final. Primeiro todos os loadings e carregamentos
        val textConteudo: TextView = findViewById(R.id.question_adicionando_txtConteudo)
        textConteudo.setText(conteudo)

        urifinal = "nao"

        val btnFechar: Button = findViewById(R.id.question_adicionando_btnFechar)
        btnFechar.setOnClickListener {
            finish()
        }

        val btnAddTextoExtra: Button = findViewById(R.id.question_adicionando_btnMaisTexto)
        val txtTextoExtra: EditText = findViewById(R.id.question_adicionando_txtMaisTexto)
        btnAddTextoExtra.setOnClickListener {
            if (txtTextoExtra.isVisible){
                txtTextoExtra.visibility = View.GONE
                btnAddTextoExtra.setText("Adicionar mais texto a pergunta")
            } else {
                txtTextoExtra.visibility = View.VISIBLE
                btnAddTextoExtra.setText("esconder")
            }

        }

        val btnUpload: Button = findViewById(R.id.question_adicionando_btnUpload)
        val imageView: ImageView = findViewById(R.id.question_adicionando_imageview)
        val btnAddImagem: Button = findViewById(R.id.question_adicionando_btnAddImg)
        btnAddImagem.setOnClickListener {

            if (imageView.isVisible){
                imageView.visibility = View.GONE
                btnUpload.visibility = View.GONE
                urifinal="nao"
                btnAddImagem.setText("Adicionar imagem")
            }else {
                imageView.visibility = View.VISIBLE
                btnUpload.visibility = View.VISIBLE
                btnAddImagem.setText("Remover imagem")
            }
        }


        btnUpload.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ContDoProfActivity)
            builder.setMessage("Qual a origem da imagem?")
                .setTitle("Selecione a forma de upload")
                .setCancelable(false)
                .setPositiveButton("Camera", DialogInterface.OnClickListener { dialog, which ->

                    takePictureFromCameraToQuestion()

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Galeria"){dialog,which ->
                takePictureFromGalleryToQuestion()
            }
            val alert : AlertDialog = builder.create()
            alert.show()

        }

        val radioA: RadioButton = findViewById(R.id.question_adicionando_lineA_radio)
        val radioB: RadioButton = findViewById(R.id.question_adicionando_lineB_radio)
        val radioC: RadioButton = findViewById(R.id.question_adicionando_lineC_radio)
        val radioD: RadioButton = findViewById(R.id.question_adicionando_lineD_radio)
        val radioE: RadioButton = findViewById(R.id.question_adicionando_lineE_radio)

        var opcaoCerta = "nao"
        radioA.setOnClickListener {
            radioB.isChecked = false
            radioC.isChecked = false
            radioD.isChecked = false
            radioE.isChecked = false
            opcaoCerta="a"
        }
        radioB.setOnClickListener {
            radioA.isChecked = false
            radioC.isChecked = false
            radioD.isChecked = false
            radioE.isChecked = false
            opcaoCerta="b"
        }
        radioC.setOnClickListener {
            radioA.isChecked = false
            radioB.isChecked = false
            radioD.isChecked = false
            radioE.isChecked = false
            opcaoCerta="c"
        }
        radioD.setOnClickListener {
            radioA.isChecked = false
            radioB.isChecked = false
            radioC.isChecked = false
            radioE.isChecked = false
            opcaoCerta="d"
        }
        radioE.setOnClickListener {
            radioA.isChecked = false
            radioB.isChecked = false
            radioC.isChecked = false
            radioD.isChecked = false
            opcaoCerta="e"
        }

        val opcaoA: EditText = findViewById(R.id.question_adicionando_lineA_txt)
        val opcaoB: EditText = findViewById(R.id.question_adicionando_lineB_txt)
        val opcaoC: EditText = findViewById(R.id.question_adicionando_lineC_txt)
        val opcaoD: EditText = findViewById(R.id.question_adicionando_lineD_txt)
        val opcaoE: EditText = findViewById(R.id.question_adicionando_lineE_txt)
        val pergunta: EditText = findViewById(R.id.question_adicionando_etPergunta)


        val btnSalvarePublicar: Button = findViewById(R.id.question_adicionando_btnSalvar)
        btnSalvarePublicar.setOnClickListener {

            if (opcaoCerta.equals("nao")){
                showToast("Marque a opção correta")
            } else if (opcaoA.text.isEmpty()){
                opcaoA.requestFocus()
                opcaoA.setError("informe valor")
            } else if (opcaoB.text.isEmpty()){
                opcaoB.requestFocus()
                opcaoB.setError("informe valor")
            } else if (opcaoC.text.isEmpty()){
                opcaoC.requestFocus()
                opcaoC.setError("informe valor")
            } else if (opcaoD.text.isEmpty()){
                opcaoD.requestFocus()
                opcaoD.setError("informe valor")
            } else if (opcaoE.text.isEmpty()){
                opcaoE.requestFocus()
                opcaoE.setError("informe valor")
            } else if (pergunta.text.isEmpty()){
                pergunta.requestFocus()
                pergunta.setError("Preenche a pergunta")
            } else {

                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("pergunta").setValue(pergunta.text.toString())
                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("opcaoA").setValue(opcaoA.text.toString())
                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("opcaoB").setValue(opcaoB.text.toString())
                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("opcaoC").setValue(opcaoC.text.toString())
                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("opcaoD").setValue(opcaoD.text.toString())
                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("opcaoE").setValue(opcaoE.text.toString())
                databaseReference.child("questões").child(disciplina).child(bdQuestao).child("opcaoCorreta").setValue(opcaoCerta)
                if (urifinal.equals("nao")){
                    databaseReference.child("questões").child(disciplina).child(bdQuestao).child("img").setValue("nao")
                } else {
                    databaseReference.child("questões").child(disciplina).child(bdQuestao).child("img").setValue(urifinal)
                }
                if (txtTextoExtra.isVisible){
                    databaseReference.child("questões").child(disciplina).child(bdQuestao).child("textoExtra").setValue(txtTextoExtra.text.toString())
                } else {databaseReference.child("questões").child(disciplina).child(bdQuestao).child("textoExtra").setValue("nao")

                }

                showToast("A questão foi atualizada.")

            }


        }




        chamaDialog()
        val rootRef = databaseReference.child("questões").child(disciplina).child(bdQuestao)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
                encerraDialog()
            }

            override fun onDataChange(p0: DataSnapshot) {

                var values = p0.child("img").value.toString()
                if (values.equals("nao")){
                    //do nothing
                } else {
                    btnAddImagem.performClick()
                    Glide.with(this@ContDoProfActivity).load(values).into(imageView)
                }

                values = p0.child("pergunta").value.toString()
                pergunta.setText(values)
                values = p0.child("textoExtra").value.toString()
                if (values.equals("nao")){
                    //do nothing
                } else {
                    btnAddTextoExtra.performClick()
                    txtTextoExtra.setText(values)
                }
                values = p0.child("opcaoA").value.toString()
                opcaoA.setText(values)
                values = p0.child("opcaoB").value.toString()
                opcaoB.setText(values)
                values = p0.child("opcaoC").value.toString()
                opcaoC.setText(values)
                values = p0.child("opcaoD").value.toString()
                opcaoD.setText(values)
                values = p0.child("opcaoE").value.toString()
                opcaoE.setText(values)

                values = p0.child("opcaoCorreta").value.toString()
                opcaoCerta = values
                if (opcaoCerta.equals("a")){
                    radioA.isChecked=true
                } else if (opcaoCerta.equals("b")){
                    radioB.isChecked=true
                } else if (opcaoCerta.equals("c")){
                    radioC.isChecked=true
                } else if (opcaoCerta.equals("d")){
                    radioD.isChecked=true
                } else {
                    radioE.isChecked=true
                }




                //values = p0.child("conteudo").value.toString()

                encerraDialog()
            }

            //EncerraDialog()

        })

    }


    //metodos de envio de foto
    fun takePictureFromCameraToQuestion() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 900)
        }
    }


    fun takePictureFromGalleryToQuestion() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Selecione a foto"), 901)

    }

    //aqui vamos reduzir o tamanho antes de enviar pro bd
    private fun compressImageToQuestion(image: Bitmap) {

        //agora sabemos as dimensões da imagem.
        //neste exemplo queremos que caiba em um banner de 100x400
        //é alterando o tamanho aqui que o tamanho total da imagem cresce ao final**************************************
//pode ser 100x100, depende do formato que você quer exibir
//400x100 fica com 2,5 kb, 800x200 fica com 5 kb
        val imageProvisoria: Bitmap = calculateInSizeSampleToFitImageView(image, 400, 400)

        //image provisoria pode ser colocada no imageview pois já é pequena suficiente.
        //val imageviewBanne:ImageView = findViewById(R.id.popup_edit_newCont_imageView)
        //imageviewBanne.setImageBitmap(imageProvisoria)

//esta parte é do método antigo. Imagino que ele nao tenha função mais
        val baos = ByteArrayOutputStream()
        var optionsCompress = 60  //taxa de compressao. 100 significa nenhuma compressao
        try {
            //Code here
            while (baos.toByteArray().size / 1024 > 150) {  //Loop if compressed picture is greater than 50kb, than to compression
                baos.reset() //Reset baos is empty baos
                imageProvisoria.compress(
                    Bitmap.CompressFormat.JPEG,
                    optionsCompress,
                    baos
                ) //The compression options%, storing the compressed data to the baos
                optionsCompress -= 25 //Every time reduced by 10
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


        //aqui faz upload pro storage database
        val tempUri: Uri = getImageUri(this, imageProvisoria)
        filePath = tempUri
        uploadImageToQuestion()


    }

    fun uploadImageToQuestion(){

        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference


        mphotoStorageReference =mFireBaseStorage.getReference().child(turmaSelecionada).child(disciplina).child(
            java.util.Calendar.getInstance().toString().trim())

        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
        val baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)

        //get the uri from the bitmap
        val tempUri: Uri = getImageUri(this, bmp)
        //transform the new compressed bmp in filepath uri
        filePath = tempUri

        //var file = Uri.fromFile(bitmap)
        var uploadTask = mphotoStorageReference.putFile(filePath)

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation mphotoStorageReference.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                urifinal = downloadUri.toString()
                val imageView: ImageView = findViewById(R.id.question_adicionando_imageview)
                Glide.with(this).load(urifinal).into(imageView)

                encerraDialog()


            } else {
                // Handle failures
                encerraDialog()
                Toast.makeText(this, "um erro ocorreu.", Toast.LENGTH_SHORT).show()
                // ...
            }
        }

    }








    //metodos de envio de foto
    fun takePictureFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 100)
        }
    }


    fun takePictureFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Selecione a foto"), 101)

    }

    //retorno da imagem
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //retorno da camera
        //primeiro if resultado da foto tirada pela camera
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

                chamaDialog()
                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                compressImage(photo)
                urifinal = "sim"

            }

        } else if (requestCode == 900){

            chamaDialog()
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            compressImageToQuestion(photo)
            urifinal = "sim"


        } else if (requestCode == 101){
            //resultado da foto pega na galeria
            if (resultCode == RESULT_OK
                && data != null && data.getData() != null
            ) {

                chamaDialog()
                filePath = data.getData()!!
                var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                compressImage(bitmap)
                urifinal = "sim"

            }
        } else if (requestCode == 901){

            chamaDialog()
            filePath = data!!.getData()!!
            var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            compressImageToQuestion(bitmap)
            urifinal = "sim"

        }
    }

    //aqui vamos reduzir o tamanho antes de enviar pro bd
    private fun compressImage(image: Bitmap) {

        //agora sabemos as dimensões da imagem.
        //neste exemplo queremos que caiba em um banner de 100x400
        //é alterando o tamanho aqui que o tamanho total da imagem cresce ao final**************************************
//pode ser 100x100, depende do formato que você quer exibir
//400x100 fica com 2,5 kb, 800x200 fica com 5 kb
        val imageProvisoria: Bitmap = calculateInSizeSampleToFitImageView(image, 400, 400)

        //image provisoria pode ser colocada no imageview pois já é pequena suficiente.
        //val imageviewBanne:ImageView = findViewById(R.id.popup_edit_newCont_imageView)
        //imageviewBanne.setImageBitmap(imageProvisoria)

//esta parte é do método antigo. Imagino que ele nao tenha função mais
        val baos = ByteArrayOutputStream()
        var optionsCompress = 60  //taxa de compressao. 100 significa nenhuma compressao
        try {
            //Code here
            while (baos.toByteArray().size / 1024 > 150) {  //Loop if compressed picture is greater than 50kb, than to compression
                baos.reset() //Reset baos is empty baos
                imageProvisoria.compress(
                    Bitmap.CompressFormat.JPEG,
                    optionsCompress,
                    baos
                ) //The compression options%, storing the compressed data to the baos
                optionsCompress -= 25 //Every time reduced by 10
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


        //aqui faz upload pro storage database
        val tempUri: Uri = getImageUri(this, imageProvisoria)
        filePath = tempUri
        //uploadImage()

        //aqui vamso deixar o user girar a foto
        val ivProvisorio: ImageView = findViewById(R.id.popup_edit_newCont_imageView)
        ivProvisorio.setImageBitmap(imageProvisoria) //imageProvisoria é o bitmap

        //vamos introduzir aqui o método de editar este bitmap

        encerraDialog()

        val btnRotate: Button = findViewById(R.id.popup_edit_newCont_btnRotateFoto)
        btnRotate.visibility = View.VISIBLE
        btnRotate.setOnClickListener {

            val newProvisoria: Bitmap? = RotateBitmap(imageProvisoria, 90F)
            //ivProvisorio.setImageBitmap(newProvisoria)
            val bit: Bitmap = newProvisoria!!
            compressImage(bit)
        }


    }

    //Adicione este método
    fun RotateBitmap(source: Bitmap?, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return source?.let {
            Bitmap.createBitmap(
                it,
                0,
                0,
                source.width,
                source.height,
                matrix,
                true
            )
        }
    }



    fun calculateInSizeSampleToFitImageView (image: Bitmap, imageViewWidth:Int, imageViewHeight:Int) : Bitmap{

        //ESTE BLOCO É PARA PEGAR AS DIMENSOES DA IMAGEM
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        //converte a imagem que o usuario escolheu para um Uri e depois para um File
        val file = bitmapToFile(image)
        val fpath = file.path
        BitmapFactory.decodeFile(fpath, options)
        //resultados pegos do método acima
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        //FIM DAS DIMENSOES DA IMAGEM

        var adaptedHeight: Int =0
        var adaptedWidh: Int =0
        //vamos primeiro acerta a altura. Poderiamos fazer tudo ao mesmo tempo, mas como estamos trabalhando com possibilidade do height ser diferente do width poderia dar erro
        if (imageHeight > imageViewHeight){

            adaptedHeight = imageHeight / 2
            while (adaptedHeight > imageViewHeight){
                adaptedHeight = adaptedHeight/2
            }

        } else {
            adaptedHeight = imageViewHeight
        }

        if (imageWidth > imageViewWidth){

            adaptedWidh = imageWidth / 2
            while (adaptedWidh > imageViewHeight){
                adaptedWidh = adaptedWidh/2
            }
        } else {
            adaptedWidh = imageViewWidth
        }

        val newBitmap = Bitmap.createScaledBitmap(image, adaptedWidh, adaptedHeight, false)
        return newBitmap

    }

    // Method to save an bitmap to a file
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    //pega o uri
    fun  getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 60, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "title", null)
        return Uri.parse(path)
    }

    //envio da foto
    //existe uma opção especial aqui para o caso de ser alvará
    fun uploadImage(){

        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference


        mphotoStorageReference =mFireBaseStorage.getReference().child(turmaSelecionada).child(disciplina).child(java.util.Calendar.getInstance().toString().trim())

        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
        val baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)

        //get the uri from the bitmap
        val tempUri: Uri = getImageUri(this, bmp)
        //transform the new compressed bmp in filepath uri
        filePath = tempUri

        //var file = Uri.fromFile(bitmap)
        var uploadTask = mphotoStorageReference.putFile(filePath)

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation mphotoStorageReference.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                urifinal = downloadUri.toString()
                //se quiser salvar, é o urifinal que é o link
                //pra salvar no bd e carregar com glide.
                arrayItens.add(urifinal)
                adapterSpecific.notifyDataSetChanged()
                encerraDialog()


            } else {
                // Handle failures
                encerraDialog()
                Toast.makeText(this, "um erro ocorreu.", Toast.LENGTH_SHORT).show()
                // ...
            }
        }

    }












    ///Permissões
    //caso o user ja tenha dado permissão antes, ele para aqui.
    private fun setupPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            //permissão concedida
        } else {
            RequestWriteStoragePermission()
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
        } else {
            RequestReadStoragePermission()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
        } else {
            RequestCameraPermission()
        }
    }

    //aqui sao tres métodos. Cada um para uma permissão
    fun RequestCameraPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para acessar a Camera. Vamos usar para você poder tirar fotos para enviar ao App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        1003)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para acessar a Camera. Vamos usar para você poder tirar fotos para enviar ao App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        1003)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        }
    }

    fun RequestReadStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para ler arquivos do seu celular. Vamos usar para você poder enviar as fotos para o App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1001)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para ler arquivos do seu celular. Vamos usar para você poder enviar as fotos para o App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1002)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        }
    }

    fun RequestWriteStoragePermission (){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para salvar arquivos no seu celular")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1002)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para salvar arquivos no seu celular")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1002)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        }
    }

    //por fim, pegue o retorno dos métodos aqui
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1002){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //permissao garantida
            } else {
                //permissao negada
            }
        }
        if (requestCode == 1001){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //permissao garantida
            } else {

            }
        }
        if (requestCode == 1003){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //permissão garantida
            } else {
                //permissao negada
            }
        }
    }

    private fun CheckPermissions() : Boolean {
        var permissao = 0  //é negado
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            //permissão concedida
            permissao=1
        } else {
            setupPermissions()
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            permissao=1
        } else {
            setupPermissions()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
            permissao=1
        } else {
            setupPermissions()
        }

        if (permissao==1){
            return true
        } else {
            return false
        }
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

    fun showToast(message: String){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    /* To hide Keyboard */
    fun hideKeyboard() {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
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
