package com.wikiEscolawiki.wikiescola

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityPrincipal : AppCompatActivity() {

    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val appUpdatedListener: InstallStateUpdatedListener by lazy {
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(installState: InstallState) {
                when {
                    installState.installStatus() == InstallStatus.DOWNLOADED -> popupSnackbarForCompleteUpdate()
                    installState.installStatus() == InstallStatus.INSTALLED -> appUpdateManager.unregisterListener(this)
                    else -> showToast("Instalando atualização"+installState.installStatus()) //Timber.d("InstallStateUpdatedListener: state: %s", installState.installStatus()) //este toast pode sair.
                }
            }
        }
    }


    var userBd: String = "nao"
    var tipo: String = "nao"
    //val turmas: MutableList<String> = ArrayList()
    var turmas: String = "nao"
    var escola: String = "nao"
    var nome: String = "nao"

    var imagem: String = "nao"


    //exclusivo de professor
    //var qntTurma =0
    var disciplina: String = "nao"
    var disciplinasExtras: String = "nao"

    val meta = 25

    var colaboracao = 0

    val arrayDesempenhoEmCadaDisciplina: MutableList<String> = ArrayList()
    /*
    /*ordem deste array
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
     */

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        databaseReference = FirebaseDatabase.getInstance().reference

        //verifica se tem verso mais recente na loja
        checkForAppUpdate()


        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        //se chegou aqui foi pq já tem o userbd no sharedprefs. Mas pode ser que não tenha o restante dos daods. Então precisamos verificar e se precisar, pegar estes dados numa query.
        userBd = sharedPref.getString("userBd", "nao").toString() //este valor é certo. Se tá aqui, já sabemos.
        tipo = sharedPref.getString("tipo", "nao").toString()
        turmas = sharedPref.getString("turma", "nao").toString()
        escola = sharedPref.getString("escola", "nao").toString()
        imagem = sharedPref.getString("imagem", "nao").toString()

        val primeiroAcesso = sharedPref.getString("primeiroAcesso", "nao").toString()
        //serie de verificacoes iniciais
        if (userBd.equals("nao")){ finish()
        }
        else if (primeiroAcesso.equals("0")){ //é o primeiro acesso
            //turmas.clear()
            primeiroAcesso()
        }
        else if (tipo.equals("professor")){ //se for professor, sempre vai pegar as turmas. Isso n vai ficar no shared
            //turmas.clear()
            val paginaInicialDoAluno: ConstraintLayout = findViewById(R.id.principal_index)
            paginaInicialDoAluno.visibility = View.GONE
            queryTeacher()
        }

        else if (tipo.equals("nao")) {//se tipo for não, é pq perdeu no shared por algum motivo
            //turmas.clear() //turma recebeu algum valor acima pegando do shared. entao se for buscar dados apagar ele. Senao a posição 0 seria "nao
            queryGetUserInfos()
        } else if (tipo.equals("null")){
            //turmas.clear()
            queryGetUserInfos()
        } else if (!userBd.equals("nao")){
            //aqui deve ocorrer tudo do usuario normal (aluno)
            nome = sharedPref.getString("nome", "nao").toString()
            inicioMetodosAposQuery()
            verMetaHoje()
        }


        if (turmas.equals("nao")){
            Log.d("teste" , "userBd")
            queryGetUserInfos()
        }else if (escola.equals("nao")){
            queryGetUserInfos()
        }


        clicks()
        //loadUserCompetences()

        val btnLogout: Button = findViewById(R.id.principal_index_btnLogout)
        btnLogout.setOnClickListener {

            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ActivityPrincipal)
            builder.setMessage("Deseja sair desta conta? Você precisará de sua senha para entrar novamente")
                .setTitle("Desconectar")
                .setCancelable(false)
                .setPositiveButton("Sim, sair", DialogInterface.OnClickListener { dialog, which ->


                    val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                    val editor = sharedPref.edit()
                    //editor.putString("userBd", "nao")
                    editor.clear().apply()
                    finish()

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->

            }
            val alert : AlertDialog = builder.create()
            alert.show()

        }

    }

    override fun onStart() {
        super.onStart()

        val imageView : ImageView = findViewById(R.id.principal_index_ivPerfil)
        val imageViewProf: ImageView = findViewById(R.id.principal_index_professor_ivPerfil)

        if (imagem.equals("nao")){
            try {
                Glide.with(applicationContext)
                    .load(R.drawable.blankprofile) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(this)) // applying the image transformer
                    .into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }


            try {
                Glide.with(applicationContext)
                    .load(R.drawable.blankprofile) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(this)) // applying the image transformer
                    .into(imageViewProf)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {

            if (tipo.equals("aluno")) {
                try {
                    Glide.with(applicationContext)
                        .load(imagem) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                        .thumbnail(0.9f)
                        .skipMemoryCache(true)
                        .transform(CircleTransform(this)) // applying the image transformer
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
                        .transform(CircleTransform(this)) // applying the image transformer
                        .into(imageViewProf)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun queryGetUserInfos() {

        arrayDesempenhoEmCadaDisciplina.clear()
        chamaDialog()
        val rootRef = databaseReference.child("usuarios").child(userBd)
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

                Log.d("teste", "entrou na query neste testew")

                values = p0.child("tipo").value.toString()
                tipo = values
                editor.putString("tipo", tipo)

                values = p0.child("nome").value.toString()
                editor.putString("nome", values)
                nome = values

                values = p0.child("apelido").value.toString()
                editor.putString("apelido", values)

                values = p0.child("escola").value.toString()
                editor.putString("escola", values)
                escola = values

                values = p0.child("turma").value.toString()
                editor.putString("turma", values)
                //turmas.add(values)
                turmas = values

                imagem = p0.child("imagem").value.toString()
                val imagemPerfil: ImageView = findViewById(R.id.principal_index_ivPerfil)
                try {
                    Glide.with(applicationContext)
                        .load(imagem) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                        .thumbnail(0.9f)
                        .skipMemoryCache(true)
                        .transform(CircleTransform(this@ActivityPrincipal)) // applying the image transformer
                        .into(imagemPerfil)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                editor.putString("imagem", imagem)

                //desempenho
                values = p0.child("portugues").value.toString()
                editor.putString("portugues", values)
                Log.d("teste", "portugues é "+values)
                arrayDesempenhoEmCadaDisciplina.add(values)
                Log.d("teste", "portugues dentro do array e "+arrayDesempenhoEmCadaDisciplina.get(0))

                values = p0.child("matematica").value.toString()
                editor.putString("matematica", values)
                arrayDesempenhoEmCadaDisciplina.add(values)

                values = p0.child("histgeo").value.toString()
                editor.putString("histgeo", values)
                arrayDesempenhoEmCadaDisciplina.add(values)

                values = p0.child("ciencias").value.toString()
                editor.putString("ciencias", values)
                arrayDesempenhoEmCadaDisciplina.add(values)

                values = p0.child("outras").value.toString()
                editor.putString("outras", values)
                arrayDesempenhoEmCadaDisciplina.add(values)

                values = p0.child("primeiroAcesso").value.toString()
                if (values.equals("0")){
                    primeiroAcesso()
                } else {
                    editor.putString("primeiroAcesso", "1")
                    inicioMetodosAposQuery()
                }


                editor.apply()


                if (tipo.equals("professor")){
                    queryTeacher()
                }

                encerraDialog()

            }

            //EncerraDialog()

        })

    }

    fun inicioMetodosAposQuery(){

        loadUserCompetences()

        verificaRespostasDeHelps()

    }

    fun montaGrafico(){

        /*
    LÍNGUA PORTUGUESA I
    LÍNGUA PORTUGUESA II  - Lp

    MATEMÁTICA I
    MATEMÁTICA II  - mat

    HISTÓRIA
    GEOGRAFIA  - Hist/geo

    CIÊNCIAS
    BIOLOGIA
    FÍSICA
    QUÍMICA - ciencias

    LÍNGUA INGLESA - ing/art/ens.r/ed.fi
    ARTE
    ENSINO RELIGIOSO
    EDUCAÇÃO FÍSICA
     */

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val NoOfEmp = ArrayList<PieEntry>()


        Log.d("teste", "o tamanhjo do array chegando em montaGrafico é "+arrayDesempenhoEmCadaDisciplina.size )
        Log.d("teste", "array 0"+arrayDesempenhoEmCadaDisciplina.get(0))
        Log.d("teste", "array 0"+arrayDesempenhoEmCadaDisciplina.get(1))
        Log.d("teste", "array 0"+arrayDesempenhoEmCadaDisciplina.get(2))
        Log.d("teste", "array 0"+arrayDesempenhoEmCadaDisciplina.get(3))
        Log.d("teste", "array 0"+arrayDesempenhoEmCadaDisciplina.get(4))

        if (arrayDesempenhoEmCadaDisciplina.get(0).contains("null")){
            Log.d("teste", "contem null")
        }

        if (arrayDesempenhoEmCadaDisciplina.get(0).equals("0") && arrayDesempenhoEmCadaDisciplina.get(1).equals("0") && arrayDesempenhoEmCadaDisciplina.get(2).equals("0") && arrayDesempenhoEmCadaDisciplina.get(3).equals("0")&& arrayDesempenhoEmCadaDisciplina.get(4).equals("0")){
            NoOfEmp.add(PieEntry(1F, "Português"))
            NoOfEmp.add(PieEntry(1F, "Matemática"))
            NoOfEmp.add(PieEntry(1F, "His/Geo"))
            NoOfEmp.add(PieEntry(1F, "Ciências"))
            NoOfEmp.add(PieEntry(1F, "Ing/Art/Ens.R/Ed.Fi"))

        } else if (arrayDesempenhoEmCadaDisciplina.get(0).contains("null") || arrayDesempenhoEmCadaDisciplina.get(1).contains("null") || arrayDesempenhoEmCadaDisciplina.get(2).contains("null") || arrayDesempenhoEmCadaDisciplina.get(3).contains("null") || arrayDesempenhoEmCadaDisciplina.get(4).contains("null")){

            /*
            NoOfEmp.add(PieEntry(1F, "Português"))
            NoOfEmp.add(PieEntry(1F, "Matemática"))
            NoOfEmp.add(PieEntry(1F, "His/Geo"))
            NoOfEmp.add(PieEntry(1F, "Ciências"))
            NoOfEmp.add(PieEntry(1F, "Ing/Art/Ens.R/Ed.Fi"))
             */
            arrayDesempenhoEmCadaDisciplina.clear()
            Log.d("teste", "chamou query novamente aqui")
            queryGetUserInfos()

        }
        else {
            NoOfEmp.add(
                PieEntry(
                    (arrayDesempenhoEmCadaDisciplina.get(0) + "F").toFloat(),
                    "Português"
                )
            )
            NoOfEmp.add(
                PieEntry(
                    (arrayDesempenhoEmCadaDisciplina.get(1) + "F").toFloat(),
                    "Matemática"
                )
            )
            NoOfEmp.add(
                PieEntry(
                    (arrayDesempenhoEmCadaDisciplina.get(2) + "F").toFloat(),
                    "His/Geo"
                )
            )
            NoOfEmp.add(
                PieEntry(
                    (arrayDesempenhoEmCadaDisciplina.get(3) + "F").toFloat(),
                    "Ciências"
                )
            )
            NoOfEmp.add(
                PieEntry(
                    (arrayDesempenhoEmCadaDisciplina.get(4) + "F").toFloat(),
                    "Ing/Art/Ens.R/Ed.Fi"
                )
            )
        }

        val dataSet = PieDataSet(NoOfEmp, "")

        pieChart.getDescription().setEnabled(false);
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 5f
        dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val data = PieData(dataSet)
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animateXY(5000, 5000)


    }

    override fun onResume() {
        super.onResume()
        val tx: TextView = findViewById(R.id.principal_index_professor_txPontos)
        tx.setText("Pontos de colaboração: "+colaboracao)

        //app update
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }

                //Check if Immediate update is required
                try {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            262)
                    }
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }


        //menuBtn
        val btnMenu: Button = findViewById(R.id.btnMenu)
        btnMenu.setOnClickListener {
            val layMenu: ConstraintLayout = findViewById(R.id.lay_menu)
            if (layMenu.isVisible){
                layMenu.visibility = View.GONE
            } else {
                layMenu.visibility = View.VISIBLE
            }
        }

    }

    //metodos exclusivos do aluno
    fun clicks(){

        val btnSalaDeAula: Button = findViewById(R.id.principal_index_menu_btnSalaDeAula)
        btnSalaDeAula.setOnClickListener {

            Log.d("teste", "turmas em activity principal é "+turmas)
            val intent = Intent(this, SalaDeAulaActivity::class.java)
            intent.putExtra("userBd", userBd)
            intent.putExtra("turma", turmas)
            intent.putExtra("escola", escola)
            intent.putExtra("nome", nome)
            startActivity(intent)
            finish()
        }

        val btnPerfil: Button = findViewById(R.id.principal_index_menu_btnAbrePerfil)
        btnPerfil.setOnClickListener {

            val intent = Intent(this, perfilActivity::class.java)
            intent.putExtra("userBd", userBd)
            intent.putExtra("turma", turmas)
            intent.putExtra("escola", escola)
            intent.putExtra("nome", nome)
            intent.putExtra("tipo", tipo)
            intent.putExtra("imagem", imagem)
            startActivity(intent)
            finish()

        }

        val btnEnterTheGame: Button = findViewById(R.id.principal_index_btnEnterTheGame)
        btnEnterTheGame.setOnClickListener {

            showToast("Ainda não disponível.")

            val intent = Intent(this, game::class.java)
            intent.putExtra("userBd", userBd)
            intent.putExtra("turma", turmas)
            intent.putExtra("escola", escola)
            intent.putExtra("nome", nome)
            intent.putExtra("imagem", imagem)
            startActivity(intent)
            finish()

        }

        val laySalaDeAyla: ConstraintLayout = findViewById(R.id.layBtnSalaDeAula)
        laySalaDeAyla.setOnClickListener {
            btnSalaDeAula.performClick()
        }

        val layEnterTheGame: ConstraintLayout = findViewById(R.id.layGame)
        layEnterTheGame.setOnClickListener {
            btnEnterTheGame.performClick()
        }

        val layPerfil: ConstraintLayout = findViewById(R.id.layBtnPerfil)
        layPerfil.setOnClickListener {
            btnPerfil.performClick()
        }

        val imgPerfil: ImageView = findViewById(R.id.principal_index_ivPerfil)
        imgPerfil.setOnClickListener {
            btnPerfil.performClick()
        }
    }

    fun loadUserCompetences(){

        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        arrayDesempenhoEmCadaDisciplina.clear()
        arrayDesempenhoEmCadaDisciplina.add(sharedPref.getString("portugues", "nao").toString())
        if (arrayDesempenhoEmCadaDisciplina.get(0).equals("nao")){
            queryGetUserInfos()
        } else {
            arrayDesempenhoEmCadaDisciplina.add(sharedPref.getString("matematica", "nao").toString())
            arrayDesempenhoEmCadaDisciplina.add(sharedPref.getString("histgeo", "nao").toString())
            arrayDesempenhoEmCadaDisciplina.add(sharedPref.getString("ciencias", "nao").toString())
            arrayDesempenhoEmCadaDisciplina.add(sharedPref.getString("outras", "nao").toString())

            montaGrafico()
        }


    }

    fun verificaRespostasDeHelps(){

        val arrayHelping: MutableList<String> = ArrayList()

        Log.d("teste", "disciplina é "+disciplina)
        val rootRef = databaseReference.child("Helping")
        rootRef.orderByChild("aluno").equalTo(userBd).limitToFirst(1)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            var values = querySnapshot.child("resposta").getValue().toString()
                            arrayHelping.add(values)
                            values = querySnapshot.child("pergunta").getValue().toString()
                            arrayHelping.add(values)
                            values = querySnapshot.child("professorRespondeu").getValue().toString()
                            arrayHelping.add(values)
                            values = querySnapshot.child("conteudo").getValue().toString()
                            arrayHelping.add(values)
                            values = querySnapshot.key.toString()
                            arrayHelping.add(values)

                            /*
                            pos 0 - resposta
                            pos 1 - pergunta
                            pos 2 - professor bd
                            pos 3 - conteudo
                            pos 4 - bd da pergunta
                             */
                            val btnMensagemChegou: Button = findViewById(R.id.principal_index_btnChegouMensagemHelp)
                            btnMensagemChegou.visibility = View.VISIBLE
                            btnMensagemChegou.setOnClickListener {
                                abreRespostaMensagem(arrayHelping)
                            }

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

    fun abreRespostaMensagem(arrayHelping: MutableList<String> = ArrayList()){

        val paginaAjuda: ConstraintLayout = findViewById(R.id.principal_helpingPeople)
        val paginaIndex: ConstraintLayout = findViewById(R.id.principal_index)
        paginaAjuda.visibility = View.VISIBLE
        paginaIndex.visibility = View.GONE

        val btnFechar: Button = findViewById(R.id.principal_helpingPeople_btnFechar)
        btnFechar.setOnClickListener {
            paginaAjuda.visibility = View.GONE
            paginaIndex.visibility = View.VISIBLE

            databaseReference.child("Helping").child(arrayHelping.get(4)).removeValue()
            arrayHelping.clear()

            val btnMensagemChegou: Button = findViewById(R.id.principal_index_btnChegouMensagemHelp)
            btnMensagemChegou.visibility = View.GONE
            btnMensagemChegou.setOnClickListener { null }

        }


        /*
        pos 0 - resposta
        pos 1 - pergunta
        pos 2 - professor bd
        pos 3 - conteudo
        pos 4 - bd da pergunta
         */

        val txPergunta: TextView = findViewById(R.id.principal_helpingPeople_txDuvida)  //na verdade é o txNome, mas aqui vamos usar pra outra função pra aproveitar o layout
        txPergunta.setText(arrayHelping.get(1))

        val txConteudo: TextView = findViewById(R.id.principal_helpingPeople_txConteudo)
        txConteudo.setText(arrayHelping.get(3))

        val etResposta: EditText = findViewById(R.id.principal_helpingPeople_etResposta)
        etResposta.setText("Sua resposta\n\n"+arrayHelping.get(0))

        val btnFinalizar: Button = findViewById(R.id.principal_helpingPeople_btnResponder)
        btnFinalizar.setText("Fechar")
        btnFinalizar.setOnClickListener {
            btnFechar.performClick()
        }


    }





    //metodos do primeiro acesso
    fun primeiroAcesso (){

        val index: ConstraintLayout = findViewById(R.id.principal_index)
        val primeiroAcesso : ConstraintLayout = findViewById(R.id.principal_primeiroAcesso)
        val souAluno: ConstraintLayout = findViewById(R.id.principal_primeiroAcesso_souAluno)
        val souProfessor: ConstraintLayout = findViewById(R.id.principal_primeiroAcesso_souProfessor)

        index.visibility = View.GONE
        primeiroAcesso.visibility = View.VISIBLE

        val btnSouAluno: Button = findViewById(R.id.principal_primeiroAcesso_btnSouAluno)
        val btnSouProfessor: Button = findViewById(R.id.principal_primeiroAcesso_btnSouProfessor)
        val btnFinalizarAluno: Button =findViewById(R.id.principal_primeiroAcesso_souAluno_btnFinalizar)
        val btnFinalizarProf: Button = findViewById(R.id.principal_primeiroAcesso_souProfessor_btnFinalizar)
        val tvStatus: TextView= findViewById(R.id.principal_primeiroAcesso_souProfessor_tvStatus)

        val etNomeCompleto: EditText = findViewById(R.id.principal_priimeiroAcesso_etNomeCompleto)
        val etApelido: EditText = findViewById(R.id.principal_primeiroAcesso_etApelido)

        btnSouAluno.setOnClickListener {
            hideKeyboard()
            souProfessor.visibility = View.GONE
            souAluno.visibility = View.VISIBLE
            val etTurma: EditText = findViewById(R.id.principal_primeiroAcesso_souAluno_etCodigoTurma)
            val btnVerificaTurma: Button = findViewById(R.id.principal_primeiroAcesso_souAluno_btnBuscaTurma)
            btnVerificaTurma.setOnClickListener {
                if (etTurma.text.isEmpty()){
                    etTurma.setError("Informe a turma")
                    etTurma.requestFocus()
                } else {
                    verificaTurmaAluno(etTurma.text.toString(), etTurma, btnFinalizarAluno)
                }
            }

        }

        var disciplinaSelecionada = "Selecione disciplina"
        btnSouProfessor.setOnClickListener {
            hideKeyboard()
            souProfessor.visibility = View.VISIBLE
            souAluno.visibility = View.GONE
            chamaDialog()
            verificaProfessor(tvStatus, btnFinalizarProf)

            //monta spinner
            var list_of_items = arrayOf(
                "Selecione disciplina",
                "Português 1",
                "Português 2",
                "Matemática 1",
                "Matemática 2",
                "História",
                "Geografia",
                "Ciências",
                "Biologia",
                "Física",
                "Química",
                "Inglês",
                "Arte",
                "Ensino religioso",
                "Educação física"
            )

            val spinnerEstado: Spinner = findViewById(R.id.principal_primeiroAcesso_souProfessor_spinner)
            //Adapter for spinner
            spinnerEstado.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list_of_items)

//item selected listener for spinner
            spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    disciplinaSelecionada = list_of_items[position]

                }
            }


        }

        //vamos chamar o clicklistener mas o botão só vai estar disponivel se for professor. Isso é verificado em verificaProfessor dentro do click do btnSouProfessor
        btnFinalizarProf.setOnClickListener {

            if (etNomeCompleto.text.isEmpty()){
                etNomeCompleto.setError("Informe o nome")
                etNomeCompleto.requestFocus()
            } else if (etApelido.text.isEmpty()) {
                etApelido.setError("Informe como deseja ser chamado")
                etApelido.requestFocus()
            }else if (disciplinaSelecionada.equals("Selecione disciplina")){
                showToast("Selecione a disciplina que leciona")
            } else {


                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()

                databaseReference.child("professores").child(userBd).child("nome").setValue(etNomeCompleto.text.toString())
                editor.putString("nome", etNomeCompleto.text.toString())

                databaseReference.child("professores").child(userBd).child("apelido").setValue(etApelido.text.toString())
                editor.putString("apelido", etApelido.text.toString())


                databaseReference.child("professores").child(userBd).child("disciplina").setValue(disciplinaSelecionada)
                editor.putString("disciplina", disciplinaSelecionada)

                databaseReference.child("professores").child(userBd).child("colaboracao").setValue(0)
                editor.putString("colaboracao", "0")
                colaboracao = 0


                editor.apply()

                showToast("Informações salvas!")
                primeiroAcesso.visibility = View.GONE
                index.visibility = View.VISIBLE
                finish()
            }
        }

        btnFinalizarAluno.setOnClickListener {

            Log.d("teste", "turma quando clica no botao é "+turmas)
            Log.d("teste", "escola quando clica no botao é "+escola)
            if (etNomeCompleto.text.isEmpty()){
                etNomeCompleto.setError("Informe o nome")
                etNomeCompleto.requestFocus()
            } else if (etApelido.text.isEmpty()){
                etApelido.setError("Informe como deseja ser chamado")
                etApelido.requestFocus()
            } else {

                showToast("Informações salvas!")
                primeiroAcesso.visibility = View.GONE
                index.visibility = View.VISIBLE

                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()

                //carregar infos
                var values: String

                tipo = "aluno"
                editor.putString("tipo", tipo)

                databaseReference.child("usuarios").child(userBd).child("nome").setValue(etNomeCompleto.text.toString())
                editor.putString("nome", etNomeCompleto.text.toString())

                databaseReference.child("usuarios").child(userBd).child("apelido").setValue(etApelido.text.toString())
                editor.putString("apelido", etApelido.text.toString())

                databaseReference.child("usuarios").child(userBd).child("primeiroAcesso").setValue("1")
                editor.putString("primeiroAcesso", "1")

                databaseReference.child("usuarios").child(userBd).child("turma").setValue(turmas)
                editor.putString("turma", turmas)

                databaseReference.child("usuarios").child(userBd).child("escola").setValue(escola)
                editor.putString("escola", escola)

                //escolaTurma é usado na busca pelos alunos de uma turma especifica
                databaseReference.child("usuarios").child(userBd).child("escolaTurma").setValue(escola+turmas)

                ativarLembrete() //começa a contar a barrinha de meta

                //agora os desempenhos
                /*
 LÍNGUA PORTUGUESA I
 LÍNGUA PORTUGUESA II  - Lp

 MATEMÁTICA I
 MATEMÁTICA II  - mat

 HISTÓRIA
 GEOGRAFIA  - Hist/geo

 CIÊNCIAS
 BIOLOGIA
 FÍSICA
 QUÍMICA - ciencias

 LÍNGUA INGLESA - ing/art/ens.r/ed.fi - outras
 ARTE
 ENSINO RELIGIOSO
 EDUCAÇÃO FÍSICA
  */

                databaseReference.child("usuarios").child(userBd).child("portugues").setValue(0)
                editor.putString("portugues", "0")
                databaseReference.child("usuarios").child(userBd).child("matematica").setValue(0)
                editor.putString("matematica", "0")
                databaseReference.child("usuarios").child(userBd).child("histgeo").setValue(0)
                editor.putString("histgeo", "0")
                databaseReference.child("usuarios").child(userBd).child("ciencias").setValue(0)
                editor.putString("ciencias", "0")
                databaseReference.child("usuarios").child(userBd).child("outras").setValue(0)
                editor.putString("outras", "0")  //

                editor.apply()
                finish()
            }

        }
    }

    fun verificaProfessor (tvStatusProf: TextView, btnFinalizarProf: Button){

        val rootRef = databaseReference.child("professores")
        rootRef.orderByChild("cel").equalTo(userBd).limitToFirst(1)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {
                            val tvStatusProf: TextView =
                                findViewById(R.id.principal_primeiroAcesso_souProfessor_tvStatus)
                            tvStatusProf.setText("Achamos você!")

                            val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                            val editor = sharedPref.edit()

                            //carregar infos
                            var values: String

                            databaseReference.child("usuarios").child(userBd)
                                .child("tipo").setValue("professor")
                            tipo = "professor"
                            editor.putString("tipo", tipo)

                            databaseReference.child("usuarios").child(userBd)
                                .child("primeiroAcesso").setValue("1")
                            editor.putString("primeiroAcesso", "1")

                            btnFinalizarProf.isEnabled = true

                            rootRef.removeEventListener(this)
                            editor.apply()
                        }

                    } else {
                            tvStatusProf.setText("Número não cadastradado como professor. Procure os responsáveis pelo app na escola.")
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

    fun verificaTurmaAluno(codigo: String, etTurma: EditText, btnFinalizar: Button){

        chamaDialog()
        val rootRef = databaseReference.child("turmas")
        rootRef.orderByChild("codigo").equalTo(codigo).limitToFirst(1)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            showToast("Turma encontrada! Pode finalizar o cadastro")
                            val turma = querySnapshot.child("turma").getValue().toString()
                            val escolaAqui = querySnapshot.child("escola").getValue().toString()
                            //turmas.clear()
                            //turmas.add(codigo)
                            turmas = turma
                            //turmas.add(turma)
                            escola = escolaAqui
                            //turmas.add(escola)//vamos aproveitar o array
                            btnFinalizar.isEnabled=true
                            rootRef.removeEventListener(this)

                        }

                    } else {

                        //Se enrou aqui e pq n achou nada
                        encerraDialog()
                        showToast("Esta turma não existe. Verifique o código")
                        etTurma.requestFocus()
                        etTurma.setError("!")
                        btnFinalizar.isEnabled=false


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
    //Fim dos métodos do primeiro acesso














    //codigos da barra de meta
    fun ativarLembrete(){

        val dataRemember = GetfutureDate(7)

        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        editor.putString("rememberDate", dataRemember)
        editor.putString("atividadesDaMeta", "0")
        editor.apply()

    }

    //use este métodod e apoio para calcular os dias no futuro
    private fun GetfutureDate (daysToAdd: Int) : String {

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())

        val c = Calendar.getInstance()
        c.time = sdf.parse(currentDate)
        c.add(Calendar.DATE, daysToAdd) // number of days to add

        var tomorrow: String = sdf.format(Date())
        tomorrow = sdf.format(c.time) // dt is now the new date

        return tomorrow

    }

    fun verMetaHoje(){

        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()


        var x = sharedPref.getString("atividadesDaMeta", "nao")
        var dateToRemember = sharedPref.getString("rememberDate", "nao")

        if (!dateToRemember.equals("nao")) {


            //criar um listenerna barra para informar o que é
            val txtmensagem: TextView = findViewById(R.id.principal_index_ondeParou_txtMensagem)
            txtmensagem.setText("Você fez "+x+" exercícios da sua meta de "+meta+" semanal. Encerra em "+dateToRemember)

            Log.d("teste", "entrou no if")
            var exerciciosFeitos: Int

            if (x.equals("nao")) {
                exerciciosFeitos=0
            } else {
                exerciciosFeitos = x!!.toInt()
            }

            val percent = ((exerciciosFeitos.toDouble()/meta)*100).toInt()
            val progressBar: ProgressBar = findViewById(R.id.meta_progressBar)
            progressBar.setProgress(percent.toInt())


            if (percent >= 100) {

                openPopUp("Parabéns!", "Você já atingiu a meta dessa semana!")
            } else  //agora vamos fazer mensagem de incentivo
            if (percent >= 80) {
                openPopUp(
                    "Estamos quase lá!",
                    "Você está quase alcançando a meta. Faltam poucos exercícios!"
                )
            }

            if (!dateToRemember.equals("nao")) { //se nao tiver é pq nao tem lembrete. O user nao comprou ração.
                //tem lembrete. Mas vamos ver se é a data certa pra mostrar.
                val dataHoje = GetDate()
                //ja temos a data de hoje e a data armazenada para lembrar.
                //agora vamos transformar ela em um objeto Date para podermos comparar
                val format = SimpleDateFormat("dd/MM/yyyy")
                val date1 = format.parse(dateToRemember)
                val date2 = format.parse(dataHoje)

                if (date1.compareTo(date2) < 0) {  //se for hoje ou no futuro

                    Log.d("teste", "entrou na data")
                    if (percent == 100) {
                        ativarLembrete() //se foi 100% nao precisa mandr mensagem pois ja foi feito no inicio. Mas reinicia o processo
                    } else {
                        openPopUp(
                            "Mais uma semana acabou.",
                            "Infelizmente você não concluiu sua meta de exercícios semanais. Vamos tentar fazer melhor esta semana?"
                        )
                        ativarLembrete() //mas reinicia o processo
                    }


                }

            }

        } else {
            Log.d("teste", "entrou no else e foi pra ativarLembrete ")
            ativarLembrete()
        }
    }

    //pega  a data
    private fun GetDate () : String {

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())

        return currentDate
    }

    fun openPopUp (titulo: String, texto:String) {
        //exibeBtnOpcoes - se for não, vai exibir apenas o botão com OK, sem opção. Senão, exibe dois botões e pega os textos deles de btnSim e btnNao


        // Get the widgets reference from custom view
        val buttonPopupOk = findViewById<Button>(R.id.popupBtnOk2)
        val txtTitulo = findViewById<TextView>(R.id.popupTitulo2)
        val txtTexto = findViewById<TextView>(R.id.popupTexto2)
        val popup: ConstraintLayout = findViewById(R.id.popup)
        popup.visibility = View.VISIBLE

            //vai esconder os botões com textos e exibir o btn ok
            buttonPopupOk.visibility = View.VISIBLE

            buttonPopupOk.setOnClickListener{
                // Dismiss the popup window
                popup.visibility = View.GONE
            }

        txtTitulo.text = titulo
        txtTexto.text = texto

    }








