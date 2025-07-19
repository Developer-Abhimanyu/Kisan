package com.agentic.quartet.kisan.presentation.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import java.io.File
import java.io.FileOutputStream

@Composable
fun CropDetailScreen(
    monthIndex: Int,
    onBack: () -> Unit
) {
    val months = listOf(
        stringResource(R.string.january),
        stringResource(R.string.february),
        stringResource(R.string.march),
        stringResource(R.string.april),
        stringResource(R.string.may),
        stringResource(R.string.june),
        stringResource(R.string.july),
        stringResource(R.string.august),
        stringResource(R.string.september),
        stringResource(R.string.october),
        stringResource(R.string.november),
        stringResource(R.string.december)
    )
    val context = LocalContext.current
    val view = LocalView.current
    val month = months[monthIndex]

    /* val cropImages = listOf(
         R.drawable.crop_january, R.drawable.crop_february, R.drawable.crop_march,
         R.drawable.crop_april, R.drawable.crop_may, R.drawable.crop_june,
         R.drawable.crop_july, R.drawable.crop_august, R.drawable.crop_september,
         R.drawable.crop_october, R.drawable.crop_november, R.drawable.crop_december
     )*/

    val cropImages = listOf(
        R.drawable.ic_leaf, R.drawable.ic_leaf, R.drawable.ic_leaf,
        R.drawable.ic_leaf, R.drawable.ic_leaf, R.drawable.ic_leaf,
        R.drawable.ic_leaf, R.drawable.ic_leaf, R.drawable.ic_leaf,
        R.drawable.ic_leaf, R.drawable.ic_leaf, R.drawable.ic_leaf
    )

    /* val activities = listOf(
         Triple("Sowing", "Maize, Bajra, Groundnut", R.drawable.ic_leaf),
         Triple("Irrigation", "Twice a week", R.drawable.ic_water),
         Triple("Fertilizer", "NPK 50 kg/acre", R.drawable.ic_fertilizer),
         Triple("Ploughing", "Before 10th", R.drawable.ic_tractor)
     )*/

    //TODO not added to strings as of now
    val activities = listOf(
        Triple("Sowing", "Maize, Bajra, Groundnut", R.drawable.ic_leaf),
        Triple("Irrigation", "Twice a week", R.drawable.ic_leaf),
        Triple("Fertilizer", "NPK 50 kg/acre", R.drawable.ic_leaf),
        Triple("Ploughing", "Before 10th", R.drawable.ic_leaf)
    )

    //TODO not added to strings as of now
    val tips = listOf(
        "Use compost or green manure to enrich soil.",
        "Prefer early morning irrigation to reduce evaporation.",
        "Watch out for aphids and fungal infections."
    )

    var tipsExpanded by remember { mutableStateOf(false) }

    // Capture size of screen content for screenshot
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .onGloballyPositioned { screenSize = it.size }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = cropImages[monthIndex]),
                    contentDescription = "$month ${stringResource(R.string.crop_image)}",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$month ${stringResource(R.string.crop_activities)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                ),
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            activities.forEach { (title, desc, icon) ->
                ActivityCard(title, desc, painterResource(id = icon))
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable tips
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { tipsExpanded = !tipsExpanded }
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.smart_farming_tips),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        ),
                        fontSize = 18.sp,
                    )

                    if (tipsExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        tips.forEach {
                            Text(
                                "• $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32),
                                fontSize = 18.sp,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Download and Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val shareVia = stringResource(R.string.share_via)
                Button(
                    onClick = {
                        val bitmap = captureViewBitmap(view)
                        bitmap?.let {
                            val file = saveBitmapAsPdf(context, it, "${month}_crop_guide")
                            sharePdf(context, file, shareVia)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.share),  fontSize = 14.sp,)
                }
                val std = stringResource(R.string.saved_to_documents)
                Button(
                    onClick = {
                        val bitmap = captureViewBitmap(view)
                        bitmap?.let {
                            saveBitmapAsPdf(context, it, "${month}_crop_guide")
                            Toast.makeText(context, std, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.download_pdf),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.download_pdf),  fontSize = 14.sp,)
                }
            }
        }
    }
}

// Reusable composable for activities
@Composable
fun ActivityCard(title: String, desc: String, icon: Painter) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32),  fontSize = 18.sp,)
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp,
                )
            }
        }
    }
}

// Capture the whole screen (or Composable view)
fun captureViewBitmap(view: View): Bitmap? {
    return try {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        bitmap
    } catch (e: Exception) {
        null
    }
}

// Save bitmap as PDF file
fun saveBitmapAsPdf(context: Context, bitmap: Bitmap, filename: String): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$filename.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()
    return file
}

// Share the PDF using FileProvider
fun sharePdf(context: Context, file: File, shareVia: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, shareVia))
}