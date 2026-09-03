import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement_fields = """
                val isConstancia by viewModel.isConstancia.collectAsState()
                
                Text("Tipo de Generación", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isConstancia,
                        onClick = { viewModel.setConstancia(true) }
                    )
                    Text("Con Constancia", modifier = Modifier.padding(end = 16.dp).clickable { viewModel.setConstancia(true) })
                    RadioButton(
                        selected = !isConstancia,
                        onClick = { viewModel.setConstancia(false) }
                    )
                    Text("De Oficio", modifier = Modifier.clickable { viewModel.setConstancia(false) })
                }
                
                FieldWithMic("N° de Expediente", uiState.expedienteNro, { viewModel.updateData(uiState.copy(expedienteNro = it)) }, "expediente")
                FieldWithMic("Nombre del Imputado", uiState.nombreImputado, { viewModel.updateData(uiState.copy(nombreImputado = it)) }, "imputado")
                FieldWithMic("Delito", uiState.delito, { viewModel.updateData(uiState.copy(delito = it)) }, "delito")
                FieldWithMic("Perjudicado", uiState.perjudicado, { viewModel.updateData(uiState.copy(perjudicado = it)) }, "perjudicado")
                FieldWithMic("Tipo de Audiencia a Reprogramar", uiState.tipoAudienciaReprogramada, { viewModel.updateData(uiState.copy(tipoAudienciaReprogramada = it)) }, "tipo_audiencia")
                FieldWithMic("Motivo de la Reprogramación", uiState.motivoReprogramacion, { viewModel.updateData(uiState.copy(motivoReprogramacion = it)) }, "motivo")
                
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
                FieldWithMic("Artículo del Código Procesal Penal", uiState.articuloCpp, { viewModel.updateData(uiState.copy(articuloCpp = it)) }, "articulo")
                FieldWithMic("Nombre del Juez", uiState.nombreJuez, { viewModel.updateData(uiState.copy(nombreJuez = it)) }, "juez")
                FieldWithMic("Nombre del Secretario", uiState.nombreSecretario, { viewModel.updateData(uiState.copy(nombreSecretario = it)) }, "secretario")

                Button(
                    onClick = { viewModel.generateDocument() },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("⚖️ GENERAR ACTA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
"""

content = re.sub(
    r'FieldWithMic\("N° de Expediente".*?Text\("Generar Documento", fontWeight = FontWeight\.Bold\)\n                \}',
    replacement_fields.strip(),
    content,
    flags=re.DOTALL
)

# We need to import collectAsState inside LeftPanel or make sure it's available.
# Since it's in a composable, we can just use `collectAsState()` if it's imported, which it is.

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
