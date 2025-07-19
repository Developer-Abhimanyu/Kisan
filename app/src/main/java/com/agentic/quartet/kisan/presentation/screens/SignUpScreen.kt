package com.agentic.quartet.kisan.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.data.model.FarmerProfile
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.utils.ProfileManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SignUpScreen(onSignUpComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.create_your_farmer_rofile),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            fun isValid(): Boolean {
                return name.isNotBlank() && phone.length == 10 && city.isNotBlank() &&
                        state.isNotBlank() && pinCode.length == 6 &&
                        password.length >= 6 && password == confirmPassword
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(stringResource(R.string.city)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text(stringResource(R.string.state)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )
            OutlinedTextField(
                value = pinCode,
                onValueChange = { pinCode = it },
                label = { Text(stringResource(R.string.pincode)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784),
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val s = stringResource(R.string.please_fill_all_fields_correctly)

            Button(
                onClick = {
                    if (!isValid()) {
                        errorMessage = s
                        return@Button
                    }

                    errorMessage = null
                    isSubmitting = true

                    scope.launch {
                        try {
                            val result = auth.createUserWithEmailAndPassword(
                                phone.plus("@kisan.com"),
                                password
                            ).await()
                            val userId = result.user?.uid ?: throw Exception("User ID not found")

                            val profile = FarmerProfile(
                                name = name,
                                phone = phone,
                                city = city,
                                state = state,
                                pinCode = pinCode,
                                farmingSource = ""
                            )

                            val profileMap = mapOf(
                                "name" to name,
                                "phone" to phone,
                                "city" to city,
                                "state" to state,
                                "pinCode" to pinCode,
                                "farmingSource" to ""
                            )
                            db.collection("farmers").document(userId).set(profileMap).await()
                            submitSuccess = true
                            Toast.makeText(context, "Signup successful", Toast.LENGTH_SHORT).show()
                            delay(1000)
                            UserPreferences(context).setSignedIn(true)
                            ProfileManager.saveProfile(context, profile)
                            onSignUpComplete()

                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Signup failed"
                            isSubmitting = false
                            errorMessage = "Failed to save profile: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (submitSuccess) Color(0xFF388E3C) else Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(50)
            ) {
                AnimatedContent(
                    targetState = submitSuccess,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    },
                    label = "Submit Button Animation"
                ) { success ->
                    if (success) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.success),
                            tint = Color.White
                        )
                    } else if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            stringResource(R.string.submit),
                            color = Color.White,
                            fontSize = 18.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.providing_correct_profile_helps_personalize_your_farming_guidance),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
            )
        }
    }
}