with open('/app/applet/app/src/main/java/com/example/DocumentGenerator.kt', 'r') as f:
    content = f.read()

content = content.replace('<hr style="border: 0; border-top: 1px dashed #ccc; margin: 24px 0;" />', '<br/><br/>')

with open('/app/applet/app/src/main/java/com/example/DocumentGenerator.kt', 'w') as f:
    f.write(content)
