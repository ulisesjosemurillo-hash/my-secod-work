import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove UI components for fechaOriginal and horaOriginal
content = re.sub(r'VoiceTextField\("FECHA ORIGINAL[^\n]*\n', '', content)
content = re.sub(r'VoiceTextField\("HORA ORIGINAL[^\n]*\n', '', content)
content = re.sub(r'Text\("Fechas \(La original extraída no afecta el acta\)"[^\n]*\n', '', content)

# Bold VoiceTextField
target_tf = """OutlinedTextField(
                            value = value,"""
replacement_tf = """OutlinedTextField(
                            value = value,
                            textStyle = LocalTextStyle.current.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),"""
if target_tf in content:
    content = content.replace(target_tf, replacement_tf)
else:
    print("VoiceTextField OUTLINEDTEXTFIELD NOT FOUND")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
