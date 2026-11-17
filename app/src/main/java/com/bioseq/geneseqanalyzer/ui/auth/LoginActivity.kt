package com.bioseq.geneseqanalyzer.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bioseq.geneseqanalyzer.MainActivity
import com.bioseq.geneseqanalyzer.R
import com.bioseq.geneseqanalyzer.databinding.ActivityLoginBinding
import com.bioseq.geneseqanalyzer.utils.AuthResult
import com.bioseq.geneseqanalyzer.utils.ThemePreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentData = result.data
        if (intentData != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intentData)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    viewModel.signInWithGoogleCredential(credential)
                } else {
                    Toast.makeText(
                        this,
                        "Google Sign-In: ID token missing. Register SHA-1 & Web Client ID in Firebase Console.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.DEVELOPER_ERROR ->
                        "Developer Error (Code 10): Register SHA-1 fingerprint & Web Client ID in Firebase Console."
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                        "Google Sign-In was cancelled by user."
                    GoogleSignInStatusCodes.NETWORK_ERROR ->
                        "Network Error: Please check your device internet connection."
                    else ->
                        "Google Sign-In Error (Code ${e.statusCode}): ${e.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Google Sign-In: No response received from Google Play Services.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePreferences.applySavedTheme(this)
        super.onCreate(savedInstanceState)

        if (viewModel.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupListeners()
        observeViewModel()
    }

    private fun setupGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id_placeholder)
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()

        if (webClientId.isNotEmpty() && !webClientId.contains("YOUR_WEB_CLIENT_ID")) {
            gsoBuilder.requestIdToken(webClientId)
        }

        googleSignInClient = GoogleSignIn.getClient(this, gsoBuilder.build())
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val webClientId = getString(R.string.default_web_client_id_placeholder)
            if (webClientId.contains("YOUR_WEB_CLIENT_ID")) {
                Toast.makeText(
                    this,
                    "Setup Notice: Replace default_web_client_id_placeholder in strings.xml with your Web Client ID from Firebase Console.",
                    Toast.LENGTH_LONG
                ).show()
            }
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.btnGuestSignIn.setOnClickListener {
            Toast.makeText(this, "Logged in under Local Researcher Mode", Toast.LENGTH_SHORT).show()
            navigateToMain()
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnGoogleSignIn.isEnabled = false
                }
                is AuthResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleSignIn.isEnabled = true
                    Toast.makeText(this, "Sign-In Successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogleSignIn.isEnabled = true
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}