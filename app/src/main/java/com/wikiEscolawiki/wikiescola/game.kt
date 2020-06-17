package com.wikiEscolawiki.wikiescola

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_game.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class game : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    var escola = "nao"
    var turma = "nao"
    var userBd = "nao"
    var imagem = "nao"
    var nome = "nao"
    var pontos=0

    val delim = "!?!"

    val arrayNome: MutableList<String> = ArrayList()
    val arrayEscola: MutableList<String> = ArrayList()
    val arrayPontos: MutableList<String> = ArrayList()
    val arrayImg: MutableList<String> = ArrayList()
    val arrayBd: MutableList<String> = ArrayList()

    var jogadas = 0

    lateinit var adapterOnlineUsers: gameOnlineUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        databaseReference = FirebaseDatabase.getInstance().reference

        escola = intent.getStringExtra("escola").toString()
        userBd = intent.getStringExtra("userBd").toString()
        imagem = intent.getStringExtra("imagem").toString()
        nome = intent.getStringExtra("nome").toString()
        turma = intent.getStringExtra("turma").toString()

        val imageViewDoAluno: ImageView = findViewById(R.id.game_imageview)
        if (imagem.equals("nao")){

            try {
                Glide.with(applicationContext)
                    .load(R.drawable.blankprofile) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(this)) // applying the image transformer
                    .into(imageViewDoAluno)
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
                    .into(imageViewDoAluno)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        inicioAposCreate()
    }



    fun inicioAposCreate(){

        val txNome: TextView = findViewById(R.id.game_txNome)
        txNome.setText(nome)

        val day = getDay()
        if (day.equals("quarta-feira") || day.equals("quinta-feira")){
            //começa o processo
            startTheGame()
        } else {
            //encerra processo
            showToast("Os jogos são na quarta e quinta-feira apenas.")
        }

    }

    fun startTheGame(){
        val paginaIndex: ConstraintLayout = findViewById(R.id.game_index)
        paginaIndex.visibility = View.VISIBLE

        setBarChart()

    }

    private fun setBarChart() {

        var barChartView = findViewById<BarChart>(R.id.barChart)

        //definir valores de legenda
        var xAxisValues = ArrayList<String>()
        xAxisValues.add("Jan")
        xAxisValues.add("Feb")
        xAxisValues.add("Mar")
        xAxisValues.add("Apr")
        xAxisValues.add("May")
        xAxisValues.add("June")
        xAxisValues.add("Jul")
        xAxisValues.add("Aug")
        xAxisValues.add("Sep")
        xAxisValues.add("Oct")
        xAxisValues.add("Nov")
        xAxisValues.add("Dec")

        //define valores das barras
        var yValueGroup1 = ArrayList<BarEntry>()

        yValueGroup1.add(BarEntry(1f, floatArrayOf(9.toFloat(), 3.toFloat())))
        yValueGroup1.add(BarEntry(2f, floatArrayOf(3.toFloat(), 3.toFloat())))
        yValueGroup1.add(BarEntry(3f, floatArrayOf(3.toFloat(), 3.toFloat())))


        //Prepare Group Data
        var barDataSet1: BarDataSet

        barDataSet1 = BarDataSet(yValueGroup1, "")
        barDataSet1.setColors(*ColorTemplate.COLORFUL_COLORS)

        barDataSet1.setDrawIcons(false)
        barDataSet1.setDrawValues(false)

        // Pass Both Bar Data Set's in Bar Data.
        var barData = BarData(barDataSet1)
        barChartView.description.isEnabled = false
        barChartView.description.textSize = 0f
        barData.setValueFormatter(LargeValueFormatter())
        barChartView.setData(barData)
        //barChartView.getBarData().setBarWidth(barWidth)
        barChartView.getXAxis().setAxisMinimum(0f)
        barChartView.getXAxis().setAxisMaximum(12f)
        //barChartView.groupBars(0f, groupSpace, barSpace)
        //   barChartView.setFitBars(true)
        barChartView.getData().setHighlightEnabled(false)
        barChartView.invalidate()

        //format legend x axis
        val xAxis = barChartView.getXAxis()
        xAxis.setGranularity(1f)
        xAxis.setGranularityEnabled(true)
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 9f

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setValueFormatter(IndexAxisValueFormatter(xAxisValues))

        xAxis.setLabelCount(12)
        xAxis.mAxisMaximum = 12f
        xAxis.setCenterAxisLabels(true)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.spaceMin = 4f
        xAxis.spaceMax = 4f

        //format y axis
        val leftAxis = barChartView.getAxisLeft()
        leftAxis.setValueFormatter(LargeValueFormatter())
        leftAxis.setDrawGridLines(false)
        leftAxis.setSpaceTop(1f)
        leftAxis.setAxisMinimum(0f)

        barChartView.data = barData
        barChartView.setVisibleXRange(1f, 12f)



        /*
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(8f, 0f))
        entries.add(BarEntry(2f, 1f))
        entries.add(BarEntry(5f, 2f))
        entries.add(BarEntry(20f, 3f))
        entries.add(BarEntry(15f, 4f))
        entries.add(BarEntry(19f, 5f))

        val barDataSet = BarDataSet(entries, "Cells")



        val labels = ArrayList<String>()
        labels.add("18-Jan")
        labels.add("19-Jan")
        labels.add("20-Jan")
        labels.add("21-Jan")
        labels.add("22-Jan")
        labels.add("23-Jan")

        val chart : BarChart = findViewById(R.id.barChart)
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        val data = BarData(barDataSet)
        barChart.data = data // set the data and list of lables into chart

        //barChart.setDescription("Set Bar Chart Description")  // set the description

        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
        barDataSet.color = resources.getColor(R.color.colorAccent)

        barChart.animateY(5000)


         */
    }









    fun getDay () :  String {

        val input_date = GetDate()
        val format1 = SimpleDateFormat("dd/MM/yyyy")
        val dt1 = format1.parse(input_date)
        val format2: DateFormat = SimpleDateFormat("EEEE")
        val finalDay = format2.format(dt1)
        return finalDay
    }

    //pega  a data
    private fun GetDate () : String {

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())

        return currentDate
    }







    //exibe a tela inicial que conta 5 segundos antes da partida começar
    fun startBatlle(){

        val topoDaTela: ConstraintLayout = findViewById(R.id.topoDaTela)
        val game_index: ConstraintLayout = findViewById(R.id.game_index)
       // val paginaDesafiando : ConstraintLayout = findViewById(R.id.paginaDesafiando)
        val paginaBatalha: ConstraintLayout = findViewById(R.id.paginaBatalha)
        val paginaINicialDaBatalha: ConstraintLayout = findViewById(R.id.paginaBatalha_inicio)
        topoDaTela.visibility = View.GONE
        game_index.visibility = View.GONE
       // paginaDesafiando.visibility = View.GONE

        paginaINicialDaBatalha.visibility = View.VISIBLE
        paginaBatalha.visibility = View.VISIBLE


        val txTimerCountdown: TextView = findViewById(R.id.paginaBatalha_inicio_txTimer)
        var time=10
        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                time=time-1
                txTimerCountdown.setText(time.toString())
            }

            override fun onFinish() {
                paginaINicialDaBatalha.visibility = View.GONE
                randomDisciplina()
            }

        }
        timer.start()



    }

    //metodoq ue sorteia qual disciplina virá
    fun randomDisciplina(){

            /*
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
             */ //14 disciplinas

        chamaDialog()
        val randomNumber = rand(1, 14)
        var disciplina = "nao"
        if (randomNumber==1){
            disciplina = "Português 1"
            //1
        } else if (randomNumber==2){
            disciplina = "Português 2"
            //2
        } else if (randomNumber==3){
            disciplina = "Matemática 1"
            //3
        } else if (randomNumber==4){
            disciplina = "Matemática 2"
            //4
        }else if (randomNumber==5){
            disciplina = "História"
            //5
        }else if (randomNumber==6){
            disciplina = "Geografia"
            //6
        }else if (randomNumber==7){
            disciplina = "Ciências"
            //7
        }else if (randomNumber==8){
            disciplina = "Biologia"
            //8
        }else if (randomNumber==9){
            disciplina = "Física"
            //9
        }else if (randomNumber==10){
            disciplina = "Química"
            //10
        }else if (randomNumber==11){
            disciplina = "Inglês"
            //11
        }else if (randomNumber==12){
            disciplina = "Arte"
            //12
        }else if (randomNumber==13){
            disciplina = "Ensino religioso"
            //13
        }else {
            disciplina = "Educação física"
            //14
        }

        queryPergunta(disciplina)

    }

    //busca a pergunta apos o sorteio. Dentro da query já monta a pergunta
    fun queryPergunta(disciplina: String){

        chamaDialog()


        val rootRef = databaseReference.child("questões").child(disciplina)
        rootRef.orderByChild("tentativas").limitToFirst(1)
            //getInstance().reference.child("usuarios").orderByChild("email").equalTo(userMail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.exists()){

                        for (querySnapshot in dataSnapshot.children) {

                            val imagem = querySnapshot.child("img").value.toString()

                            /*
                            if (values.equals("nao")){
                                //do nothing
                                imagem.visibility = View.GONE
                            } else {
                                Glide.with(this@SalaDeAulaActivity).load(values).into(imagem)
                            }

                             */


                            //temPerguntas=true

                            val pergunta = querySnapshot.child("pergunta").value.toString()

                            val textoExtra = querySnapshot.child("textoExtra").value.toString()

                            val opcaoA = querySnapshot.child("opcaoA").value.toString()

                            val opcaoB = querySnapshot.child("opcaoB").value.toString()

                            val opcaoC = querySnapshot.child("opcaoC").value.toString()

                            val opcaoD = querySnapshot.child("opcaoD").value.toString()

                            val opcaoE = querySnapshot.child("opcaoE").value.toString()

                            val opcaoCorreta = querySnapshot.child("opcaoCorreta").value.toString()

                            exibePergunta(pergunta, imagem, textoExtra, opcaoA, opcaoB, opcaoC, opcaoD, opcaoE, opcaoCorreta)

                            encerraDialog()
                        }

                    } else {

                        randomDisciplina()
                        //showToast("Aguarde, buscando perguntas")


                    }

                    //encerraDialog()

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    encerraDialog()
                    // ...
                }
            })


    }

    //exibe a pergunta com os dados passados pela query e coloca os listeners
    fun exibePergunta(pergunta: String, imagem: String, txtExtra: String, opcaoA:String, opcaoB:String, opcaoC:String,
                      opcaoD:String, opcaoE:String, opcaoCorreta:String){

        val imageView: ImageView = findViewById(R.id.respondendoQuestao_imageView)
        val txtpergunta: TextView = findViewById(R.id.respondendoQuestao_tvPergunta)
        val textoExtra: TextView = findViewById(R.id.respondendoQuestao_tvPerguntaExtra)
        val txtOpcaoA: TextView = findViewById(R.id.question_adicionando_lineA_txt)
        val txtOpcaoB: TextView = findViewById(R.id.question_adicionando_lineB_txt)
        val txtOpcaoC: TextView = findViewById(R.id.question_adicionando_lineC_txt)
        val txtOpcaoD: TextView = findViewById(R.id.question_adicionando_lineD_txt)
        val txtOpcaoE: TextView = findViewById(R.id.question_adicionando_lineE_txt)

        txtpergunta.setText(pergunta)
        if (imagem.equals("nao")){
            imageView.visibility = View.GONE
        } else {
            Glide.with(this).load(imagem).into(imageView)
            var zoom = false
            imageView.setOnClickListener {
                zoomEffectQuestao(zoom)
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
        txtOpcaoA.setText(opcaoA)
        txtOpcaoB.setText(opcaoB)
        txtOpcaoC.setText(opcaoC)
        txtOpcaoD.setText(opcaoD)
        txtOpcaoE.setText(opcaoE)

        val btnA: Button = findViewById(R.id.question_adicionando_lineA_btn)
        val btnB: Button = findViewById(R.id.question_adicionando_lineB_btn)
        val btnC: Button = findViewById(R.id.question_adicionando_lineC_btn)
        val btnD: Button = findViewById(R.id.question_adicionando_lineD_btn)
        val btnE: Button = findViewById(R.id.question_adicionando_lineE_btn)

        val btnProxima: Button = findViewById(R.id.respondendoQuestao_btnProxima)

        btnA.setOnClickListener {
            if (opcaoCorreta.equals("a")){
                computaAcerto()
            } else {
                computaErro()
            }
            btnProxima.performClick()
        }
        btnB.setOnClickListener {
            if (opcaoCorreta.equals("b")){
                computaAcerto()
            } else {
                computaErro()
            }
            btnProxima.performClick()
        }
        btnC.setOnClickListener {
            if (opcaoCorreta.equals("c")){
                computaAcerto()
            } else {
                computaErro()
            }
            btnProxima.performClick()
        }
        btnD.setOnClickListener {
            if (opcaoCorreta.equals("d")){
                computaAcerto()
            } else {
                computaErro()
            }
            btnProxima.performClick()
        }
        btnE.setOnClickListener {
            if (opcaoCorreta.equals("e")){
                computaAcerto()
            } else {
                computaErro()
            }
            btnProxima.performClick()
        }

        val paginaPergunta: ConstraintLayout = findViewById(R.id.paginaBatalha_jogando)
        paginaPergunta.visibility = View.VISIBLE

    }

    //ajusta o valor caso o user acerte
    fun computaAcerto(){

        val txMeusPontos: TextView = findViewById(R.id.batalha_jogando_pontosEu)
        val valorAtual = txMeusPontos.text.toString()
        txMeusPontos.setText((valorAtual.toInt()+10).toString())
        showToast("Acertou! Ganhou 10 pontos")

    }

    //ajusta o valor caso o user erre
    fun computaErro(){

        val txMeusPontos: TextView = findViewById(R.id.batalha_jogando_pontosEu)
        if (txMeusPontos.text.equals("0")){
            val txPontosAdversario: TextView = findViewById(R.id.batalha_jogando_pontosAdv)
            val valorAtual = txPontosAdversario.text.toString()
            txPontosAdversario.setText(valorAtual.toInt()+5)
            showToast("Errou! Seu adversário ganhou 5 pontos")
        } else {
            val valorAtual = txMeusPontos.text.toString()
            txMeusPontos.setText((valorAtual.toInt()-5).toString())
            showToast("Errou! perdeu 5 pontos")
        }

    }











    fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    fun showToast(message: String){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
















    fun zoomEffectQuestao (zoom: Boolean){

                //dar zoom
                val cL = findViewById(R.id.respondendoQuestao_imageView) as ImageView
                val lp =  cL.layoutParams as ConstraintLayout.LayoutParams
                lp.width = lp.width*2
                lp.height = lp.height*2
                cL.layoutParams = lp




    }

    fun placeImageInside(link: String, imageView: ImageView){

        if (link.equals("nao")){

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


        } else {

            try {
                Glide.with(applicationContext)
                    .load(link) //.load(mRootRef+"/imgperfil"+imr_src_so_pra_testar)
                    .thumbnail(0.9f)
                    .skipMemoryCache(true)
                    .transform(CircleTransform(this)) // applying the image transformer
                    .into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
