package com.bioseq.geneseqanalyzer.repository

import com.bioseq.geneseqanalyzer.utils.AuthResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(parseErrorMessage(e))
        }
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            result.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(parseErrorMessage(e))
        }
    }

    suspend fun signInWithGoogle(credential: AuthCredential): AuthResult {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(parseErrorMessage(e))
        }
    }

    private fun parseErrorMessage(e: Exception): String {
        val msg = e.localizedMessage ?: "Authentication failed"
        return if (msg.contains("API key not valid", ignoreCase = true) || msg.contains("API_KEY_INVALID", ignoreCase = true)) {
            "Firebase Configuration Required: Replace app/google-services.json with your actual google-services.json downloaded from Firebase Console."
        } else {
            msg
        }
    }
}