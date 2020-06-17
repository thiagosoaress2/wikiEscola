package com.wikiEscolawiki.wikiescola

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Base64.DEFAULT
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    var userBd: String = "nao"
    var senhaProvi: String = "nao"
    val criptoPass = "Sup3rN3rd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseReference = FirebaseDatabase.getInstance().reference

        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        userBd  = sharedPref.getString("userBd", "nao").toString()
        if (userBd.equals("nao")){
            updateUi("nao")
        } else{
            updateUi(userBd)
        }

    }

    //processo de login-Verifica o status do user e atualiza chama UpdateUI
    override fun onStart() {
        super.onStart()

        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        userBd  = sharedPref.getString("userBd", "nao").toString()
        updateUi(userBd)


        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                //Carregue o objeto que vai receber a animação
                val layBg: ConstraintLayout = findViewById(R.id.loginContainer)
                //carregue a animação
                val animacao = AnimationUtils.loadAnimation(this@MainActivity, R.anim.main_subelay)
                //utilize assim
                layBg.visibility = View.VISIBLE
                layBg.startAnimation(animacao)

            }

        }
        timer.start()

    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
        val editor = sharedPref.edit()

        userBd  = sharedPref.getString("userBd", "nao").toString()
        updateUi(userBd)


        //textwatcher do edittext
        val fieldCel: EditText =findViewById(R.id.main_index_etCel)
        fieldCel.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (fieldCel.length()<9){
                    fieldCel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    fieldCel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                }
            }

        }
        )
    }

    fun updateUi(user: String){

        val index: ConstraintLayout = findViewById(R.id.main_index)
        val mainSenha: ConstraintLayout = findViewById(R.id.main_senha)
        val mainNovoUser: ConstraintLayout = findViewById(R.id.main_novoAluno)

        if (user.equals("nao")){
            index.visibility = View.VISIBLE
            mainSenha.visibility = View.GONE
            mainNovoUser.visibility = View.GONE
            val btnProximo: Button = findViewById(R.id.main_index_btnContinuar)
            btnProximo.setOnClickListener {
                val etCel: EditText = findViewById(R.id.main_index_etCel)
                if (etCel.length()!=9){
                    etCel.setError("Algo errado. Confira o número")
                    etCel.requestFocus()
                } else {
                    userBd = etCel.text.toString()
                    val user = "faltaSenha"
                    updateUi(user)
                }
            }
        } else if (user.equals("faltaSenha")){
            chamaDialog()
            vericicaSeEteUsuarioExiste()

        } else if (user.equals("novoUser")){
            index.visibility = View.GONE
            mainSenha.visibility = View.GONE
            mainNovoUser.visibility = View.VISIBLE
            novoUser()
        } else if (user.equals("userExisteSemSenha")){
            index.visibility = View.GONE
            mainSenha.visibility = View.VISIBLE
            mainNovoUser.visibility = View.GONE
            testaSenha()
        }
        else {

            val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
            val editor = sharedPref.edit()
            editor.putString("userBd", userBd)
            editor.apply()

            val intent = Intent(this, ActivityPrincipal::class.java)
            //n precisa passar nenhum valor. Está tudo no sharedprefs
            startActivity(intent)

        }

    }

    fun vericicaSeEteUsuarioExiste (){

        val rootRef = databaseReference.child("usuarios").child(userBd)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
                encerraDialog()
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){

                    senhaProvi = p0.child("senha").getValue().toString()
                    senhaProvi = decrypt(senhaProvi).toString()
                    val user = "userExisteSemSenha"
                    updateUi(user)
                    encerraDialog()
                } else {
                    //novo usuario
                    encerraDialog()
                    val user = "novoUser"
                    updateUi("novoUser")
                }
            }
        })

    }

    fun novoUser (){

        val etSenha: EditText = findViewById(R.id.main_novoAluno_etSenha)
        val etConfirmaSenha: EditText = findViewById(R.id.main_novoAluno_etSenhaConfirma)
        val btnCriar: Button = findViewById(R.id.main_novoAluno_btnEntrar)

        etSenha.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (etSenha.length()>=6){
                    etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                }
            }

        }
        )

        etSenha.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (etConfirmaSenha.length()>=6 && etSenha.text.equals(etConfirmaSenha.text)){
                    etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                }
            }

        }
        )

        btnCriar.setOnClickListener {
            if (etSenha.text.isEmpty()){
                etSenha.setError("Informe uma senha")
                etSenha.requestFocus()
            } else if (etSenha.text.length>6){
                etSenha.setError("6 dígitos no máximo")
                etSenha.requestFocus()
            } else if (!etConfirmaSenha.text.toString().equals(etSenha.text.toString())){
                etConfirmaSenha.setError("As senhas estão diferentes")
                etConfirmaSenha.setText("")
                etConfirmaSenha.requestFocus()
            } else {
                //criar bd
                databaseReference = FirebaseDatabase.getInstance().reference

                val href = databaseReference.child("usuarios").child(userBd)

                href.child("primeiroAcesso").setValue(0)
                href.child("cel").setValue(userBd)
                href.child("tipo").setValue("aluno")
                href.child("nome").setValue("nao")
                href.child("apelido").setValue("nao")
                href.child("imagem").setValue("nao")
                var senha = etSenha.text.toString()
                senha = encrypt(senha).toString()
                href.child("senha").setValue(senha)


                //agora salvar no shared
                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()

                editor.putString("primeiroAcesso", "0")
                editor.putString("cel", userBd)
                editor.putString("tipo", "aluno")
                editor.putString("nome", "nao")
                editor.putString("apelido", "nao")
                editor.apply()

                val user = "logado"
                updateUi(user)
            }
        }
    }

    fun testaSenha(){

        val etSenha: EditText = findViewById(R.id.main_senha_etSenha)
        etSenha.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (etSenha.length()<6){
                    etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                }
            }

        }
        )
        val btnLogin: Button = findViewById(R.id.main_senha_btnEntrar)
        btnLogin.setOnClickListener {
            if (etSenha.text.isEmpty()){
                etSenha.setError("Informe a senha")
                etSenha.requestFocus()
            } else if (!etSenha.text.toString().equals(senhaProvi)){
                etSenha.setError("A senha está incorreta")
                etSenha.requestFocus()
            } else {
                val user = "logado"
                //agora salvar no shared
                val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences), 0) //0 é private mode
                val editor = sharedPref.edit()

                editor.putString("cel", userBd)
                editor.apply()

                updateUi(user)
            }
        }

    }

    fun encrypt(strToEncrypt: String) : String? {
        try
        {

            val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
            val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =  PBEKeySpec(criptoPass.toCharArray(), Base64.decode(salt, DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
        }
        catch (e: Exception)
        {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt : String) : String? {
        try
        {
            val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
            val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX
            val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =  PBEKeySpec(criptoPass.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec);
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return  String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        }
        catch (e : Exception) {
            println("Error while decrypting: $e");
        }
        return null
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
