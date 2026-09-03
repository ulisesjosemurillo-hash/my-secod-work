import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update the reprogramar click to include original date and time
content = content.replace(
    'articuloCpp = ev.articuloCpp',
    'articuloCpp = ev.articuloCpp,\n                                fechaOriginal = ev.date,\n                                horaOriginal = ev.time'
)

# Add UI fields in LeftPanel
fields_to_add = """
                if (uiState.fechaOriginal.isNotBlank()) {
                    OutlinedTextField(
                        value = uiState.fechaOriginal, onValueChange = {}, label = { Text("Fecha Original") },
                        modifier = Modifier.fillMaxWidth(), readOnly = true
                    )
                }
                if (uiState.horaOriginal.isNotBlank()) {
                    OutlinedTextField(
                        value = uiState.horaOriginal, onValueChange = {}, label = { Text("Hora Original") },
                        modifier = Modifier.fillMaxWidth(), readOnly = true
                    )
                }
"""
content = content.replace(
    'FieldWithMic("Motivo / Tipo Audiencia", uiState.tipoAudienciaReprogramada, { viewModel.updateData(uiState.copy(tipoAudienciaReprogramada = it)) }, "motivo")',
    'FieldWithMic("Motivo / Tipo Audiencia", uiState.tipoAudienciaReprogramada, { viewModel.updateData(uiState.copy(tipoAudienciaReprogramada = it)) }, "motivo")' + fields_to_add
)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
