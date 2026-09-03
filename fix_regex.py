with open('app/src/main/java/com/example/DataExtractor.kt', 'r') as f:
    content = f.read()

content = content.replace('"""\\\\', '"""\\')
content = content.replace('\\\\', '\\')

with open('app/src/main/java/com/example/DataExtractor.kt', 'w') as f:
    f.write(content)
