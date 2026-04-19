package com.example.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _totalSpots = MutableStateFlow(0)
    val totalSpots: StateFlow<Int> = _totalSpots.asStateFlow()

    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser

            if (it.currentUser != null) {
                loadTotalSpots(it.currentUser!!.uid)
            } else {
                _totalSpots.value = 0
            }
        }
    }

    fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _totalSpots.value = 0
    }

    private fun loadTotalSpots(uid: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .document(uid)
                    .collection("spots")
                    .get()
                    .await()

                _totalSpots.value = snapshot.size()
            } catch (e: Exception) {
                _totalSpots.value = 0
            }
        }
    }
}