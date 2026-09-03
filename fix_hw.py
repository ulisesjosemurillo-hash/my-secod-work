import re

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    content = f.read()

replacement = """WebView(context).apply {
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    settings.javaScriptEnabled = true"""

content = content.replace("WebView(context).apply {\n                    settings.javaScriptEnabled = true", replacement)

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(content)

