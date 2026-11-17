package com.bioseq.geneseqanalyzer.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bioseq.geneseqanalyzer.R
import com.bioseq.geneseqanalyzer.databinding.ActivityScannerBinding
import com.bioseq.geneseqanalyzer.ui.analysis.AnalysisActivity
import com.bioseq.geneseqanalyzer.utils.AccessionExtractor
import com.google.android.material.chip.Chip
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            binding.imgPreview.setImageBitmap(bitmap)
            binding.layoutPlaceholder.visibility = View.GONE
            processImageForText(InputImage.fromBitmap(bitmap, 0))
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                binding.imgPreview.setImageURI(uri)
                binding.layoutPlaceholder.visibility = View.GONE
                val inputImage = InputImage.fromFilePath(this, uri)
                processImageForText(inputImage)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(this, getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnConfirmFetch.setOnClickListener {
            val confirmedAccession = binding.etConfirmedAccession.text.toString().trim()
            if (confirmedAccession.isEmpty()) {
                Toast.makeText(this, "Please enter or select a valid accession number", Toast.LENGTH_SHORT).show()
            } else {
                navigateToSequenceFetch(confirmedAccession)
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun processImageForText(image: InputImage) {
        binding.progressBarScanning.visibility = View.VISIBLE
        binding.chipGroupAccessions.removeAllViews()

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                binding.progressBarScanning.visibility = View.GONE
                val fullText = visionText.text
                val extractedAccessions = AccessionExtractor.extractAccessions(fullText)

                if (extractedAccessions.isNotEmpty()) {
                    displayCandidateChips(extractedAccessions)
                    binding.etConfirmedAccession.setText(extractedAccessions.first())
                    Toast.makeText(this, "Detected ${extractedAccessions.size} accession ID(s)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.no_accession_found), Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBarScanning.visibility = View.GONE
                Toast.makeText(this, "OCR Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayCandidateChips(accessions: List<String>) {
        binding.chipGroupAccessions.removeAllViews()
        for (acc in accessions) {
            val chip = Chip(this).apply {
                text = acc
                isCheckable = true
                setOnClickListener {
                    binding.etConfirmedAccession.setText(acc)
                }
            }
            binding.chipGroupAccessions.addView(chip)
        }
    }

    private fun navigateToSequenceFetch(accessionId: String) {
        val intent = Intent(this, AnalysisActivity::class.java).apply {
            putExtra("EXTRA_ACCESSION_ID", accessionId)
        }
        startActivity(intent)
        finish()
    }
}