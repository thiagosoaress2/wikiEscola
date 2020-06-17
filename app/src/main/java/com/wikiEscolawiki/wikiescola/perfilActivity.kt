package com.wikiEscolawiki.wikiescola

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.w3c.dom.Text
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class perfilActivity : AppCompatActivity() {

    var escola = "nao"
    var turmas: MutableList<String> = ArrayList()
    //var qntTurmas = 0
    var disciplina = "nao"
    var userBd = "nao"
    var tipo = "nao"
    var imagem = "nao"
    var nome = "nao"

    var colaboracao = 0

    private lateinit var databaseReference: DatabaseReference

    //variaveis do envio de foto
    private lateinit var filePath: Uri
    private var urifinal: String = "nao"
    private lateinit var mphotoStorageReference: StorageReference
    private lateinit var mFireBaseStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        databaseReference = FirebaseDatabase.getInstance().reference
        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference

        setupPermissions()

        escola = intent.getStringExtra("escola").toString()
        userBd = intent.getStringExtra("userBd").toString()
        tipo = intent.getStringExtra("tipo").toString()
        imagem = intent.getStringExtra("imagem").toString()
        nome = intent.getStringExtra("nome").toString()


        //turmas = intent.getStringArrayListExtra("turmas").toMutableList() // declare temp as ArrayList
        if (tipo.equals("professor")){
            disciplina = intent.getStringExtra("disciplina")
            colaboracao = intent.getIntExtra("colaboracao", 0).toInt()
            //pega as turmas

            val x = intent.getStringExtra("turmas")
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

            Log.d("teste", "o valor de turmas é "+turmas.get(0))
            Log.d("teste", "O tamanho de turmas é "+turmas.size)
            /*
            val x = intent.getStringExtra("turmas")
            qntTurmas = intent.getStringExtra("qntTurma").toInt()
            if (x.contains(";")){
                var cont=0
                val tokens = StringTokenizer(x, ";") //imagine a string with fruit; they
                while (cont<qntTurmas.toInt()){
                    var y = tokens.nextToken() // this will contain "Fruit"
                    turmas.add(y)
                    cont++
                }
            } else {
                turmas.add(x)
            }
             */
        }

        //disciplina vai ter que pegar do sharedprefs se for professor.
        if (tipo.equals("aluno")){
            //pegar info das turmas
            val paginaAluno: ConstraintLayout = findViewById(R.id.paginaAluno)
            paginaAluno.visibility = View.VISIBLE

            metodosAluno()
        } else {
            //pegar a disciplina
            val paginaAluno: ConstraintLayout = findViewById(R.id.paginaAluno)
            paginaAluno.visibility = View.VISIBLE

            val layturmas: ConstraintLayout = findViewById(R.id.layGrafico) //professor nao precisa ver isso
            layturmas.visibility = View.GONE

            metodosProfessor()
        }

    }

    override fun onStart() {
        super.onStart()

        val imageView : ImageView = findViewById(R.id.paginaAluno_imageView)

        if (imagem.equals("nao")){

            try {
                Glide.with(applicationContext)
                    .load(R.drawable.blankprofile) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(this@perfilActivity)) // applying the image transformer
                    .into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else {

            try {
                Glide.with(applicationContext)
                    .load(imagem) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(this@perfilActivity)) // applying the image transformer
                    .into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (tipo.equals("professor")){
            val txNome: TextView = findViewById(R.id.paginaAluno_txNome)
            val txEscola: TextView = findViewById(R.id.paginaAluno_txPontos)  //se for aluno vai mostrar a escola no lugar dos pontos. Neste momento aluno n tem pontos
            txNome.setText(nome)
            txEscola.setText("Pontos de colaboração: "+colaboracao)
        } else {
            val txNome: TextView = findViewById(R.id.paginaAluno_txNome)
            val txEscola: TextView = findViewById(R.id.paginaAluno_txPontos)  //se for aluno vai mostrar a escola no lugar dos pontos. Neste momento aluno n tem pontos
            txNome.setText(nome)
            txEscola.setText(escola)
            val txt: TextView = findViewById(R.id.textView31)
            txt.visibility = View.GONE //titulo da recyclerview
        }

    }

    fun metodosAluno(){

        val btnUpload: Button = findViewById(R.id.paginaAluno_btnFoto)
        btnUpload.setOnClickListener {

                val builder: AlertDialog.Builder = AlertDialog.Builder(this@perfilActivity)
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

        getTurmasComparationInfo()
    }

    fun getTurmasComparationInfo(){

        /*
        var turma1: String = "nao"
        var colTurma1 = 0
        var turma2: String = "nao"
        var colTurma2 = 0
        var turma3: String = "nao"
        var colTurma3 = 0


         */

        val arrayTurmas: MutableList<String> = ArrayList()
        val arrayPontos: MutableList<String> = ArrayList()

//        var cont=1

        val rootRef = databaseReference.child("turmas")
        rootRef.orderByChild("escola").equalTo(escola)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {


                            var value="nao"
                            value = querySnapshot.child("turma").getValue().toString()
                            arrayTurmas.add(value)
                            value= querySnapshot.child("colaboracao").getValue().toString()
                            arrayPontos.add(value)


                            /*
                            if (querySnapshot.child("colaboracao").exists()) {
                                if (cont==1){
                                    turma1 = querySnapshot.child("turma").getValue().toString()
                                    val x= querySnapshot.child("colaboracao").getValue().toString()
                                    colTurma1 = x.toInt()
                                } else if (cont==2){
                                    turma2 = querySnapshot.child("turma").getValue().toString()
                                    val x= querySnapshot.child("colaboracao").getValue().toString()
                                    colTurma2 = x.toInt()
                                } else {
                                    turma3 = querySnapshot.child("turma").getValue().toString()
                                    val x= querySnapshot.child("colaboracao").getValue().toString()
                                    colTurma3 = x.toInt()
                                }

                                cont++

                            } else {
                                if (cont==1){
                                    turma1 = querySnapshot.child("turma").getValue().toString()
                                    val key = querySnapshot.key.toString()
                                    databaseReference.child("turmas").child(key).child("colaboracao").setValue(0)
                                    colTurma1= 0
                                } else
                                if (cont==2){
                                    turma2 = querySnapshot.child("turma").getValue().toString()
                                    val key = querySnapshot.key.toString()
                                    databaseReference.child("turmas").child(key).child("colaboracao").setValue(0)
                                    colTurma2= 0
                                } else {
                                    turma3 = querySnapshot.child("turma").getValue().toString()
                                    val key = querySnapshot.key.toString()
                                    databaseReference.child("turmas").child(key).child("colaboracao").setValue(0)
                                    colTurma3= 0
                                }
                                cont++
                            }


                             */


                        }

                        //aqui filtrar os resultados
                        val arrayMaiores: MutableList<String> = ArrayList()

                        var index=0
                        var cont=0
                        var max =0
                        //quero pegar os 3 maiores valores.
                        //entao preciso descobrir se realmente existem tres ou mais
                        if (arrayPontos.size>=3){
                            max=3
                        } else if (arrayPontos.size==2){
                            max=2
                        } else if (arrayPontos.size==1){
                            max=1
                        } else {
                            max=0
                        }

                        while (cont<max){
                            index = getIndexOfLargest(arrayPontos) //descobre a posição do maior
                             //copia os maiores valores para este array provisorio
                            arrayMaiores.add(arrayPontos.get(index))
                            arrayMaiores.add(arrayTurmas.get(index))

                            arrayTurmas.removeAt(index)
                            arrayPontos.removeAt(index)

                            cont++
                        }

                        if (arrayMaiores.size==2){
                                montaGrafico(arrayMaiores.get(0).toInt(), arrayMaiores.get(1), 0, "nao", 0, "nao", "Turmas mais ativas no app")

                        } else if (arrayMaiores.size==4){
                            montaGrafico(arrayMaiores.get(0).toInt(), arrayMaiores.get(1), arrayMaiores.get(2).toInt(), arrayMaiores.get(3), 0, "nao", "Turmas mais ativas no app")
                        } else if (arrayMaiores.size==6) {
                            montaGrafico(arrayMaiores.get(0).toInt(), arrayMaiores.get(1), arrayMaiores.get(2).toInt(), arrayMaiores.get(3), arrayMaiores.get(4).toInt(), arrayMaiores.get(5), "Turmas mais ativas no app")
                        } else {
                            //n monta gráfico
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

    fun montaGrafico(val1: Int, label1:String, val2: Int, label2:String, val3: Int, label3: String, titulo: String){

        val bar1: ConstraintLayout = findViewById(R.id.customgraphBar1)
        val bar2: ConstraintLayout = findViewById(R.id.customgraphBar2)
        val bar3: ConstraintLayout = findViewById(R.id.customgraphBar3)

        val label1tv: TextView = findViewById(R.id.label1)
        val label2tv: TextView = findViewById(R.id.label2)
        val label3tv: TextView = findViewById(R.id.label3)

        findViewById<TextView>(R.id.customgraphTitle).setText(titulo)

        Log.d("teste", "os valoroes de labels "+label1+label2+label3)
        if (label1.equals("nao")){
            label1tv.visibility = View.GONE
        } else {
            label1tv.setText(label1)
        }
        if (label2.equals("nao")){
            label2tv.visibility = View.GONE
        } else {
            label2tv.setText(label2)
        }
        if (label3.equals("nao")){
            label3tv.visibility = View.GONE
        } else {
            label3tv.setText(label3)
        }

        //primeiro passo: Descobrir qual é o maior número.
        if (val1 >= val2 && val1 >= val3) {
            //val 1 é o maior. e agora que sabemos isso, vamos colocar ele com 100 dp.

            adjustBarSize(100, bar1) //AJUSTA BARRA 1

            var x: Long
            //calculo da segunda barra a partir da primeira
            if (val2==0){
                adjustBarSize(1, bar2)
            } else {
                x = (val1/val2).toLong() //aqui descobrimos a razão entre os valores
                x = 100/x  //aplicamos a razão nos tamanhos das barras
                adjustBarSize(x, bar2) //ajusta a barra
            }

            //calculo da terceira barra
            if (val3==0){
                adjustBarSize(1, bar3)
            } else {
                x = (val1/val3).toLong()
                x = 100/x
                adjustBarSize(x, bar3)
            }

            //valores 10 e 5
            //10/5 =2
            //tamanho da barra 100/2 = resultado é o tamanho que tem que ficar

        } else if (val2 >= val1 && val2 >= val3) {
            //val 2 é o maior numero
            adjustBarSize(100, bar2) //AJUSTA BARRA 2, que agora esta é a mais alta

            var x: Long

            if (val1==0){
                adjustBarSize(1, bar1)
            } else {
                x = (val2/val1).toLong() //aqui descobrimos a razão entre os valores
                x = 100/x  //aplicamos a razão nos tamanhos das barras
                adjustBarSize(x, bar1) //ajusta a barra
            }

            //calculo da terceira barra
            if (val3==0){
                adjustBarSize(1, bar3)
            } else {
                x = (val2/val1).toLong()
                x = 100/x
                adjustBarSize(x, bar3)
            }

        } else {
            //val 3 é o maiork numero
            adjustBarSize(100, bar3) //AJUSTA BARRA 2, que agora esta é a mais alta

            var x: Long

            if (val1==0){
                adjustBarSize(1, bar1)
            } else {
                x = (val3/val1).toLong() //aqui descobrimos a razão entre os valores
                x = 100/x  //aplicamos a razão nos tamanhos das barras
                adjustBarSize(x, bar1) //ajusta a barra
            }

            //calculo da terceira barra
            if (val2==0){
                adjustBarSize(1, bar2)
            } else {
                x = (val3/val2).toLong()
                x = 100/x
                adjustBarSize(x, bar2)
            }

        }

    }

    fun adjustBarSize (dpsSize: Long, barra: ConstraintLayout){

        if (dpsSize.toInt()==0){

            val lp = barra.getLayoutParams()
            lp.height = 0
            barra.setLayoutParams(lp)

        } else {

            //vamos descobrir quantos pixels tem 100dp neste telefone. Pra isso, a primeira barra está por padrão setada para 100dp.
            //val dps = 100
            val scale: Float = this.getResources().getDisplayMetrics().density
            val pixels = (dpsSize * scale + 0.5f)

            //agora passamos os parametros para a barra 1 ficar com 100dp.
            val lp = barra.getLayoutParams()
            lp.height = pixels.toInt()
            barra.setLayoutParams(lp)
        }

    }

    fun getIndexOfLargest(array: MutableList<String> = ArrayList()): Int {
        var largest = 0
        for (i in 1 until array.size) {
            if (array[i] > array[largest]) largest = i
        }
        return largest // position of the first largest found
    }









    fun metodosProfessor (){

        val btnUpload: Button = findViewById(R.id.paginaAluno_btnFoto)
        btnUpload.setOnClickListener {

            val builder: AlertDialog.Builder = AlertDialog.Builder(this@perfilActivity)
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

        montaRecyclerViewTurmas()

    }

    //ao clicar chama query com informações da turma
    fun montaRecyclerViewTurmas(){

        if (turmas.size==0){
            val txt: TextView = findViewById(R.id.textView31)
            txt.setText("Sem turmas para exibir")
        } else {

            var adapter: turmasAdapter = turmasAdapter(this, turmas)

//chame a recyclerview
            var recyclerView: RecyclerView = findViewById(R.id.perfil_recyclerView)

//define o tipo de layout (linerr, grid)
            var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

//coloca o adapter na recycleview
            recyclerView.adapter = adapter

            recyclerView.layoutManager = linearLayoutManager

// Notify the adapter for data change.
            adapter.notifyDataSetChanged()

            //constructor: context, nomedarecycleview, object:ClickListener
            recyclerView.addOnItemTouchListener(
                ContDoProfActivity.RecyclerTouchListener(
                    this,
                    recyclerView!!,
                    object : ContDoProfActivity.ClickListener {

                        override fun onClick(view: View, position: Int) {
                            //Toast.makeText(this@ContDoProfActivity, turmas.get(position).toString(), Toast.LENGTH_SHORT).show()
                            queryQuemSaoMeusAluninhos(turmas.get(position))
                            //Log.d("teste", "Entrou na recyclerview")
                        }

                        override fun onLongClick(view: View?, position: Int) {

                        }
                    })
            )
        }

    }

    fun queryQuemSaoMeusAluninhos(turma: String){

        chamaDialog()
        var arrayNomeAluno: MutableList<String> = ArrayList()
        var arrayDesemp: MutableList<String> = ArrayList()
        var arrayFoto: MutableList<String> = ArrayList()

        Log.d("teste", "o valir de escolaTurma é "+escola+turma)
        val rootRef = databaseReference.child("usuarios")
        rootRef.orderByChild("escolaTurma").equalTo(escola+turma)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {


                            var x = querySnapshot.child("nome").getValue().toString()
                            arrayNomeAluno.add(x)

                            x = querySnapshot.child("imagem").getValue().toString()
                            arrayFoto.add(x)

                            if (disciplina.equals("Português 1") || disciplina.equals("Português 2")){
                                x = querySnapshot.child("portugues").getValue().toString()
                                arrayDesemp.add(x)
                            } else if (disciplina.equals("Matemática 1") || disciplina.equals("Matemática 2")){
                                x = querySnapshot.child("matematica").getValue().toString()
                                arrayDesemp.add(x)
                            } else if (disciplina.equals("História") || disciplina.equals("Geografia")){
                                x = querySnapshot.child("histgeo").getValue().toString()
                                arrayDesemp.add(x)
                            } else if (disciplina.equals("Ciências") || disciplina.equals("Biologia") || disciplina.equals("Física") || disciplina.equals("Química")){
                                x = querySnapshot.child("ciencias").getValue().toString()
                                arrayDesemp.add(x)
                            } else {
                                x = querySnapshot.child("outras").toString()
                                arrayDesemp.add(x)
                            }

                        }
                        encerraDialog()
                        montaRecyclerViewMeusAluninhos(arrayNomeAluno, arrayDesemp, arrayFoto)

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })


    }


    fun montaRecyclerViewMeusAluninhos(arrayNomeAluno: MutableList<String>, arrayDesempenho: MutableList<String>, arrayImg: MutableList<String>){

        val layDetalheDaTurma: ConstraintLayout = findViewById(R.id.paginaTurmaDetalhe)
        val layPaginaPrimeira: ConstraintLayout = findViewById(R.id.paginaCorpo)
        layDetalheDaTurma.visibility = View.VISIBLE
        layPaginaPrimeira.visibility = View.INVISIBLE

        val btnVoltar: Button = findViewById(R.id.paginaTurmaDetalhe_btnVoltar)
        btnVoltar.setOnClickListener {
            arrayNomeAluno.clear()
            arrayDesempenho.clear()
            arrayImg.clear()
            layDetalheDaTurma.visibility = View.GONE
            layPaginaPrimeira.visibility = View.VISIBLE

        }

        //chame aqui pelo adaptador que criamos, com o nome dado e o construtor
        var adapter: meusAlunosAdapter = meusAlunosAdapter(this, arrayNomeAluno, arrayImg, arrayDesempenho)

//chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.paginaTurmaDetalhe_recyclerView)

//define o tipo de layout (linerr, grid)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

//coloca o adapter na recycleview
        recyclerView.adapter = adapter

        recyclerView.layoutManager = linearLayoutManager

// Notify the adapter for data change.
        adapter.notifyDataSetChanged()

        recyclerView.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView!!, object: ClickListener{

            override fun onClick(view: View, position: Int) {
                //Log.d("teste", aNome.get(position))
                //Toast.makeText(this@MainActivity, !! aNome.get(position).toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))


    }










    //envio da foto
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

                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                compressImage(photo)

            }

        } else {
            //resultado da foto pega na galeria
            if (resultCode == RESULT_OK
                && data != null && data.getData() != null
            ) {

                filePath = data.getData()!!
                var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                compressImage(bitmap)


            }
        }
    }

    //aqui vamos reduzir o tamanho antes de enviar pro bd
    private fun compressImage(image: Bitmap) {

        chamaDialog()
        //agora sabemos as dimensões da imagem.
        //neste exemplo queremos que caiba em um banner de 100x400
        //é alterando o tamanho aqui que o tamanho total da imagem cresce ao final**************************************
//pode ser 100x100, depende do formato que você quer exibir
//400x100 fica com 2,5 kb, 800x200 fica com 5 kb
        val imageProvisoria: Bitmap = calculateInSizeSampleToFitImageView(image, 500, 500)

        //image provisoria pode ser colocada no imageview pois já é pequena suficiente.
        val imageviewBanne: ImageView = findViewById(R.id.paginaAluno_imageView)
        //imageviewBanne.setImageBitmap(imageProvisoria)
        try {
            Glide.with(applicationContext)
                .load(imageProvisoria) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                .thumbnail(0.9f)
                .skipMemoryCache(true)
                .transform(CircleTransform(this@perfilActivity)) // applying the image transformer
                .into(imageviewBanne)
        } catch (e: Exception) {
            e.printStackTrace()
        }

//esta parte é do método antigo. Imagino que ele nao tenha função mais
        val baos = ByteArrayOutputStream()
        var optionsCompress = 20  //taxa de compressao. 100 significa nenhuma compressao
        try {
            //Code here
            while (baos.toByteArray().size / 1024 > 50) {  //Loop if compressed picture is greater than 50kb, than to compression
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
        uploadImage()
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
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
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
        inImage.compress(Bitmap.CompressFormat.PNG, 35, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }


    //envio da foto
    //existe uma opção especial aqui para o caso de ser alvará
    fun uploadImage(){

        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference


        mphotoStorageReference = mFireBaseStorage.getReference().child(tipo).child(userBd).child("perfil")

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
                    encerraDialog()
                }
            }
            return@Continuation mphotoStorageReference.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                urifinal = downloadUri.toString()
                //se quiser salvar, é o urifinal que é o link
                //pra salvar no bd e carregar com glide.
                databaseReference.child("usuarios").child(userBd).child("imagem").setValue(urifinal)
                val imageView : ImageView = findViewById(R.id.paginaAluno_imageView)
                try {
                    Glide.with(applicationContext)
                        .load(urifinal) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                        .thumbnail(0.9f)
                        .skipMemoryCache(true)
                        .transform(CircleTransform(this)) // applying the image transformer
                        .into(imageView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()
                editor.putString("imagem", urifinal)
                editor.apply()

                encerraDialog()


            } else {
                // Handle failures
                Toast.makeText(this, "um erro ocorreu.", Toast.LENGTH_SHORT).show()
                encerraDialog()
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


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
        } else {
            RequestReadStoragePermission()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
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


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            permissao=1
        } else {
            setupPermissions()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
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
