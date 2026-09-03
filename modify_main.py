import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add dialogs
dialog_code = """
    // Dialog para mostrar las audiencias de un día
    var dayEventsDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    if (dayEventsDate != null && selectedEvent == null) {
        val dateStr = String.format("%04d-%02d-%02d", dayEventsDate!!.year, dayEventsDate!!.monthValue, dayEventsDate!!.dayOfMonth)
        val dayEvents = eventsByDate[dateStr] ?: emptyList()
        
        AlertDialog(
            onDismissRequest = { dayEventsDate = null },
            title = { Text("Audiencias del $dateStr") },
            text = {
                if (dayEvents.isEmpty()) {
                    Text("No hay audiencias programadas para este día.")
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dayEvents.forEach { event ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().clickable { selectedEvent = event }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Hora: ${event.time.ifBlank { "No disponible" }}", fontWeight = FontWeight.Bold)
                                    Text("Exp: ${event.expediente.ifBlank { "No disponible" }}")
                                    Text("Imputado: ${event.nombreImputado.ifBlank { "No disponible" }}")
                                    Text("Delito: ${event.delito.ifBlank { "No disponible" }}")
                                    Text("Tipo: ${event.tipoAudiencia.ifBlank { "No disponible" }}")
                                    Text("Juez: ${event.nombreJuez.ifBlank { "No disponible" }}")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dayEventsDate = null }) { Text("Cerrar") }
            }
        )
    }

    // Dialog para el detalle completo de una audiencia
    if (selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text("Detalle de Audiencia") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val ev = selectedEvent!!
                    fun detailRow(label: String, value: String) {
                        Text(text = "$label: ${value.ifBlank { "No disponible" }}", style = MaterialTheme.typography.bodyMedium)
                    }
                    detailRow("📅 Fecha", ev.date)
                    detailRow("🕐 Hora", ev.time)
                    detailRow("📁 Expediente", ev.expediente)
                    detailRow("👤 Imputado", ev.nombreImputado)
                    detailRow("⚖️ Delito", ev.delito)
                    detailRow("👥 Perjudicado", ev.perjudicado)
                    detailRow("📋 Tipo de audiencia", ev.tipoAudiencia)
                    detailRow("👨⚖️ Juez", ev.nombreJuez)
                    detailRow("👩💼 Secretario", ev.nombreSecretario)
                    detailRow("📜 Artículo CPP", ev.articuloCpp)
                    detailRow("ℹ️ Detalle", ev.description)
                    detailRow("📌 Estado", ev.estado)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ev = selectedEvent!!
                        val current = viewModel.uiState.value
                        viewModel.updateData(
                            current.copy(
                                expedienteNro = ev.expediente,
                                nombreImputado = ev.nombreImputado,
                                delito = ev.delito,
                                perjudicado = ev.perjudicado,
                                tipoAudienciaReprogramada = ev.tipoAudiencia,
                                nombreJuez = ev.nombreJuez,
                                nombreSecretario = ev.nombreSecretario,
                                articuloCpp = ev.articuloCpp
                                // Nota: no sobreescribimos nuevaFecha y nuevaHora aquí
                                // porque la idea es que seleccionen una *nueva* fecha.
                                // La fecha actual del evento es la original.
                            )
                        )
                        selectedEvent = null
                        dayEventsDate = null // Cerrar dialogos para ir al form
                    }
                ) {
                    Text("🔄 REPROGRAMAR ESTA AUDIENCIA")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEvent = null }) { Text("Atrás") }
            }
        )
    }
"""

content = content.replace("        // Mantener dialogos existentes", dialog_code + "\n        // Mantener dialogos existentes")

# We also need to change the main calendar in LeftPanel to set dayEventsDate instead of selectedDate
# Wait, LeftPanel calls CalendarMonthView with `onDateSelected = onDateSelected`.
# `onDateSelected = { selectedDate = it }` in LeftPanel invocation in MainScreen.
# Wait, we need it to show the dayEventsDate.
# In MainScreen, the `LeftPanel` invocation has `onDateSelected = { selectedDate = it }`.
# We should change it to `onDateSelected = { dayEventsDate = it }`.

content = content.replace(
    "onDateSelected = { selectedDate = it }", 
    "onDateSelected = { dayEventsDate = it; selectedDate = it }"
)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

