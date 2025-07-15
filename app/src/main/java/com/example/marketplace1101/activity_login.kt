package com.example.marketplace1101

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color
import android.widget.TextView
import androidx.core.graphics.toColorInt
import android.content.Intent
import android.widget.ImageView


class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Criação da instanscia do Auth do Firebase
        auth = FirebaseAuth.getInstance()

        val textViewRegister = findViewById<TextView>(R.id.textViewRegister)
        val textViewResetPassword = findViewById<TextView>(R.id.textViewResetPassword)


        //Fade-in na Logo
        val imageView = findViewById<ImageView>(R.id.imageLogo)

        imageView.alpha = 0f // começa invisível
        imageView.animate()
            .alpha(1f)       // termina visível
            .setDuration(2000) // duração em milissegundos (1s)
            .start()

        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)

        //Ação do botao de Login
        buttonLogin.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            //Autenticação do email e senha com o Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(this, RecipeListActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Email ou Senha Incorretos!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        //Animação para mostrar o clique no Botão de registrar
        val registerText = findViewById<TextView>(R.id.textViewRegister)
        registerText.setOnClickListener {
            it.animate()
                .alpha(0.6f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    // Redirecionamento para a tela de cadastro
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
                .start()
        }

        //Ação do botao de Registro
        textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //Ação do botão de Resetar a Senha
        textViewResetPassword.setOnClickListener {
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
        }
    }
}