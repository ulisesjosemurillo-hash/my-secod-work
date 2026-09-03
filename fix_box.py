import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# I will find the whole RightPanel and replace it cleanly.
# I'll just write a script to extract RightPanel block and replace it correctly.

def get_right_panel():
    return """
@Composable
fun RightPanel(documentText: String, viewModel: MainViewModel, context: android.content.Context, modifier: Modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("VISTA PREVIA DEL ACTA", style = MaterialTheme.typography.titleMedium)
            
            var imageToInsert by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            val pickImageLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri: android.net.Uri? ->
                if (uri != null) {
                    try {
                        val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                        } else {
                            @Suppress("DEPRECATION")
                            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                        imageToInsert = bitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
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
            
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newHtmlText("Documento", android.text.Html.fromHtml(documentText, android.text.Html.FROM_HTML_MODE_LEGACY).toString(), documentText)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    enabled = documentText.isNotBlank()
                ) { Text("Copiar") }
                
                Button(
                    onClick = { 
                        try {
                            val fileName = "Acta_Reprogramacion_${System.currentTimeMillis()}.doc"
                            val contentValues = android.content.ContentValues().apply {
                                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/msword")
                                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS)
                            }
                            val uri = context.contentResolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
                            if (uri != null) {
                                context.contentResolver.openOutputStream(uri)?.use { 
                                    it.write(documentText.toByteArray())
                                }
                                android.widget.Toast.makeText(context, "Exportado a Documentos", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error al exportar", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = documentText.isNotBlank()
                ) { Text("Exportar Word") }
                
                Button(
                    onClick = { 
                        val webView = android.webkit.WebView(context)
                        webView.loadDataWithBaseURL(null, documentText, "text/html", "UTF-8", null)
                        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                        val printAdapter = webView.createPrintDocumentAdapter("Acta_Reprogramacion")
                        printManager.print("Acta", printAdapter, android.print.PrintAttributes.Builder().build())
                    },
                    enabled = documentText.isNotBlank()
                ) { Text("Imprimir") }
                
                Button(
                    onClick = { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/html"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Acta de Reprogramación")
                            putExtra(android.content.Intent.EXTRA_TEXT, android.text.Html.fromHtml(documentText, android.text.Html.FROM_HTML_MODE_LEGACY))
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Enviar correo"))
                    },
                    enabled = documentText.isNotBlank()
                ) { Text("Correo") }
            }
        }
    }
}
"""

content = re.sub(r'@Composable\s*fun RightPanel\(.*$', get_right_panel(), content, flags=re.DOTALL)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
