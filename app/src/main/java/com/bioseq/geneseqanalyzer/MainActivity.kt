package com.bioseq.geneseqanalyzer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bioseq.geneseqanalyzer.databinding.ActivityMainBinding
import com.bioseq.geneseqanalyzer.repository.AuthRepository
import com.bioseq.geneseqanalyzer.ui.analysis.AnalysisActivity
import com.bioseq.geneseqanalyzer.ui.auth.LoginActivity
import com.bioseq.geneseqanalyzer.ui.history.HistoryActivity
import com.bioseq.geneseqanalyzer.ui.scanner.ScannerActivity
import com.bioseq.geneseqanalyzer.ui.sequence.ManualEntryActivity
import com.bioseq.geneseqanalyzer.ui.settings.SettingsActivity
import com.bioseq.geneseqanalyzer.utils.ThemePreferences

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePreferences.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWelcomeHeader()
        setupListeners()
        handleIncomingAccessionIntent()
    }

    private fun setupWelcomeHeader() {
        val user = authRepository.getCurrentUser()
        val userName = if (!user?.displayName.isNullOrBlank()) user?.displayName else user?.email?.substringBefore("@") ?: "Researcher"
        binding.tvWelcomeUser.text = getString(R.string.welcome_user, userName)
    }

    private fun setupListeners() {
        // Logout Action
        binding.btnLogout.setOnClickListener {
            authRepository.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Quick NCBI Fetch Action
        binding.btnQuickFetch.setOnClickListener {
            val accessionId = binding.etQuickAccession.text.toString().trim()
            if (accessionId.isEmpty()) {
                Toast.makeText(this, "Please enter an accession ID", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AnalysisActivity::class.java).apply {
                    putExtra("EXTRA_ACCESSION_ID", accessionId)
                }
                startActivity(intent)
            }
        }

        // Feature Card 1: OCR Scanner
        binding.cardScanAccession.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        // Feature Card 2: Manual Accession Entry
        binding.cardEnterAccession.setOnClickListener {
            startActivity(Intent(this, ManualEntryActivity::class.java))
        }

        // Feature Card 3: Recent Analyses
        binding.cardRecentAnalyses.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Feature Card 4: Saved Sequences
        binding.cardSavedSequences.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Feature Card 5: Settings
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun handleIncomingAccessionIntent() {
        val scannedAccession = intent.getStringExtra("EXTRA_ACCESSION_ID")
        if (!scannedAccession.isNullOrBlank()) {
            binding.etQuickAccession.setText(scannedAccession)
            val intent = Intent(this, AnalysisActivity::class.java).apply {
                putExtra("EXTRA_ACCESSION_ID", scannedAccession)
            }
            startActivity(intent)
        }
    }
}