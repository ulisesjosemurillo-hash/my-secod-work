import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace RightPanel header
content = content.replace('Text("Vista Previa (Editor Tipo Word)", style = MaterialTheme.typography.titleMedium)', 'Text("VISTA PREVIA DEL ACTA", style = MaterialTheme.typography.titleMedium)')

# Replace the box with the empty state logic
box_pattern = r'Box\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.weight\(1f\)\s*\.background\(Color\.White\)\s*\.padding\(1\.dp\)\s*\)\s*\{\s*RichTextEditor\(.*?\)\s*\}'

replacement_box = """
            if (documentText.isBlank()) {
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .padding(1.dp)
                ) {
                    RichTextEditor(
                        initialHtml = documentText,
                        onHtmlChanged = { viewModel.updateDocumentText(it) },
                        onInsertImageClick = { pickImageLauncher.launch("image/*") },
                        imageToInsert = imageToInsert,
                        onImageInserted = { imageToInsert = null },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
"""

content = re.sub(box_pattern, replacement_box.strip(), content, flags=re.DOTALL)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
