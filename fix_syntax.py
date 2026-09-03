import re

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the stray brackets
content = re.sub(r'LaunchedEffect\(errorMessage\) \{.*?\n    \}\n\n\s*\}\n\s*\}\n\n\s*\n\n\s*val pickMedia', 'LaunchedEffect(errorMessage) {\n        errorMessage?.let {\n            Toast.makeText(context, it, Toast.LENGTH_LONG).show()\n            viewModel.clearError()\n        }\n    }\n\n    val pickMedia', content, flags=re.DOTALL)

with open('/app/applet/app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
