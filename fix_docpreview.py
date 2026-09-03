with open('app/src/main/java/com/example/DocumentPreview.kt', 'r') as f:
    content = f.read()

content = content.replace(".fillMaxSize()", ".fillMaxWidth()")
content = content.replace("Modifier.fillMaxSize()", "Modifier.fillMaxWidth()")

with open('app/src/main/java/com/example/DocumentPreview.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()
content = content.replace("modifier = Modifier.fillMaxSize()", "modifier = Modifier.fillMaxWidth()")
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
