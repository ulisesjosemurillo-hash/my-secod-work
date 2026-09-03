with open('app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    content = f.read()

# Enable zoom in settings
content = content.replace("settings.domStorageEnabled = true", "settings.domStorageEnabled = true\n                    settings.setSupportZoom(true)\n                    settings.builtInZoomControls = true\n                    settings.displayZoomControls = false")

# Remove user-scalable=no from meta tag
content = content.replace('<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />', '<meta name="viewport" content="width=device-width, initial-scale=1.0" />')

with open('app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(content)
