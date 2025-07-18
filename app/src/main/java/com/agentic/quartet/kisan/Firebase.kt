package com.agentic.quartet.kisan

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

object FirebaseManager {
    val auth = Firebase.auth
    val firestore = Firebase.firestore
}