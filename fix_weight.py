with open('app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    content = f.read()

content = content.replace(".weight(1f),", ",")

with open('app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(content)
