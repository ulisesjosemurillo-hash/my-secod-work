import re

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'r') as f:
    rte_content = f.read()

replacement = """
    LaunchedEffect(initialHtml) {
        if (webViewRef != null && initialHtml.isNotBlank()) {
            val b64 = Base64.encodeToString(initialHtml.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            webViewRef?.evaluateJavascript(
                "var newHtml = decodeURIComponent(escape(window.atob('$b64'))); if (getHtml() !== newHtml) { setHtml(newHtml); }"
            ) {}
        }
    }
"""

rte_content = re.sub(
    r'LaunchedEffect\(initialHtml\) \{.*?webViewRef\?\.evaluateJavascript\(.*?\).*?\}',
    replacement.strip(),
    rte_content,
    flags=re.DOTALL
)

with open('/app/applet/app/src/main/java/com/example/RichTextEditor.kt', 'w') as f:
    f.write(rte_content)
