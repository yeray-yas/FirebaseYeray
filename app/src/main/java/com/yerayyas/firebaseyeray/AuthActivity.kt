package com.yerayyas.firebaseyeray

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.yerayyas.firebaseyeray.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Firebase integration complete")
        analytics.logEvent("InitScreen", bundle)

        // Setup
        setup()


    }

    private fun setup() {
        title = "Authentication"

        with(binding) {
            val mail = emailEditText.text
            val pass = passwordEditText.text
            signUpButton.setOnClickListener {
                // Analitycs event
                val analytics = FirebaseAnalytics.getInstance(this@AuthActivity)
                analytics.logEvent("UserSigned", Bundle().apply {
                    putString("message", "Usuario registrado")
                })
                if (mail.isNotEmpty() && pass.isNotEmpty()) {
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(mail.toString(), pass.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@AuthActivity,
                                    "Usuario registrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showHome(it.result.user?.email ?: "", ProviderType.BASIC)
                            } else {
                                showSingInAlert()
                                //Toast.makeText(this@AuthActivity, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@AuthActivity, "Empty field/s", Toast.LENGTH_SHORT).show()
                }
            }
            loginButton.setOnClickListener {
                if (mail.isNotEmpty() && pass.isNotEmpty()) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(mail.toString(), pass.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@AuthActivity,
                                    "Usuario logueado satisfactoriamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showHome(it.result.user?.email ?: "", ProviderType.BASIC)
                            } else {
                                showLoginAlert()
                                //Toast.makeText(this@AuthActivity, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@AuthActivity, "Empty field/s", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSingInAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Error al registrar usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    private fun showLoginAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Error en mail y/o password")
        builder.setPositiveButton("Aceptar", null)
        val dialog = builder.create()
        dialog.show()
    }
}