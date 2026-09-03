package com.example

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Html
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFD4AF37), // Amber/Gold details
                onPrimary = Color.Black,
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onBackground = Color.White,
                onSurface = Color.White
            )) {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                    MobileAppScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileAppScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val documentText by viewModel.documentText.collectAsState()
    val isConstancia by viewModel.isConstancia.collectAsState()
    val ocrRawText by viewModel.ocrRawText.collectAsState()
    val ocrFoundFields by viewModel.ocrFoundFields.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var isCameraActive by remember { mutableStateOf(false) }
    var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeVoiceField by remember { mutableStateOf("") }
    var showOcrOptions by remember { mutableStateOf(false) }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (spokenText.isNotBlank()) {
                val current = viewModel.uiState.value
                val up = when (activeVoiceField) {
                    "expediente" -> current.copy(expedienteNro = spokenText)
                    "imputado" -> current.copy(nombreImputado = spokenText)
                    "delito" -> current.copy(delito = spokenText)
                    "perjudicado" -> current.copy(perjudicado = spokenText)
                    "tipo" -> current.copy(tipoAudienciaReprogramada = spokenText)
                    "motivo" -> current.copy(motivoReprogramacion = spokenText)
                    "fecha" -> current.copy(nuevaFecha = spokenText)
                    "hora" -> current.copy(nuevaHora = spokenText)
                    "articulo" -> current.copy(articuloCpp = spokenText)
                    "juez" -> current.copy(nombreJuez = spokenText)
                    "secretario" -> current.copy(nombreSecretario = spokenText)
                    else -> current
                }
                viewModel.updateData(up)
            }
        }
    }

    fun startVoice(field: String) {
        activeVoiceField = field
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-HN")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Entrada por voz no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    fun processImage(bitmap: Bitmap) {
        // If there's already some data, ask if they want to overwrite
        val current = viewModel.uiState.value
        val hasData = current.expedienteNro.isNotBlank() || current.nombreImputado.isNotBlank() || current.delito.isNotBlank()
        
        if (hasData) {
            pendingBitmap = bitmap
            showOverwriteDialog = true
        } else {
            capturedImageBitmap = bitmap
            viewModel.analyzeImage(bitmap)
        }
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            val swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            processImage(swBitmap)
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            isCameraActive = true
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara. Use la galería.", Toast.LENGTH_LONG).show()
        }
    }

    if (isCameraActive) {
        CameraXScreen(
            onPhotoCaptured = { file ->
                isCameraActive = false
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                processImage(swBitmap)
            },
            onCancel = { isCameraActive = false }
        )
        return // Render only camera
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text("¿Reemplazar datos actuales?") },
            text = { Text("¿Desea reemplazar los datos actuales con la información detectada de esta nueva imagen?") },
            confirmButton = {
                TextButton(onClick = { 
                    showOverwriteDialog = false
                    capturedImageBitmap = pendingBitmap
                    viewModel.analyzeImage(pendingBitmap!!)
                    pendingBitmap = null
                }) { Text("REEMPLAZAR") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showOverwriteDialog = false
                    pendingBitmap = null
                }) { Text("CONSERVAR") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("JUSTICIA RÁPIDA HN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Asistente Judicial", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // CARD 1: OCR AUTORRELLENADO
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AUTORRELLENADO POR IMAGEN", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showOcrOptions = true },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("DIGITALIZAR ACTA", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
                    }
                    if (isLoading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Procesando imagen localmente...", modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.bodySmall)
                    } else if (capturedImageBitmap != null) {
                        Image(
                            bitmap = capturedImageBitmap!!.asImageBitmap(),
                            contentDescription = "Documento escaneado",
                            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)
                        )
                    }
                }
            }

            if (showOcrOptions) {
                AlertDialog(
                    onDismissRequest = { showOcrOptions = false },
                    title = { Text("Digitalizar Documento") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    showOcrOptions = false
                                    requestPermissionLauncher.launch(Manifest.permission.CAMERA) 
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) { Text("TOMAR FOTO") }
                            
                            OutlinedButton(
                                onClick = { 
                                    showOcrOptions = false
                                    pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) { Text("SELECCIONAR IMAGEN") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showOcrOptions = false }) { Text("CANCELAR") }
                    }
                )
            }

            // TEXTO DETECTADO
            if (ocrRawText.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TEXTO DETECTADO", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Si hay algún error tipográfico en el OCR (ej. un apellido mal escrito), corrígelo aquí y presiona Actualizar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ocrRawText,
                            onValueChange = { viewModel.updateOcrRawText(it) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 200.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.reProcessOcrText() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ACTUALIZAR DATOS")
                        }

                        // Resumen
                        if (ocrFoundFields.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text("Resumen de extracción:", fontWeight = FontWeight.Bold)
                            ocrFoundFields.forEach { (field, found) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (found) Icons.Default.Check else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (found) Color.Green else Color.Yellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = if (found) "$field encontrado" else "$field no encontrado", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // CARD 3: FORMULARIO
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("DATOS PARA REPROGRAMACIÓN", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isConstancia, onClick = { viewModel.setConstancia(true) })
                            Text("CON CONSTANCIA", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !isConstancia, onClick = { viewModel.setConstancia(false) })
                            Text("DE OFICIO", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    @Composable
                    fun VoiceTextField(label: String, value: String, onValueChange: (String) -> Unit, voiceTag: String, isMultiline: Boolean = false, isReadOnly: Boolean = false, onClick: (() -> Unit)? = null) {
                        OutlinedTextField(
                            value = value,
                            textStyle = LocalTextStyle.current.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            onValueChange = onValueChange,
                            label = { Text(label) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp)
                                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
                            readOnly = isReadOnly,
                            enabled = onClick == null, 
                            minLines = if (isMultiline) 3 else 1,
                            maxLines = if (isMultiline) 5 else 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            trailingIcon = {
                                if (voiceTag.isNotEmpty()) {
                                    IconButton(onClick = { startVoice(voiceTag) }) {
                                        Icon(Icons.Default.Mic, contentDescription = "Dictar $label", tint = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (onClick != null) {
                                    IconButton(onClick = onClick) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = "Seleccionar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }

                    VoiceTextField("N° DE EXPEDIENTE", uiState.expedienteNro, { viewModel.updateData(uiState.copy(expedienteNro = it)) }, "expediente")
                    VoiceTextField("NOMBRE DEL IMPUTADO", uiState.nombreImputado, { viewModel.updateData(uiState.copy(nombreImputado = it)) }, "imputado")
                    VoiceTextField("DELITO", uiState.delito, { viewModel.updateData(uiState.copy(delito = it)) }, "delito")
                    VoiceTextField("PERJUDICADO", uiState.perjudicado, { viewModel.updateData(uiState.copy(perjudicado = it)) }, "perjudicado")
                    VoiceTextField("TIPO DE AUDIENCIA", uiState.tipoAudienciaReprogramada, { viewModel.updateData(uiState.copy(tipoAudienciaReprogramada = it)) }, "tipo")
                    VoiceTextField("MOTIVO DE REPROGRAMACIÓN", uiState.motivoReprogramacion, { viewModel.updateData(uiState.copy(motivoReprogramacion = it)) }, "motivo", isMultiline = true)
                    
                                                            
                    Box(modifier = Modifier.fillMaxWidth().clickable {
                        val cal = java.util.Calendar.getInstance()
                        android.app.DatePickerDialog(context, { _, year, month, dayOfMonth ->
                            val dateStr = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                            viewModel.updateData(uiState.copy(nuevaFecha = dateStr))
                        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
                    }) {
                        VoiceTextField("NUEVA FECHA (DD/MM/YYYY)", uiState.nuevaFecha, {}, "", isReadOnly = true, onClick = {
                            val cal = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                val dateStr = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                                viewModel.updateData(uiState.copy(nuevaFecha = dateStr))
                            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
                        })
                    }
                    
                    // Selector nativo de hora
                    Box(modifier = Modifier.fillMaxWidth().clickable {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(context, { _, hourOfDay, minute ->
                            val timeStr = String.format("%02d:%02d", hourOfDay, minute)
                            viewModel.updateData(uiState.copy(nuevaHora = timeStr))
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                    }) {
                        VoiceTextField("NUEVA HORA (HH:MM)", uiState.nuevaHora, {}, "", isReadOnly = true, onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(context, { _, hourOfDay, minute ->
                                val timeStr = String.format("%02d:%02d", hourOfDay, minute)
                                viewModel.updateData(uiState.copy(nuevaHora = timeStr))
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                        })
                    }

                    VoiceTextField("ARTÍCULO CPP", uiState.articuloCpp, { viewModel.updateData(uiState.copy(articuloCpp = it)) }, "articulo")
                    VoiceTextField("NOMBRE DEL JUEZ", uiState.nombreJuez, { viewModel.updateData(uiState.copy(nombreJuez = it)) }, "juez")
                    VoiceTextField("NOMBRE DEL SECRETARIO", uiState.nombreSecretario, { viewModel.updateData(uiState.copy(nombreSecretario = it)) }, "secretario")

                    Button(
                        onClick = { viewModel.generateDocument() },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("GENERAR DOCUMENTO", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.clearData() }, modifier = Modifier.weight(1f).height(48.dp)) {
                            Text("Limpiar", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                        OutlinedButton(onClick = { viewModel.recoverData() }, modifier = Modifier.weight(1f).height(48.dp)) {
                            Text("Recuperar", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                    }
                }
            }

            // CARD: LIBRO DE SECRETARÍA (Calendario)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LIBRO DE SECRETARÍA", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    CalendarView { date ->
                        val formattedDate = String.format("%02d/%02d/%04d", date.dayOfMonth, date.monthValue, date.year)
                        viewModel.updateData(viewModel.uiState.value.copy(nuevaFecha = formattedDate))
                    }
                }
            }

            // CARD 4: VISTA PREVIA Y ACCIONES (MINI WORD)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("VISTA PREVIA DEL DOCUMENTO", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    var imageToInsert by remember { mutableStateOf<Bitmap?>(null) }
                    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        if (uri != null) {
                            try {
                                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                }
                                imageToInsert = bitmap
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // A constrained height preview for mobile
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White)) {
                        DocumentPreview(
                            documentText = documentText,
                            onDocumentTextChanged = { viewModel.updateDocumentText(it) },
                            onInsertImageClick = { pickImageLauncher.launch("image/*") },
                            imageToInsert = imageToInsert,
                            onImageInserted = { imageToInsert = null },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Botones de acción grandes y móviles
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newHtmlText("Documento", Html.fromHtml(documentText, Html.FROM_HTML_MODE_LEGACY).toString(), documentText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Documento copiado correctamente", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = documentText.isNotBlank()
                        ) { Text("COPIAR") }

                        Button(
                            onClick = { 
                                val webView = WebView(context)
                                webView.loadDataWithBaseURL(null, documentText, "text/html", "UTF-8", null)
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                                val printAdapter = webView.createPrintDocumentAdapter("Acta_Reprogramacion")
                                printManager.print("Acta", printAdapter, android.print.PrintAttributes.Builder().build())
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = documentText.isNotBlank()
                        ) { Text("IMPRIMIR") }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            try {
                                val fileName = "Acta_Reprogramacion_${System.currentTimeMillis()}.doc"
                                val contentValues = android.content.ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                    put(MediaStore.MediaColumns.MIME_TYPE, "application/msword")
                                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                                }
                                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                                if (uri != null) {
                                    context.contentResolver.openOutputStream(uri)?.use { 
                                        // A VERY basic doc wrapper. For robust MSOffice format we output HTML wrapped with DOC extension
                                        val docBytes = """
                                            <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
                                            <head><meta charset="utf-8"></head><body>
                                            $documentText
                                            </body></html>
                                        """.trimIndent().toByteArray()
                                        it.write(docBytes)
                                    }
                                    Toast.makeText(context, "Exportado a Documentos", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = documentText.isNotBlank()
                    ) { Text("EXPORTAR A WORD", color = MaterialTheme.colorScheme.onSecondary) }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CalendarView(onDateSelected: (LocalDate) -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior", tint = MaterialTheme.colorScheme.primary)
            }
            
            val mesNombre = currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
            Text(
                text = "$mesNombre ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Mes siguiente", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            for (day in daysOfWeek) {
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        
        var dayCounter = 1
        var currentWeek = 0
        
        Column(modifier = Modifier.fillMaxWidth()) {
            while (dayCounter <= daysInMonth) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    for (i in 1..7) {
                        if (currentWeek == 0 && i < firstDayOfWeek) {
                            Box(modifier = Modifier.weight(1f))
                        } else if (dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val date = currentMonth.atDay(dayCounter)
                            val isSelected = selectedDate == date
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { 
                                        selectedDate = date
                                        onDateSelected(date)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            dayCounter++
                        }
                    }
                }
                currentWeek++
            }
        }
    }
}
