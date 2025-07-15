package com.example.marketplace1101

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        val nameField = findViewById<EditText>(R.id.editTextRegisterName)
        val cpfField = findViewById<EditText>(R.id.editTextCPF)
        val emailField = findViewById<EditText>(R.id.editTextRegisterEmail)
        val passwordField = findViewById<EditText>(R.id.editTextRegisterPassword)
        val confirmPasswordField = findViewById<EditText>(R.id.editTextRegisterConfirmPassword)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val name = nameField.text.toString().trim()
            val cpf = cpfField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (name.isEmpty() || cpf.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    val user = hashMapOf(
                        "uid" to uid,
                        "nome" to name,
                        "cpf" to cpf,
                        "email" to email
                    )

                    firestore.collection("usuarios").document(uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    if (e.message?.contains("email address is already in use") == true) {
                        Toast.makeText(this, "Este e-mail já está cadastrado. Tente novamente!.", Toast.LENGTH_LONG).show()

                    } else {
                        Toast.makeText(this, "Erro no cadastro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}