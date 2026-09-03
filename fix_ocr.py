with open('app/src/main/java/com/example/LocalOcrService.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)',
    'private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }'
)

with open('app/src/main/java/com/example/LocalOcrService.kt', 'w') as f:
    f.write(content)
