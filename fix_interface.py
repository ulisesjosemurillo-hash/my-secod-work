with open('app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    content = f.read()

content = content.replace("Android.onHeightChanged(document.documentElement.scrollHeight);", "")
content = content.replace("IconButton(onClick = { onCommand(\"changeCase\", \"upper\") }) { \n            Text(\"AA\", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)\n        }", 
"""IconButton(onClick = { onCommand("changeCase", "upper") }) { 
            Text("AA", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        IconButton(onClick = { onCommand("removeFormat", "") }) { 
            Icon(Icons.Default.FormatClear, "Borrar Formato")
        }""")

with open('app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(content)
