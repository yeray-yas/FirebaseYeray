package com.yerayyas.firebaseyeray

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.*
import com.google.firebase.messaging.FirebaseMessaging
import com.yerayyas.firebaseyeray.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private lateinit var binding: ActivityAuthBinding
    private val callbackManager = CallbackManager.Factory.create()

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

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
        session()
        notification()


    }

    private fun notification() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String> ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("PUSH_TOKEN", "pushToken: $token")
        }


        // Temas (Topics)
        FirebaseMessaging.getInstance().subscribeToTopic("tutorial")

        // Recuperar información
        val url = intent.getStringExtra("url")
        url?.let {
            println("Ha llegado información en una push: $it")
        }


    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            // Quiere decir que ya tenemos iniciada una sesión en nuestra app
            binding.authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    override fun onStart() {
        super.onStart()

        binding.authLayout.visibility = View.VISIBLE
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
            googleButton.setOnClickListener {

                val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val googleClient = GoogleSignIn.getClient(this@AuthActivity, googleConf)
                googleClient.signOut()

                startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
            }
            facebookButton.setOnClickListener {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this@AuthActivity, listOf("email"))
                LoginManager.getInstance()
                    .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                        override fun onCancel() {
                            // TODO
                        }

                        override fun onError(error: FacebookException) {
                            showLoginAlert()
                        }

                        override fun onSuccess(result: LoginResult) {
                            result.let {
                                val token = it.accessToken
                                val credential = FacebookAuthProvider.getCredential(token.token)

                                FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                this@AuthActivity,
                                                "Usuario logueado en Google satisfactoriamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showHome(
                                                it.result.user?.email ?: "",
                                                ProviderType.FACEBOOK
                                            )
                                        } else {
                                            showLoginAlert()
                                        }
                                    }

                            }
                        }


                    })
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@AuthActivity,
                                    "Usuario logueado en Google satisfactoriamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showHome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showLoginAlert()
                            }
                        }
                }
            } catch (e: ApiException) {
                showLoginAlert()
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}