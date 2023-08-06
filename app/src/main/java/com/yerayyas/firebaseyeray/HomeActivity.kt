package com.yerayyas.firebaseyeray

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.yerayyas.firebaseyeray.ProviderType.FACEBOOK
import com.yerayyas.firebaseyeray.databinding.ActivityHomeBinding


enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
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

        // Saving data
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            .edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        // Remote Config
        binding.errorButton.visibility = View.INVISIBLE
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val showErrorButton = Firebase.remoteConfig.getBoolean("show_error_button")
                val errorButtonText = Firebase.remoteConfig.getString("error_button_text")

                if (showErrorButton) {
                    binding.errorButton.visibility = View.VISIBLE
                }

                binding.errorButton.text = errorButtonText

            }
        }


    }

    private fun setup(email: String, provider: String) {
        title = "Home"

        with(binding) {
            emailTextView.text = email
            providerTextView.text = provider

            logOutButton.setOnClickListener {

                //Erase data
                val prefs =
                    getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                        .edit()
                prefs.clear()
                prefs.apply()

                if (provider == FACEBOOK.name) {
                    LoginManager.getInstance().logOut()
                }

                FirebaseAuth.getInstance().signOut()
                onBackPressed()

            }


            errorButton.setOnClickListener {

                FirebaseCrashlytics.getInstance().setUserId(email)
                FirebaseCrashlytics.getInstance().setCustomKey("provider", provider)

                // Sending context log
                FirebaseCrashlytics.getInstance().log("The force crash button has been clicked")

                // Forcing error handling
                throw RuntimeException("Force error")
            }

            saveButton.setOnClickListener {
                db.collection("user").document(email).set(
                    hashMapOf(
                        "provider" to provider,
                        "address" to addressTextView.text.toString(),
                        "phone" to phoneTextView.text.toString()

                    )
                )
            }
            getButton.setOnClickListener {
                db.collection("user").document(email).get().addOnSuccessListener {
                    addressTextView.setText(it.get("address") as String?)
                  phoneTextView.setText(it.get("phone") as String?)
                }
            }
            eraseButton.setOnClickListener {
                db.collection("user").document(email).delete()
            }
        }
    }

}