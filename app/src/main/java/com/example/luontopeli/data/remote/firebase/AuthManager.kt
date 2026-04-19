package com.example.luontopeli.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication -hallinta.
 *
 * Käyttää anonyymiä kirjautumista (signInAnonymously), joka antaa
 * käyttäjälle uniikin UID:n ilman rekisteröitymistä. UID säilyy
 * sovelluksen uudelleenkäynnistysten välillä.
 */
class AuthManager {

    /** Firebase Auth -instanssi (singleton) */
    private val auth = FirebaseAuth.getInstance()

    /** Nykyisen käyttäjän UID tai "anonymous" jos ei kirjautunut */
    val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    /** Onko käyttäjä kirjautunut sisään */
    val isSignedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Kirjautuu sisään anonyymisti.
     * Firebase luo uniikin UID:n, joka säilyy kunnes käyttäjä
     * kirjautuu ulos tai sovelluksen data tyhjennetään.
     *
     * @return Result.success(uid) tai Result.failure(exception)
     */
    suspend fun signInAnonymously(): Result<String> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user?.uid ?: "unknown")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Kirjautuu ulos ja poistaa anonyymin session. */
    fun signOut() {
        auth.signOut()
    }
}