//METODOS DO PROFESSOR
    fun queryTeacher (){

        Log.d("teste", "entrou em queryTeacher")
        val rootRef = databaseReference.child("professores")
        rootRef.orderByChild("cel").equalTo(userBd).limitToFirst(1)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            escola = querySnapshot.child("escola").getValue().toString()

                            var provi = "0"
                            if (querySnapshot.child("colaboracao").exists()){
                                provi = querySnapshot.child("colaboracao").getValue().toString()
                            } else {
                                databaseReference.child("professores").child(userBd).child("colaboracao").setValue(0)
                            }

                            val txPontos: TextView = findViewById(R.id.principal_index_professor_txPontos)
                            colaboracao = provi.toInt()
                            txPontos.setText("Pontos de colaboração: "+colaboracao)

                            disciplina = querySnapshot.child("disciplina").getValue().toString()

                            if (disciplina.contains(";")){

                                val tokens = StringTokenizer(disciplina, ";") //”*” este é delim
                                val size = tokens.countTokens()
                                var cont=0
                                while (cont<size){
                                    val value = tokens.nextToken()
                                    if (cont==0){
                                        disciplina = value //essa vai ser a disciplian ativa, as demais ficam armazenadas pro user trocar
                                        //disciplinasExtras=value
                                    } else if (cont==1){
                                        disciplinasExtras=value //0 foi pra disciplina. Agora é o primeiro que vai pra disciplina extras
                                    } else {
                                        disciplinasExtras= disciplinasExtras+";"+value
                                    }
                                    cont++
                                }

                                trocarDisciplina(disciplinasExtras)

                            }



                            var x = "nao"
                            if (querySnapshot.child("turmas").exists()) {
                                turmas = querySnapshot.child("turmas").getValue().toString()

                            } //nao precisa else pq ai disciplians fica como "nao'


                            val btnPerfil: Button = findViewById(R.id.principal_index_professor_btnPerfil)
                            btnPerfil.visibility = View.VISIBLE

                            metodosProfessor()

                        }

                    } else {
                        showToast("Ocorreu um erro. Reiniciando aplicação")
                        val btnLogout: Button = findViewById(R.id.principal_index_btnLogout)
                        btnLogout.performClick()
                    }


                    encerraDialog()
                    populaRecycleHelp()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })

    }

    fun abreCadTurmasProf(){

        val indexProf: ConstraintLayout = findViewById(R.id.principal_index_professor)
        val index: ConstraintLayout = findViewById(R.id.principal_index)
        val cadTurmas: ConstraintLayout = findViewById(R.id.principal_findTurmasProf)
        val principalProfessor: ConstraintLayout = findViewById(R.id.principal_index_professor)
        principalProfessor.visibility = View.GONE
        index.visibility = View.GONE
        cadTurmas.visibility = View.VISIBLE
        indexProf.visibility = View.GONE

        val turmasDaEscola: MutableList<String> = ArrayList()
        val SerieDaTurma: MutableList<String> = ArrayList()

        queryTurmasDaEscola(turmasDaEscola, SerieDaTurma)
    }

    fun queryTurmasDaEscola(turmasDaEscola: MutableList<String>, SerieDaTurma: MutableList<String>){

        chamaDialog()
        val rootRef = databaseReference.child("turmas")
        rootRef.orderByChild("escola").equalTo(escola)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            var x = querySnapshot.child("turma").getValue().toString()
                            turmasDaEscola.add(x)
                            Log.d("teste", "o tamanho de turmasDaEscola é "+turmasDaEscola.size)
                            x = querySnapshot.child("serie").getValue().toString()
                            SerieDaTurma.add(x)

                        }

                    } else {
                        showToast("Não existem turmas cadastradas para sua escola.")

                    }

                    val tvTurmas: TextView = findViewById(R.id.principal_findTurmasProf_tvTurmasSelecionadas)
                    //var nTurmas = 0
                    var cont=0
                    //vamos colocar turmas que já existam dentro do textview

                    if (turmas.equals("nao") || turmas.equals("null")){
                        tvTurmas.setText("")
                    } else {
                        tvTurmas.setText(turmas)
                    }


                    /*
                    if (turmas.size!=0){
                        if (turmas.get(0).contains(";")){
                            tvTurmas.setText(turmas.get(0))
                        } else {
                            tvTurmas.setText(turmas.get(0)+";")
                        }

                        if (tvTurmas.text.equals(";")){ //estava dando este erro
                            tvTurmas.setText("")
                        }

                        cont++
                    }

                     */

                    //agora vamos exibir as turmas da escola num spinner

                    cont=0
                    var txt:String = "nao"
                    while (cont<turmasDaEscola.size){
                        if (cont==0){
                            txt = turmasDaEscola.get(cont).toString()
                        } else {
                            txt = txt+","+turmasDaEscola.get(cont)
                        }
                        cont++
                    }

                    var list_of_items = turmasDaEscola
                    var turmaSelecionada = "nao"
                    val spinnerEstado: Spinner = findViewById(R.id.principal_findTurmasProf_spinner)
                    //Adapter for spinner
                    spinnerEstado.adapter = ArrayAdapter(this@ActivityPrincipal, android.R.layout.simple_spinner_dropdown_item, list_of_items)

                    //item selected listener for spinner
                    spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            turmaSelecionada = list_of_items[position]
                            Log.d("teste", "turmaSelecionada é "+turmaSelecionada)

                        }
                    }

                    val btnAdd: Button = findViewById(R.id.principal_findTurmasProf_btnAdd)
                    btnAdd.setOnClickListener {
                        val turmas = tvTurmas.text.toString()
                        if (turmas.contains(turmaSelecionada)){
                            showToast("Você já adicionou esta turma")
                        } else {
                            tvTurmas.setText(turmas+turmaSelecionada+";")
                        }
                    }

                    val btnClear: Button = findViewById(R.id.principal_findTurmasProf_btnClear)
                    btnClear.setOnClickListener {
                        tvTurmas.setText("")
                    }

                    val btnSalvar: Button = findViewById(R.id.principal_findTurmasProf_btnFinalizar)
                    btnSalvar.setOnClickListener {
                        showToast("Informações salvas!")
                        val cadTurmas: ConstraintLayout = findViewById(R.id.principal_findTurmasProf)
                        val principalProfessor: ConstraintLayout = findViewById(R.id.principal_index_professor)
                        principalProfessor.visibility = View.VISIBLE
                        cadTurmas.visibility = View.GONE
                        //turmas.add(0, tvTurmas.text.toString())
                        databaseReference.child("professores").child(userBd).child("turmas").setValue(tvTurmas.text)
                        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                        val editor = sharedPref.edit()
                        editor.putString("turmas", tvTurmas.text.toString())
                        turmas = tvTurmas.text.toString()
                        editor.apply()

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

    fun metodosProfessor(){

        val principalIndex: ConstraintLayout = findViewById(R.id.principal_index)
        val principalSemTurma: ConstraintLayout = findViewById(R.id.principal_semTurma)
        val principalProfessor: ConstraintLayout = findViewById(R.id.principal_index_professor)

        principalIndex.visibility = View.GONE
        principalSemTurma.visibility = View.GONE
        principalProfessor.visibility = View.VISIBLE

        val btnAddTurma: Button = findViewById(R.id.principal_index_professor_btnAddTurma)
        btnAddTurma.setOnClickListener {
            abreCadTurmasProf()
        }

        val btnConteudosProf: Button = findViewById(R.id.principal_index_professor_btnAddCont)
        btnConteudosProf.setOnClickListener {

            var cont=0

            val intent = Intent(this, ContDoProfActivity::class.java)
            Log.d("teste", "O valor de turmas antes de abrir a nova activity é "+turmas.get(0))
            intent.putExtra("turmas", turmas)
            intent.putExtra("escola", escola.toString())
            //intent.putExtra("qntTurmas", qntTurma.toString())
            intent.putExtra("disciplina", disciplina)
            intent.putExtra("userBd", userBd)
            intent.putExtra("colaboracao", colaboracao)
            startActivity(intent)
        }

        val btnLogoutProf: Button = findViewById(R.id.principal_index_professor_btnLogout)
        btnLogoutProf.setOnClickListener {

            val btnLogout: Button = findViewById(R.id.principal_index_btnLogout)
            btnLogout.performClick()
        }

        populaTopColaboradores()

        val btnAbrePerfil: Button = findViewById(R.id.principal_index_professor_btnPerfil)
        btnAbrePerfil.setOnClickListener {


            val intent = Intent(this, perfilActivity::class.java)
            intent.putExtra("userBd", userBd)
            intent.putExtra("escola", escola)
            intent.putExtra("nome", nome)
            intent.putExtra("tipo", tipo)
            intent.putExtra("imagem", imagem)
            //intent.putExtra("qntTurma", qntTurma.toString())
            intent.putExtra("turmas", turmas)
            intent.putExtra("colaboracao", colaboracao)
            intent.putExtra("disciplina", disciplina)
            startActivity(intent)
            finish()

        }

        val imageView: ImageView = findViewById(R.id.principal_index_professor_ivPerfil)
        imageView.setOnClickListener {
            btnAbrePerfil.performClick()
        }

        val btnAddDisciplina: Button = findViewById(R.id.principal_index_professor_btnMaisDisciplina)
        btnAddDisciplina.setOnClickListener {
            addDisciplinas()
        }

    }

    fun populaRecycleHelp(){

        val recyclerView : RecyclerView = findViewById(R.id.principal_index_professor_recyclerViewGiveHelp)

        val duvida: MutableList<String> = ArrayList()
        val conteudos: MutableList<String> = ArrayList()
        val aluno: MutableList<String> = ArrayList()
        val data: MutableList<String> = ArrayList()
        val bd: MutableList<String> = ArrayList()
        val bdAluno: MutableList<String> = ArrayList()

        var adapter: HelpMePleaseTeacherAdapter = HelpMePleaseTeacherAdapter(this, duvida, conteudos, aluno, data, bd, bdAluno)

        //define o tipo de layout (linerr, grid)
        //var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)


        //coloca o adapter na recycleview
        recyclerView.adapter = adapter

        recyclerView.layoutManager = linearLayoutManager

        // Notify the adapter for data change.
        adapter.notifyDataSetChanged()

        //constructor: context, nomedarecycleview, object:ClickListener
        recyclerView.addOnItemTouchListener(
            SalaDeAulaActivity.RecyclerTouchListener(
                this,
                recyclerView!!,
                object : SalaDeAulaActivity.ClickListener {

                    override fun onClick(view: View, position: Int) {

                        // openHelpPeopleMet(bd.get(position), conteudos.get(position), duvida.get(position), aluno.get(position), adapter, positio)
                        val paginaIndexProf: ConstraintLayout =
                            findViewById(R.id.principal_index_professor)
                        val paginaHelpingPeople: ConstraintLayout =
                            findViewById(R.id.principal_helpingPeople)
                        paginaIndexProf.visibility = View.GONE
                        paginaHelpingPeople.visibility = View.VISIBLE

                        val btnFechar: Button = findViewById(R.id.principal_helpingPeople_btnFechar)
                        btnFechar.setOnClickListener {
                            paginaIndexProf.visibility = View.VISIBLE
                            paginaHelpingPeople.visibility = View.GONE
                        }

                        val txNome: TextView = findViewById(R.id.principal_helpingPeople_txNome)
                        txNome.setText("Dúvida de: " + aluno.get(position))

                        val txDuvida: TextView = findViewById(R.id.principal_helpingPeople_txDuvida)
                        txDuvida.setText(duvida.get(position))

                        val txConteudo: TextView =
                            findViewById(R.id.principal_helpingPeople_txConteudo)
                        txConteudo.setText("Conteúdo: " + conteudos.get(position))

                        val etResposta: EditText =
                            findViewById(R.id.principal_helpingPeople_etResposta)
                        val btnResponder: Button =
                            findViewById(R.id.principal_helpingPeople_btnResponder)

                        etResposta.isEnabled = true
                        btnResponder.isEnabled = true

                        btnResponder.setOnClickListener {
                            if (etResposta.text.isEmpty()) {
                                etResposta.performClick()
                                etResposta.setError("Escreva sua resposta")
                            } else {
                                chamaDialog()
                                databaseReference.child("Helping").child(bd.get(position)).child("resposta").setValue(etResposta.text.toString())
                                databaseReference.child("Helping").child(bd.get(position)).child("aluno").setValue(bdAluno.get(position))
                                databaseReference.child("Helping").child(bd.get(position)).child("pergunta").setValue(duvida.get(position))
                                databaseReference.child("Helping").child(bd.get(position)).child("professorRespondeu").setValue(userBd)
                                databaseReference.child("Helping").child(bd.get(position)).child("conteudo").setValue(conteudos.get(position))

                                //apaga a pergunta
                                databaseReference.child("HelpMePlease").child(disciplina).child(bd.get(position)).removeValue()

                                //salva os pontos do user
                                colaboracao = colaboracao + 2
                                databaseReference.child("professores").child(userBd)
                                    .child("colaboracao").setValue(colaboracao)
                                val sharedPref: SharedPreferences = getSharedPreferences(
                                    getString(R.string.sharedpreferences),
                                    0
                                ) //0 é private mode
                                val editor = sharedPref.edit()
                                editor.putString("colaboracao", colaboracao.toString())
                                editor.apply()

                                duvida.removeAt(position)
                                conteudos.removeAt(position)
                                aluno.removeAt(position)
                                data.removeAt(position)
                                bd.removeAt(position)
                                bdAluno.removeAt(position)

                                adapter.notifyDataSetChanged()

                                etResposta.isEnabled = false
                                btnResponder.isEnabled = false
                                showToast("Mensagem respondida! Você ganhou 2 pontos de colaboração")
                                encerraDialog()


                            }

                        }
                    }

                    override fun onLongClick(view: View?, position: Int) {

                    }
                })
        )


        val rootRef = databaseReference.child("HelpMePlease").child(disciplina)
        rootRef.orderByChild("situacao").equalTo("aberta")
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            var values = querySnapshot.child("conteudo").getValue().toString()
                            conteudos.add(values)
                            values = querySnapshot.child("duvida").getValue().toString()
                            duvida.add(values)
                            values = querySnapshot.child("estudante").getValue().toString()
                            aluno.add(values)
                            values = querySnapshot.child("data").getValue().toString()
                            data.add(values)
                            values = querySnapshot.key.toString()
                            bd.add(values)
                            values = querySnapshot.child("bdAluno").getValue().toString()
                            bdAluno.add(values)


                            adapter.notifyDataSetChanged()
                        }

                    } else {

                        //Se entrou aqui e pq n achou nada
                        encerraDialog()
                        val textview: TextView = findViewById(R.id.textView19)
                        textview.setText("Nenhum aluno pediu ajuda")
                        recyclerView.visibility = View.GONE

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

    fun populaTopColaboradores(){

        val outrasNome: MutableList<String> = ArrayList()
        val outrasEscola: MutableList<String> = ArrayList()
        val outrasPontos: MutableList<String> = ArrayList()
        //val outrasImg: MutableList<String> = ArrayList()  nao tem a imagem armazenada em professores. PRecisaria replicar aqui e desisti.Mt trabalho para pouco.


        val rootRef = databaseReference.child("professores")
        //rootRef.child("colaboracao").orderByValue().limitToFirst(3)
        rootRef.orderByChild("escola").equalTo(escola)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {


                            var value = "nao"

                            //impedir que professores com 0 pontos ou null entrem na lista
                            if (querySnapshot.child("colaboracao").exists()){
                                value = querySnapshot.child("colaboracao").getValue().toString()
                                if (!value.equals("0")){
                                    outrasPontos.add(value)
                                    value = querySnapshot.child("nome").getValue().toString()
                                    outrasNome.add(value)
                                    value = querySnapshot.child("escola").getValue().toString()
                                    outrasEscola.add(value)
                                }

                            }





                                //txt1.setText("1º - "+nome+" - "+escola+" - "+pontos+" pontos")

                        }

                    } else {

                        Log.d("teste", "nao achou valores")
                        val layTopColaborators: ConstraintLayout = findViewById(R.id.layTopColaborators)
                        layTopColaborators.visibility = View.GONE

                    }

                    //exibindo os maiores colaboradores na ordem certa
                    val txt1: TextView = findViewById(R.id.layTopColaborators_txt1)
                    val txt2: TextView = findViewById(R.id.layTopColaborators_txt2)
                    val txt3: TextView = findViewById(R.id.layTopColaborators_txt3)
                    txt1.visibility = View.GONE
                    txt2.visibility = View.GONE
                    txt3.visibility = View.GONE

                    val arrayMaiores: MutableList<String> = ArrayList()

                    var index=0
                    var cont=0
                    var max =0
                    //quero pegar os 3 maiores valores.
                    //entao preciso descobrir se realmente existem tres ou mais
                    if (outrasPontos.size>=3){
                        max=3
                    } else if (outrasPontos.size==2){
                        max=2
                    } else if (outrasPontos.size==1){
                        max=1
                    } else {
                        max=0
                    }

                    while (cont<max){
                        index = getIndexOfLargest(outrasPontos) //descobre a posição do maior
                        arrayMaiores.add(outrasNome.get(index)) //copia os maiores valores para este array provisorio
                        arrayMaiores.add(outrasEscola.get(index))
                        arrayMaiores.add(outrasPontos.get(index))

                        outrasEscola.removeAt(index)
                        outrasNome.removeAt(index)
                        outrasPontos.removeAt(index)

                        if (cont==0){
                            val contcerto = cont*3
                            txt1.visibility = View.VISIBLE
                            txt1.setText("1º - "+arrayMaiores.get(contcerto)+" - "+arrayMaiores.get(contcerto+1)+" - "+arrayMaiores.get(contcerto+2)+" pontos")
                        } else if (cont==1){
                            val contcerto = cont*3
                            txt2.visibility = View.VISIBLE
                            txt2.setText("2º - "+arrayMaiores.get(contcerto)+" - "+arrayMaiores.get(contcerto+1)+" - "+arrayMaiores.get(contcerto+2)+" pontos")
                        } else {
                            val contcerto = cont*3
                            txt3.visibility = View.VISIBLE
                            txt3.setText("3º - "+arrayMaiores.get(contcerto)+" - "+arrayMaiores.get(contcerto+1)+" - "+arrayMaiores.get(contcerto+2)+" pontos")
                        }

                        cont++

                    }

                    //libera memoria
                    outrasEscola.clear()
                    outrasNome.clear()
                    outrasPontos.clear()

                    encerraDialog()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })

    }

    fun getIndexOfLargest(array: MutableList<String> = ArrayList()): Int {
        var largest = 0
        for (i in 1 until array.size) {
            if (array[i] > array[largest]) largest = i
        }
        return largest // position of the first largest found
    }

    fun addDisciplinas(){

        val paginaIndex: ConstraintLayout = findViewById(R.id.principal_index_professor)
        val paginaAddDisciplina: ConstraintLayout = findViewById(R.id.paginaAddDisciplinasAlternativas)
        paginaIndex.visibility = View.GONE
        paginaAddDisciplina.visibility = View.VISIBLE

        val btnFechar: Button = findViewById(R.id.paginaAddDisciplinas_btnFechar)
        btnFechar.setOnClickListener {
            paginaIndex.visibility = View.VISIBLE
            paginaAddDisciplina.visibility = View.GONE

        }

        val outrasDisciplinas: MutableList<String> = ArrayList()

        outrasDisciplinas.add(disciplina)
        if (disciplinasExtras.contains(";")){
            val tokens = StringTokenizer(disciplinasExtras, ";") //”*” este é delim
            val size = tokens.countTokens()
            var cont=0
            while (cont<size){
                val value = tokens.nextToken()
                outrasDisciplinas.add(value)
                cont++
            }
        }
        //outrasDisciplinas.add(disciplinasExtras)

        //monta a recuclerview com as disciplinas
        //chame aqui pelo adaptador que criamos, com o nome dado e o construtor

        //vamos usar esse adapter pois ele é simples e nos serve. Precisei repetir o array aqui mas so vamos usar uma vez, n precisa de bd
        var adapter: conteudosAdapter = conteudosAdapter(this, outrasDisciplinas, outrasDisciplinas)

        //chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.paginaAddDisciplinas_recyclerView)

        //define o tipo de layout (linerr, grid)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        //coloca o adapter na recycleview
        recyclerView.adapter = adapter

        recyclerView.layoutManager = linearLayoutManager

        // Notify the adapter for data change.
        adapter.notifyDataSetChanged()


        var list_of_items = arrayOf(
            "Selecione disciplina",
            "Português 1",
            "Português 2",
            "Matemática 1",
            "Matemática 2",
            "História",
            "Geografia",
            "Ciências",
            "Biologia",
            "Física",
            "Química",
            "Inglês",
            "Arte",
            "Ensino religioso",
            "Educação física"
        )

        //tambem pode usar mutable list assim:
        //var list_of_items = turmasDaEscola

        var disciplinaSelecionada = "Selecione Disciplina"
        val spinnerDisciplinas: Spinner = findViewById(R.id.spinner_novasDisciplinas)
        //Adapter for spinner
        spinnerDisciplinas.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list_of_items)

        val textView: TextView = findViewById(R.id.paginaAddDisciplinas_txNovaDisciplina)
        //item selected listener for spinner
        spinnerDisciplinas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                disciplinaSelecionada = list_of_items[position]

                textView.setText(disciplinaSelecionada)

            }
        }

        val btnAddDisciplina: Button = findViewById(R.id.paginaAddDisciplinas_btnAddDisciplina)
        btnAddDisciplina.setOnClickListener {
            chamaDialog()
            if (textView.text.isEmpty()) {
                showToast("Selecione uma disciplina primeiro")
                encerraDialog()
            } else if (disciplinaSelecionada.equals("Selecione disciplina")){
                showToast("Selecione uma disciplina primeiro")
                encerraDialog()
            } else {
                var cont=0
                var jaE= false
                while (cont<outrasDisciplinas.size){
                    if (disciplinaSelecionada.equals(outrasDisciplinas.get(cont))){
                        showToast("Você já escolheu esta disciplina antes")
                        jaE=true
                    }
                    cont++
                }
                if (jaE==false){
                    outrasDisciplinas.add(disciplinaSelecionada)
                    adapter.notifyDataSetChanged()

                }
                encerraDialog()

            }
        }

        val btnSalvarEsair: Button = findViewById(R.id.paginaAddDisciplinas_btnSalvar)
        btnSalvarEsair.setOnClickListener {

                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()

                var cont=0
                var disciplinaExtra: String = "nao"

                while (cont<outrasDisciplinas.size){
                    if (cont==0){
                        disciplina = outrasDisciplinas.get(cont)
                        disciplinaExtra = outrasDisciplinas.get(cont)
                    } else {
                        disciplinaExtra = disciplinaExtra+";"+outrasDisciplinas.get(cont)
                    }
                    cont++
                }

                //se tiver ; é pq tem mais de uma disciplina. Então vai salvar o valor conjunto que está em disciplina extra
                if (disciplinaExtra.contains(";")){
                    databaseReference.child("professores").child(userBd).child("disciplina").setValue(disciplinaExtra)
                } else {
                    //senao, salva o valor que está em disciplina e sabemos que é só 1 sem ";"
                    databaseReference.child("professores").child(userBd).child("disciplina").setValue(disciplina)
                }
                editor.putString("disciplina", disciplina)
                editor.apply()

            trocarDisciplina(disciplinasExtras)
            btnFechar.performClick()
        }


        //constructor: context, nomedarecycleview, object:ClickListener
        recyclerView.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView!!, object: ClickListener{

            override fun onClick(view: View, position: Int) {


                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ActivityPrincipal)
                builder.setMessage("Remover esta disciplina da lista?")
                    .setTitle("O que deseja fazer?")
                    .setCancelable(false)
                    .setPositiveButton("Sim, remover", DialogInterface.OnClickListener { dialog, which ->

                        if (outrasDisciplinas.size>1){
                            outrasDisciplinas.removeAt(position)
                            adapter.notifyDataSetChanged()

                            if (outrasDisciplinas.size==1){
                                val btnTrocarDisciplina: Button = findViewById(R.id.principal_index_professor_btnTrocarDisciplina)
                                btnTrocarDisciplina.visibility = View.GONE
                            }
                        } else {
                            showToast("Você não pode remover todas as disciplinas.")
                        }



                    })
                // Display a negative button on alert dialog
                builder.setNegativeButton("Não"){dialog,which ->

                }
                val alert : AlertDialog = builder.create()
                alert.show()

            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))





    }

    fun trocarDisciplina(disciplinas: String){

        val btnTrocarDisciplina: Button = findViewById(R.id.principal_index_professor_btnTrocarDisciplina)
        btnTrocarDisciplina.visibility = View.VISIBLE

        val outrasDisciplinas: MutableList<String> = ArrayList()

        //escreve no textview a disciplina que está selecionada
        val txDisciplinaSelecionada: TextView = findViewById(R.id.principal_index_professor_paginaDisciplinasParaEscolhertxDisciplinaEscolhida)
        txDisciplinaSelecionada.setText("Disciplina selecionada: "+disciplina)

        //imagine that txt has string “Fruit*they”
        val tokens = StringTokenizer(disciplinas, ";") //”*” este é delim
        val size = tokens.countTokens()
        var cont=0
        while (cont<size){
            val value = tokens.nextToken()
            outrasDisciplinas.add(value)
            cont++
        }

        //abre e fecha o layout
        btnTrocarDisciplina.setOnClickListener {
            val paginaDiscipliansParaEscolher: ConstraintLayout = findViewById(R.id.principal_index_professor_paginaDisciplinasParaEscolher)
            if (paginaDiscipliansParaEscolher.isVisible){
                paginaDiscipliansParaEscolher.visibility = View.GONE
            } else {
                paginaDiscipliansParaEscolher.visibility = View.VISIBLE
            }

        }

        //vamos usar esse adapter pois ele é simples e nos serve. Precisei repetir o array aqui mas so vamos usar uma vez, n precisa de bd
        var adapter: conteudosAdapter = conteudosAdapter(this, outrasDisciplinas, outrasDisciplinas)

        //chame a recyclerview
        var recyclerView: RecyclerView = findViewById(R.id.principal_index_professor_paginaDisciplinasParaEscolherRecycler)

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


                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ActivityPrincipal)
                builder.setMessage("A nova disciplina será "+outrasDisciplinas.get(position))
                    .setTitle("Trocar a disciplina "+disciplina+"?")
                    .setCancelable(false)
                    .setPositiveButton("Sim, trocar", DialogInterface.OnClickListener { dialog, which ->

                        val temp = outrasDisciplinas.get(position)
                        //coloca a disciplina da vez no lugar da disciplian que trocou
                        outrasDisciplinas.set(position, disciplina)
                        //agora disciplina da vez vira a que ele selecionou
                        disciplina = temp
                        adapter.notifyDataSetChanged()

                        //fecha o layout
                        btnTrocarDisciplina.performClick()

                    })
                // Display a negative button on alert dialog
                builder.setNegativeButton("Não"){dialog,which ->

                }
                val alert : AlertDialog = builder.create()
                alert.show()

            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))




    }










    fun enterTheGame(){

    }










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

    //metodos de update automático do app
    private fun checkForAppUpdate() {
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                try {
                    val installType = when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                        else -> null
                    }
                    if (installType == AppUpdateType.FLEXIBLE) appUpdateManager.registerListener(appUpdatedListener)

                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        installType!!,
                        this,
                        262)
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 262) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this,
                    "Update falhou. Por vaor tente novamente da próxima vez que abrir o app.",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        /*
        val snackbar = Snackbar.make(
            findViewById(R.id.drawer_layout),
            "Update concluído.",
            Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("RESTART") { appUpdateManager.completeUpdate() }
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.azulClaro))
        snackbar.show()

         */
        showToast("Aplicativo atualizado!")
    }


    fun showToast(message: String){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

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

    /* To hide Keyboard */
    fun hideKeyboard() {
        try {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isNullOrEmpty(str: String?): Boolean {
        if (str != null && !str.isEmpty())
            return false
        return true
    }

}
