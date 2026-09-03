package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DocumentPreview(
    documentText: String,
    onDocumentTextChanged: (String) -> Unit,
    onInsertImageClick: () -> Unit,
    imageToInsert: android.graphics.Bitmap?,
    onImageInserted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Adaptable al ancho del teléfono (como un contenedor blanco nativo)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        RichTextEditor(
            initialHtml = documentText,
            onHtmlChanged = onDocumentTextChanged,
            onInsertImageClick = onInsertImageClick,
            imageToInsert = imageToInsert,
            onImageInserted = onImageInserted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
