import re

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

target = """    fun generateDocument() {
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

replacement = """    fun generateDocument() {
        val data = _uiState.value
        val text = DocumentGenerator.generate(data, _isConstancia.value)
        _documentText.value = text
        saveToFirestore(data, text)
    }"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
