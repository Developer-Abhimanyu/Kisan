package com.agentic.quartet.kisan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp


@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8E1),
                        Color(0xFF8BC34A)
                    )
                )
            )
    ) {
        content()
    }
}

/*@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8E1), // Light soil/beige
                        Color(0xFFF1F8E9), // Slightly greenish hint (crop base)
                        Color(0xFF8BC34A), // Crop green
                        Color(0xFF4CAF50), // Darker leaves
                        Color(0xFF81D4FA)  // Light blue sky at top
                    )
                )
            )
    ) {
        content()
    }
}*/

/*
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background gradient layer (field to green)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F8E9), // Soft green field
                            Color(0xFF8BC34A)  // Crop green
                        )
                    )
                )
        )

        // Decorative curved shape (hills/fields on top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Only partial height to make it like a hill/field
                .graphicsLayer {
                    shape = RoundedCornerShape(bottomStart = 160.dp, bottomEnd = 160.dp)
                    clip = true
                }
                .background(Color(0xFFFFF8E1)) // Light soil or sky layer on top
        )

        // ✅ Actual screen content (always on top)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}*/
