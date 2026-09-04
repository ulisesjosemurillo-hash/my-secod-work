import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """                    SelectionGroup(
                        title = "JUEZ",
                        options = listOf("JACOBO MURILLO", "JENNY PENMAN", "OSMAN FAJARDO", "ADELA LAGOS"),
                        selectedOption = uiState.nombreJuez,
                        onOptionSelected = { viewModel.updateData(uiState.copy(nombreJuez = it)) }
                    )
                    SelectionGroup(
                        title = "SECRETARIO",
                        options = listOf("IVAN RENDON", "GERSON RODEZNO", "YORDI", "IRIS"),
                        selectedOption = uiState.nombreSecretario,
                        onOptionSelected = { viewModel.updateData(uiState.copy(nombreSecretario = it)) }
                    )"""

content = re.sub(r'                    VoiceTextField\("NOMBRE DEL JUEZ"[^\n]+\n                    VoiceTextField\("NOMBRE DEL SECRETARIO"[^\n]+', replacement, content)

selection_group_code = """
@Composable
fun SelectionGroup(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { option ->
            val isSelected = option == selectedOption
            OutlinedButton(
                onClick = { onOptionSelected(option) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
"""

content += selection_group_code

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
