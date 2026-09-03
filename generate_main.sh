#!/bin/bash
cat << 'KOTLIN_EOF' > /app/applet/app/src/main/java/com/example/MainActivity.kt
package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.window.Dialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val documentText by viewModel.documentText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAnalyzingBook by viewModel.isAnalyzingBook.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isConstancia by viewModel.isConstancia.collectAsState()
    
    val context = LocalContext.current
    var isCalendarExpanded by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    val eventsByDate = remember(calendarEvents) { calendarEvents.groupBy { it.date } }
    
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var showCalendarDialog by remember { mutableStateOf(false) }
    var activeVoiceField by remember { mutableStateOf("") }
    
    // Toast error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotEmpty()) {
                val current = viewModel.uiState.value
                when (activeVoiceField) {
                    "expediente" -> viewModel.updateData(current.copy(expedienteNro = text))
                    "imputado" -> viewModel.updateData(current.copy(nombreImputado = text))
                    "delito" -> viewModel.updateData(current.copy(delito = text))
                    "perjudicado" -> viewModel.updateData(current.copy(perjudicado = text))
                    "motivo" -> viewModel.updateData(current.copy(tipoAudienciaReprogramada = text))
                    "hora" -> viewModel.updateData(current.copy(nuevaHora = text))
                }
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

    val takePhotoHighRes = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                capturedImageBitmap = swBitmap
                viewModel.analyzeImage(swBitmap)
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val uri = createImageFileUri(context)
            cameraUri = uri
            takePhotoHighRes.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    
    val takeBookPhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val swBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            viewModel.analyzeCalendarBookImage(swBitmap)
        }
    }
    val requestBookPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) { takeBookPhoto.launch(null) }
    }

    if (showCalendarDialog) {
        Dialog(onDismissRequest = { showCalendarDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seleccionar Nueva Fecha", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    CalendarMonthView(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        eventsByDate = eventsByDate,
                        onDateSelected = { 
                            selectedDate = it
                            val dateString = String.format("%04d-%02d-%02d", it.year, it.monthValue, it.dayOfMonth)
                            viewModel.updateData(viewModel.uiState.value.copy(nuevaFecha = dateString))
                            showCalendarDialog = false
                        },
                        onMonthChange = { currentMonth = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showCalendarDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Justicia Rápida HN - Asistente Judicial", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val isWide = maxWidth > 600.dp
            
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        LeftPanel(
                            uiState = uiState, viewModel = viewModel, isLoading = isLoading, 
                            capturedImageBitmap = capturedImageBitmap, requestPermissionLauncher = requestPermissionLauncher,
                            startVoice = ::startVoice, onOpenCalendar = { showCalendarDialog = true },
                            isAnalyzingBook = isAnalyzingBook, currentMonth = currentMonth, selectedDate = selectedDate, eventsByDate = eventsByDate,
                            onDateSelected = { selectedDate = it }, onMonthChange = { currentMonth = it }, showAddEventDialog = { showAddEventDialog = true },
                            requestBookPermissionLauncher = requestBookPermissionLauncher, takeBookPhoto = takeBookPhoto, context = context
                        )
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        RightPanel(documentText = documentText, viewModel = viewModel, context = context)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LeftPanel(
                        uiState = uiState, viewModel = viewModel, isLoading = isLoading, 
                        capturedImageBitmap = capturedImageBitmap, requestPermissionLauncher = requestPermissionLauncher,
                        startVoice = ::startVoice, onOpenCalendar = { showCalendarDialog = true },
                        isAnalyzingBook = isAnalyzingBook, currentMonth = currentMonth, selectedDate = selectedDate, eventsByDate = eventsByDate,
                        onDateSelected = { selectedDate = it }, onMonthChange = { currentMonth = it }, showAddEventDialog = { showAddEventDialog = true },
                        requestBookPermissionLauncher = requestBookPermissionLauncher, takeBookPhoto = takeBookPhoto, context = context
                    )
                    RightPanel(documentText = documentText, viewModel = viewModel, context = context)
                }
            }
        }
        
        // Mantener dialogos existentes
        if (showAddEventDialog) {
            var newTime by remember { mutableStateOf("") }
            var newExpediente by remember { mutableStateOf("") }
            var newDescription by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showAddEventDialog = false },
                title = { Text("Añadir Audiencia") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newTime, onValueChange = { newTime = it }, label = { Text("Hora (HH:MM)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newExpediente, onValueChange = { newExpediente = it }, label = { Text("Número de Expediente") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Detalle / Delito") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDate?.let { date ->
                                val dateString = String.format("%04d-%02d-%02d", date.year, date.monthValue, date.dayOfMonth)
                                viewModel.addManualEvent(CalendarEvent(id = java.util.UUID.randomUUID().toString(), date = dateString, time = newTime, description = newDescription, expediente = newExpediente))
                            }
                            showAddEventDialog = false
                        }
                    ) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { showAddEventDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun LeftPanel(
    uiState: ReprogramacionData, viewModel: MainViewModel, isLoading: Boolean, capturedImageBitmap: Bitmap?,
    requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>, startVoice: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    // Calendar parameters
    isAnalyzingBook: Boolean, currentMonth: YearMonth, selectedDate: LocalDate?, eventsByDate: Map<String, List<CalendarEvent>>,
    onDateSelected: (LocalDate) -> Unit, onMonthChange: (YearMonth) -> Unit, showAddEventDialog: () -> Unit,
    requestBookPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>, takeBookPhoto: androidx.activity.result.ActivityResultLauncher<Void?>,
    context: android.content.Context
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // 1. Calendario Existente (Libro de Secretaría)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Libro de Secretaría", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                takeBookPhoto.launch(null)
                            } else {
                                requestBookPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        enabled = !isAnalyzingBook,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Digitalizar Libro")
                    }
                }
                if (isAnalyzingBook) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onPrimaryContainer)
                } else {
                    CalendarMonthView(
                        currentMonth = currentMonth, selectedDate = selectedDate, eventsByDate = eventsByDate,
                        onDateSelected = onDateSelected, onMonthChange = onMonthChange
                    )
                }
            }
        }

        // 2. Autorrellenado de Acta por Imagen
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Autorrellenado por Imagen", style = MaterialTheme.typography.titleMedium)
                
                Button(
                    onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                    Spacer(Modifier.width(8.dp))
                    Text("Digitalizar Acta")
                }
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Procesando documento...", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (capturedImageBitmap != null) {
                    Image(
                        bitmap = capturedImageBitmap.asImageBitmap(),
                        contentDescription = "Acta Original",
                        modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp)
                    )
                    Text("Revisa los datos extraídos abajo. La imagen original se conserva arriba.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // 3. Formulario
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Datos para Reprogramación", style = MaterialTheme.typography.titleMedium)
                
                @Composable
                fun FieldWithMic(label: String, value: String, onValueChange: (String) -> Unit, voiceTag: String) {
                    OutlinedTextField(
                        value = value, onValueChange = onValueChange, label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { startVoice(voiceTag) }) { Icon(Icons.Default.Mic, "Dictar") } }
                    )
                }

                FieldWithMic("N° de Expediente", uiState.expedienteNro, { viewModel.updateData(uiState.copy(expedienteNro = it)) }, "expediente")
                FieldWithMic("Nombre del Imputado", uiState.nombreImputado, { viewModel.updateData(uiState.copy(nombreImputado = it)) }, "imputado")
                FieldWithMic("Delito", uiState.delito, { viewModel.updateData(uiState.copy(delito = it)) }, "delito")
                FieldWithMic("Perjudicado", uiState.perjudicado, { viewModel.updateData(uiState.copy(perjudicado = it)) }, "perjudicado")
                FieldWithMic("Motivo / Tipo Audiencia", uiState.tipoAudienciaReprogramada, { viewModel.updateData(uiState.copy(tipoAudienciaReprogramada = it)) }, "motivo")
                
                OutlinedTextField(
                    value = uiState.nuevaFecha,
                    onValueChange = { viewModel.updateData(uiState.copy(nuevaFecha = it)) },
                    label = { Text("Nueva Fecha (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = onOpenCalendar) { Icon(Icons.Default.CalendarMonth, "Abrir Calendario") }
                    }
                )
                
                FieldWithMic("Nueva Hora (HH:MM)", uiState.nuevaHora, { viewModel.updateData(uiState.copy(nuevaHora = it)) }, "hora")

                Button(
                    onClick = { viewModel.generateDocument() },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = "Generar")
                    Spacer(Modifier.width(8.dp))
                    Text("Generar Documento", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RightPanel(documentText: String, viewModel: MainViewModel, context: android.content.Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vista Previa del Documento", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = documentText,
                onValueChange = { viewModel.updateDocumentText(it) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Documento", documentText))
                        Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = documentText.isNotBlank()
                ) { Text("Copiar") }
                
                Button(
                    onClick = { Toast.makeText(context, "Imprimiendo...", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(1f),
                    enabled = documentText.isNotBlank()
                ) { Text("Imprimir") }
            }
        }
    }
}

@Composable
fun CalendarMonthView(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    eventsByDate: Map<String, List<CalendarEvent>>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ChevronLeft, "Mes anterior")
            }
            Text(
                text = "${currentMonth.month.name} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.ChevronRight, "Mes siguiente")
            }
        }
        
        val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
        val daysInMonth = currentMonth.lengthOfMonth()
        
        var dayCounter = 1
        for (row in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 1..7) {
                    if (row == 0 && col < firstDayOfWeek) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else if (dayCounter <= daysInMonth) {
                        val date = currentMonth.atDay(dayCounter)
                        val dateString = String.format("%04d-%02d-%02d", date.year, date.monthValue, date.dayOfMonth)
                        val hasEvents = eventsByDate.containsKey(dateString)
                        val isSelected = selectedDate == date
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (hasEvents) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
            if (dayCounter > daysInMonth) break
        }
    }
}
KOTLIN_EOF
