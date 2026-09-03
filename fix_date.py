with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """VoiceTextField("NUEVA FECHA (DD/MM/YYYY)", uiState.nuevaFecha, { viewModel.updateData(uiState.copy(nuevaFecha = it)) }, "fecha")"""

replacement = """Box(modifier = Modifier.fillMaxWidth().clickable {
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
                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("FIXED")
else:
    print("TARGET NOT FOUND")
