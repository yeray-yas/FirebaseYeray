package com.yerayyas.firebaseyeray

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.yerayyas.firebaseyeray.databinding.ActivityHomeBinding


enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")


    }

    private fun setup(email: String, provider: String) {
        title = "Home"

       with(binding){
            emailTextView.text = email
           providerTextView.text = provider

           logOutButton.setOnClickListener {
               FirebaseAuth.getInstance().signOut()
               onBackPressed()

           }
       }



    }
}