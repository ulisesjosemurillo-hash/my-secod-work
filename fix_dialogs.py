with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

dialogs = """
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

    if (selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text("Detalle de Audiencia") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val ev = selectedEvent!!
                    @Composable
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selectedEvent = null }) { Text("Cerrar") }
                    Button(onClick = {
                        val ev = selectedEvent!!
                        viewModel.updateData(
                            viewModel.uiState.value.copy(
                                expedienteNro = ev.expediente,
                                nombreImputado = ev.nombreImputado,
                                delito = ev.delito,
                                perjudicado = ev.perjudicado,
                                tipoAudienciaReprogramada = ev.tipoAudiencia,
                                nombreJuez = ev.nombreJuez,
                                nombreSecretario = ev.nombreSecretario,
                                fechaOriginal = ev.date,
                                horaOriginal = ev.time
                            )
                        )
                        selectedEvent = null
                        dayEventsDate = null
                    }) { Text("Reprogramar esta Audiencia") }
                }
            }
        )
    }
"""

content = content.replace('        // Dialog para mostrar las audiencias de un día', '')
# Insert just before the end of MainScreen function.
# Wait, MainScreen ends at line 559 in my grep.
target = """            }
        }
    }
}

@Composable
fun RightPanel"""
replacement = """            }
        }
        
""" + dialogs + """
    }
}

@Composable
fun RightPanel"""

content = content.replace(target, replacement)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
