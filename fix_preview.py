with open('/app/applet/app/src/main/java/com/example/DocumentPreview.kt', 'r') as f:
    content = f.read()

content = content.replace('.heightIn(min = 1000.dp)', '.height(1200.dp)')

with open('/app/applet/app/src/main/java/com/example/DocumentPreview.kt', 'w') as f:
    f.write(content)
