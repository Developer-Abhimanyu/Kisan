package com.agentic.quartet.kisan.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.agentic.quartet.kisan.presentation.AppBackground
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                FirebaseAuth.getInstance().signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSignedIn()
                        } else {
                            Toast.makeText(context, "Auth failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }) {
                Text("Sign In Anonymously")
            }
        }
    }
}