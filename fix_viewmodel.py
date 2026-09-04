import re

with open('app/src/main/java/com/example/MainViewModel.kt', 'r') as f:
    content = f.read()

replacement = """        // Validación obligatoria
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
"""

content = re.sub(r'        // Validación obligatoria.*?        } else \{\n            _errorMessage.value = null\n        }', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainViewModel.kt', 'w') as f:
    f.write(content)
