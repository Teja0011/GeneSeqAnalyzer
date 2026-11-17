package com.bioseq.geneseqanalyzer.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bioseq.geneseqanalyzer.databinding.ActivitySettingsBinding
import com.bioseq.geneseqanalyzer.repository.AuthRepository
import com.bioseq.geneseqanalyzer.ui.auth.LoginActivity
import com.bioseq.geneseqanalyzer.utils.ThemePreferences

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeRadioGroup()
        setupUserInfo()
        setupListeners()
    }

    private fun setupThemeRadioGroup() {
        val currentMode = ThemePreferences.getSavedThemeMode(this)
        when (currentMode) {
            ThemePreferences.MODE_LIGHT -> binding.rbThemeLight.isChecked = true
            ThemePreferences.MODE_DARK -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                binding.rbThemeLight.id -> ThemePreferences.MODE_LIGHT
                binding.rbThemeDark.id -> ThemePreferences.MODE_DARK
                else -> ThemePreferences.MODE_SYSTEM
            }
            ThemePreferences.saveThemeMode(this, mode)
        }
    }

    private fun setupUserInfo() {
        val user = authRepository.getCurrentUser()
        val email = user?.email ?: "Guest User"
        binding.tvUserEmail.text = "Logged in as: $email"
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            authRepository.signOut()
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}