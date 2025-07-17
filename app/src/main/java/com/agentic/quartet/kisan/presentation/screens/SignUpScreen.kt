package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.res.stringResource
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.data.model.FarmerProfile
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.utils.ProfileManager
import com.agentic.quartet.kisan.utils.UserPreferences

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SignUpScreen(onSignUpComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val farmingSources = listOf(stringResource(R.string.terrace), stringResource(R.string.plotted_plants), stringResource(R.string.agriculture_land))
    var selectedFarmingSource by remember { mutableStateOf(farmingSources[0]) }
    var expanded by remember { mutableStateOf(false) }

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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
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
                    focusedPlaceholderColor = Color(0xFF66BB6A),
                    unfocusedPlaceholderColor = Color(0xFF81C784),
                    focusedTextColor = Color(0xFF4CAF50),
                    unfocusedTextColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedFarmingSource,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.source_of_farming)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF81C784),
                        cursorColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color(0xFF81C784),
                        focusedPlaceholderColor = Color(0xFF66BB6A),
                        unfocusedPlaceholderColor = Color(0xFF81C784),
                        focusedTextColor = Color(0xFF4CAF50),
                        unfocusedTextColor = Color(0xFF4CAF50)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(color = Color(0xFF4CAF50))
                ) {
                    farmingSources.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                selectedFarmingSource = option
                                expanded = false
                            }
                        )
                    }
                }
            }

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
            val error = stringResource(R.string.please_fill_all_fields_correctly)
            Button(
                onClick = {
                    if (name.isBlank() || phone.length != 10 || city.isBlank() || state.isBlank() || pinCode.length != 6 || password.length < 6 || password != confirmPassword) {
                        errorMessage = error
                        return@Button
                    }

                    errorMessage = null
                    isSubmitting = true
                    scope.launch {
                        delay(1000)
                        submitSuccess = true
                        delay(1000)
                        isSubmitting = false

                        val profile = FarmerProfile(
                            name = name,
                            phone = phone,
                            city = city,
                            state = state,
                            pinCode = pinCode,
                            farmingSource = selectedFarmingSource
                        )
                        ProfileManager.saveProfile(context, profile)
                        scope.launch {
                            UserPreferences(context).setSignedIn(true)
                            onSignUpComplete()
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
                    label = stringResource(R.string.submit_animation)
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
                        Text(stringResource(R.string.submit), color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.providing_correct_profile_helps_personalize_your_farming_guidance),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}