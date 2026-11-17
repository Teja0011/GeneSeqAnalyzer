package com.bioseq.geneseqanalyzer.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioseq.geneseqanalyzer.repository.AuthRepository
import com.bioseq.geneseqanalyzer.utils.AuthResult
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _authResult.value = AuthResult.Error("Email is required")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authResult.value = AuthResult.Error("Enter a valid email address")
            return
        }
        if (password.isBlank()) {
            _authResult.value = AuthResult.Error("Password is required")
            return
        }

        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            val result = repository.loginWithEmail(email, password)
            _authResult.value = result
        }
    }

    fun register(name: String, email: String, password: String, confirmPass: String) {
        if (name.isBlank()) {
            _authResult.value = AuthResult.Error("Full name is required")
            return
        }
        if (email.isBlank()) {
            _authResult.value = AuthResult.Error("Email is required")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authResult.value = AuthResult.Error("Enter a valid email address")
            return
        }
        if (password.isBlank()) {
            _authResult.value = AuthResult.Error("Password is required")
            return
        }
        if (password.length < 6) {
            _authResult.value = AuthResult.Error("Password must be at least 6 characters")
            return
        }
        if (password != confirmPass) {
            _authResult.value = AuthResult.Error("Passwords do not match")
            return
        }

        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            val result = repository.registerWithEmail(name, email, password)
            _authResult.value = result
        }
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            val result = repository.signInWithGoogle(credential)
            _authResult.value = result
        }
    }

    fun signOut() {
        repository.signOut()
    }
}