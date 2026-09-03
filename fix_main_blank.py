import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """            if (documentText.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Todavía no se ha generado el acta.\\nLlene los datos y presione Generar Acta.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                DocumentPreview(
                    documentText = documentText,
                    onDocumentTextChanged = { viewModel.updateDocumentText(it) },
                    onInsertImageClick = { pickImageLauncher.launch("image/*") },
                    imageToInsert = imageToInsert,
                    onImageInserted = { imageToInsert = null },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }"""

replacement = """            DocumentPreview(
                documentText = documentText,
                onDocumentTextChanged = { viewModel.updateDocumentText(it) },
                onInsertImageClick = { pickImageLauncher.launch("image/*") },
                imageToInsert = imageToInsert,
                onImageInserted = { imageToInsert = null },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
