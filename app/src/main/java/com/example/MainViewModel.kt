package com.example

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val localOcrService = LocalOcrService()
    // private val firestore = FirebaseFirestore.getInstance() // DESACTIVADO TEMPORALMENTE POR FALTA DE CONFIGURACION
    private val prefs = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ReprogramacionData())
    val uiState: StateFlow<ReprogramacionData> = _uiState.asStateFlow()

    private val _documentText = MutableStateFlow("")
    val documentText: StateFlow<String> = _documentText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isConstancia = MutableStateFlow(true)
    val isConstancia: StateFlow<Boolean> = _isConstancia.asStateFlow()

    private val _ocrRawText = MutableStateFlow("")
    val ocrRawText: StateFlow<String> = _ocrRawText.asStateFlow()

    private val _ocrFoundFields = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val ocrFoundFields: StateFlow<Map<String, Boolean>> = _ocrFoundFields.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        recoverData()
    }

    fun updateData(newData: ReprogramacionData) {
        _uiState.value = newData
        _documentText.value = DocumentGenerator.generate(newData, _isConstancia.value)
    }

    fun clearData() {
        _uiState.value = ReprogramacionData()
        _ocrRawText.value = ""
        _ocrFoundFields.value = emptyMap()
        generateDocument()
    }

    fun recoverData() {
        val recovered = ReprogramacionData(
            expedienteNro = prefs.getString("expedienteNro", "") ?: "",
            nombreImputado = prefs.getString("nombreImputado", "") ?: "",
            delito = prefs.getString("delito", "") ?: "",
            perjudicado = prefs.getString("perjudicado", "") ?: "",
            tipoAudienciaReprogramada = prefs.getString("tipoAudienciaReprogramada", "") ?: "",
            nuevaFecha = prefs.getString("nuevaFecha", "") ?: "",
            nuevaHora = prefs.getString("nuevaHora", "") ?: "",
            nombreJuez = prefs.getString("nombreJuez", "") ?: "",
            nombreSecretario = prefs.getString("nombreSecretario", "") ?: "",
            articuloCpp = prefs.getString("articuloCpp", "") ?: "",
            motivoReprogramacion = prefs.getString("motivoReprogramacion", "") ?: ""
        )
        _uiState.value = recovered
        generateDocument()
    }

    fun setConstancia(value: Boolean) {
        _isConstancia.value = value
        _documentText.value = DocumentGenerator.generate(_uiState.value, value)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateOcrRawText(newText: String) {
        _ocrRawText.value = newText
    }

    fun reProcessOcrText() {
        viewModelScope.launch {
            val text = _ocrRawText.value
            if (text.isBlank()) return@launch
            try {
                val extractedData = DataExtractor.parseOcrResult(text)
                applyExtractedData(extractedData)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error al reprocesar el texto OCR."
            }
        }
    }

    fun analyzeImage(bitmap: Bitmap, text: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val extractedData = if(text.isNotBlank()) {
                    _ocrRawText.value = text
                    DataExtractor.parseOcrResult(text)
                } else {
                    val rawText = localOcrService.analyzeImageForRawText(bitmap)
                    _ocrRawText.value = rawText
                    DataExtractor.parseOcrResult(rawText)
                }
                
                applyExtractedData(extractedData)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "No fue posible extraer la información. Revise la imagen o complete los campos manualmente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyExtractedData(data: ReprogramacionData) {
        val found = mutableMapOf<String, Boolean>()
        found["Expediente"] = data.expedienteNro != "NO IDENTIFICADO" && data.expedienteNro.isNotBlank()
        found["Imputado"] = data.nombreImputado != "NO IDENTIFICADO" && data.nombreImputado.isNotBlank()
        found["Delito"] = data.delito != "NO IDENTIFICADO" && data.delito.isNotBlank()
        found["Perjudicado"] = data.perjudicado != "NO IDENTIFICADO" && data.perjudicado.isNotBlank()
        found["Audiencia"] = data.tipoAudienciaReprogramada != "NO IDENTIFICADO" && data.tipoAudienciaReprogramada.isNotBlank()
        found["Artículo"] = data.articuloCpp != "NO IDENTIFICADO" && data.articuloCpp.isNotBlank()
        found["Juez"] = data.nombreJuez != "NO IDENTIFICADO" && data.nombreJuez.isNotBlank()
        found["Secretario"] = data.nombreSecretario != "NO IDENTIFICADO" && data.nombreSecretario.isNotBlank()
        _ocrFoundFields.value = found

        // As requested by user: DO NOT merge with old data. 
        // New photo means new data. Old data is completely wiped out by `data`.
        _uiState.value = data
        generateDocument()
    }

    fun generateDocument() {
        val data = _uiState.value
        
        // Save to prefs whenever we generate to keep it persistent across restarts
        prefs.edit()
            .putString("expedienteNro", data.expedienteNro)
            .putString("nombreImputado", data.nombreImputado)
            .putString("delito", data.delito)
            .putString("perjudicado", data.perjudicado)
            .putString("tipoAudienciaReprogramada", data.tipoAudienciaReprogramada)
            .putString("nuevaFecha", data.nuevaFecha)
            .putString("nuevaHora", data.nuevaHora)
            .putString("nombreJuez", data.nombreJuez)
            .putString("nombreSecretario", data.nombreSecretario)
            .putString("articuloCpp", data.articuloCpp)
            .putString("motivoReprogramacion", data.motivoReprogramacion)
            .apply()

        // Validación obligatoria
        val faltantes = mutableListOf<String>()
        if (data.expedienteNro.isBlank() || data.expedienteNro == "NO IDENTIFICADO") faltantes.add("N° de Expediente")
        if (data.nombreImputado.isBlank() || data.nombreImputado == "NO IDENTIFICADO") faltantes.add("Nombre del Imputado")
        if (data.delito.isBlank() || data.delito == "NO IDENTIFICADO") faltantes.add("Delito")
        if (data.tipoAudienciaReprogramada.isBlank() || data.tipoAudienciaReprogramada == "NO IDENTIFICADO") faltantes.add("Tipo de Audiencia")
        if (data.nuevaFecha.isBlank()) faltantes.add("Nueva Fecha")
        if (data.nuevaHora.isBlank()) faltantes.add("Nueva Hora")
        
        if (data.nombreJuez.isBlank() || data.nombreJuez == "NO IDENTIFICADO") {
            _errorMessage.value = "SELECCIONE UN JUEZ"
            return
        }
        if (data.nombreSecretario.isBlank() || data.nombreSecretario == "NO IDENTIFICADO") {
            _errorMessage.value = "SELECCIONE UN SECRETARIO"
            return
        }

        if (faltantes.isNotEmpty()) {
            _errorMessage.value = "Faltan campos obligatorios para generar un acta completa: ${faltantes.joinToString(", ")}"
            return
        } else {
            _errorMessage.value = null
        }

        
        val text = DocumentGenerator.generate(data, _isConstancia.value)
        _documentText.value = text
        saveToFirestore(data, text)
    }

    fun updateDocumentText(newText: String) {
        _documentText.value = newText
    }

    private fun saveToFirestore(data: ReprogramacionData, text: String) {
        viewModelScope.launch {
            try {
                val docData = hashMapOf(
                    "expedienteNro" to data.expedienteNro,
                    "nombreImputado" to data.nombreImputado,
                    "delito" to data.delito,
                    "fechaGeneracion" to System.currentTimeMillis(),
                    "documento" to text
                )
                // firestore.collection("reprogramaciones").document(UUID.randomUUID().toString()).set(docData) // DESACTIVADO
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
