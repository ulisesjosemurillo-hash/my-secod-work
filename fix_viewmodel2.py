import re

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

# 1. Ensure document updates dynamically
target_update = """    fun updateData(newData: ReprogramacionData) {
        _uiState.value = newData
    }"""
replacement_update = """    fun updateData(newData: ReprogramacionData) {
        _uiState.value = newData
        _documentText.value = DocumentGenerator.generate(newData, _isConstancia.value)
    }"""
content = content.replace(target_update, replacement_update)

# 2. Same for setConstancia
target_const = """    fun setConstancia(value: Boolean) {
        _isConstancia.value = value
    }"""
replacement_const = """    fun setConstancia(value: Boolean) {
        _isConstancia.value = value
        _documentText.value = DocumentGenerator.generate(_uiState.value, value)
    }"""
content = content.replace(target_const, replacement_const)

# 3. Clean generateDocument to just save
target_gen = """    fun generateDocument() {
        val data = _uiState.value
        if (data.expedienteNro.isBlank()) {
            _errorMessage.value = "Debe completar el N° de Expediente."
            return
        }
        if (data.nombreImputado.isBlank()) {
            _errorMessage.value = "Debe completar el Nombre del Imputado."
            return
        }
        if (data.delito.isBlank()) {
            _errorMessage.value = "Debe completar el Delito."
            return
        }
        if (data.perjudicado.isBlank()) {
            _errorMessage.value = "Debe completar el Perjudicado."
            return
        }
        if (data.tipoAudienciaReprogramada.isBlank()) {
            _errorMessage.value = "Debe completar el Tipo de Audiencia a Reprogramar."
            return
        }
        if (data.nuevaFecha.isBlank()) {
            _errorMessage.value = "Debe seleccionar una Nueva Fecha."
            return
        }
        if (data.nuevaHora.isBlank()) {
            _errorMessage.value = "Debe seleccionar una Nueva Hora."
            return
        }

        val text = DocumentGenerator.generate(data, _isConstancia.value)
        _documentText.value = text
        saveToFirestore(data, text)
    }"""

replacement_gen = """    fun generateDocument() {
        val data = _uiState.value
        val text = DocumentGenerator.generate(data, _isConstancia.value)
        _documentText.value = text
        saveToFirestore(data, text)
    }"""
content = content.replace(target_gen, replacement_gen)

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
